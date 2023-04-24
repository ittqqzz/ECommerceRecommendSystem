package com.tqz.itemcf

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

case class ProductRating(userId: Int, productId: Int, score: Double, timestamp: Int)

case class MongoConfig(uri: String, db: String)

// 定义标准推荐对象
case class Recommendation(productId: Int, score: Double)

// 定义商品相似度列表
case class ProductRecs(productId: Int, recs: Seq[Recommendation])

object ItemCFRecommender {
  // 定义常量和表名
  val MONGODB_RATING_COLLECTION = "Rating"
  val ITEM_CF_PRODUCT_RECS = "ItemCFProductRecs"
  val MAX_RECOMMENDATION = 10

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://127.0.0.1:27017/recommender",
      "mongo.db" -> "recommender"
    )
    // 创建一个spark config
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("ItemCFRecommender")
    // 创建spark session
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 加载数据，转换成 DF 进行处理
    val ratingDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[ProductRating]
      .map(
        x => (x.userId, x.productId, x.score)
      )
      .toDF("userId", "productId", "score")
      .cache()

    // 核心算法，计算同现相似度，得到商品的相似列表


    /*
      统计每个商品的评分个数，按照 productId 来做 groupBy
      得到的数据是：
      | productId | count |
      | 12345689  |  10   |
     */
    val productRatingCountDF = ratingDF.groupBy("productId").count()


    /*
      在原有的评分表上 rating 添加 count
      join 结果：
      +---------+------+-----+-----+
      |productId|userId|score|count|
      +---------+------+-----+-----+
      |505556   |13784 |3.0  |172  |
                 ......
     */
    val ratingWithCountDF = ratingDF.join(productRatingCountDF, "productId")

    // 将评分按照用户 id 两两配对，统计两个商品被同一个用户评分过的次数
    // 也就是计算公式里面分子处的交集
    val joinedDF = ratingWithCountDF.join(ratingWithCountDF, "userId")
      .toDF("userId", "product1", "score1", "count1", "product2", "score2", "count2")
      .select("userId", "product1", "count1", "product2", "count2")
    // 创建一张临时表，用于写 sql 查询
    joinedDF.createOrReplaceTempView("joined")

    // 按照 product1, product2 做 groupBy，统计 userId 的数量，就是对两个商品同时评分的人数
    val cooccurrenceDF = spark.sql(
      """
        |select product1
        |, product2
        |, count(userId) as cocount
        |, first(count1) as count1
        |, first(count2) as count2
        |from joined
        |group by product1, product2
      """.stripMargin
    ).cache()

    // 提取需要的数据，包装成( productId1, (productId2, score) )
    val simDF = cooccurrenceDF.map {
      row =>
        val coocSim = cooccurrenceSim(row.getAs[Long]("cocount")
          , row.getAs[Long]("count1")
          , row.getAs[Long]("count2"))
        (row.getInt(0), (row.getInt(1), coocSim))
    }
      .rdd
      .groupByKey()
      .map {
        case (productId, recs) =>
          ProductRecs(productId, recs.toList
            .filter(x => x._1 != productId) // 在 recs 中过滤掉与 productId 相同的商品，不同自己推荐自己
            .sortWith(_._2 > _._2) // 按照分数降序排序
            .take(MAX_RECOMMENDATION)
            .map(x => Recommendation(x._1, x._2)) // 组装成被推荐列表的格式 (productId, score)
          ) // 最后封装成存入 MongoDB 的格式 (productId, 多个(productId, score))
      }
      .toDF()

    // 保存到mongodb
    simDF.write
      .option("uri", mongoConfig.uri)
      .option("collection", ITEM_CF_PRODUCT_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    spark.stop()
  }

  // 按照公式计算同现相似度
  def cooccurrenceSim(coCount: Long, count1: Long, count2: Long): Double = {
    coCount / math.sqrt(count1 * count2)
  }
}
