package com.tqz.offline

import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.sql.SparkSession
import org.jblas.DoubleMatrix

case class ProductRating(userId: Int, productId: Int, score: Double, timestamp: Int)

case class MongoConfig(uri: String, db: String)

// 定义标准推荐对象
case class Recommendation(productId: Int, score: Double)

// 定义用户的推荐列表
case class UserRecs(userId: Int, recs: Seq[Recommendation])

// 定义商品相似度列表
case class ProductRecs(productId: Int, recs: Seq[Recommendation])

object OfflineRecommender {
  // 定义mongodb中存储的表名
  val MONGODB_RATING_COLLECTION = "Rating"

  val USER_RECS = "UserRecs"
  val PRODUCT_RECS = "ProductRecs"
  val USER_MAX_RECOMMENDATION = 20

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://127.0.0.1:27017/recommender",
      "mongo.db" -> "recommender"
    )
    // 创建一个spark config
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OfflineRecommender")
    // 创建spark session
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 加载数据
    val ratingRDD = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_RATING_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[ProductRating]
      .rdd
      .map(
        rating => (rating.userId, rating.productId, rating.score)
      ).cache()

    // 提取出所有用户和商品的数据集
    val userRDD = ratingRDD.map(_._1).distinct()
    val productRDD = ratingRDD.map(_._2).distinct()

    //  构建 userProducts：ratingRDD.map( x => (x._1, x._2))

    // 核心计算过程
    // 1. 训练隐语义模型
    val trainData = ratingRDD.map(x => Rating(x._1, x._2, x._3))
    // 定义模型训练的参数，rank隐特征个数，iterations迭代词数，lambda正则化系数
    val (rank, iterations, lambda) = (5, 10, 0.1)
    // rank：表示隐特征的维度 K
    // iterations：迭代次数，交替相乘的次数
    val model = ALS.train(trainData, rank, iterations, lambda) //得到一个成熟的预测模型，下面使用该模型去预测


    // 2. 获得预测评分矩阵，得到用户的推荐列表
    // 用userRDD和productRDD做一个笛卡尔积，得到空的userProductsRDD表示的评分矩阵
    val userProducts = userRDD.cartesian(productRDD)
    val preRating = model.predict(userProducts)

    // 从预测评分矩阵中提取得到用户推荐列表
    val userRecs = preRating.filter(_.rating > 0)
      .map(
        rating => (rating.user, (rating.product, rating.rating))
      )
      .groupByKey()
      .map {
        case (userId, recs) =>
          UserRecs(userId, recs.toList.sortWith(_._2 > _._2).take(USER_MAX_RECOMMENDATION).map(x => Recommendation(x._1, x._2)))
      }
      .toDF()
    userRecs.write
      .option("uri", mongoConfig.uri)
      .option("collection", USER_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    // 3. 利用商品的特征向量，计算商品的相似度列表
    val productFeatures = model.productFeatures.map {
      case (productId, features) => (productId, new DoubleMatrix(features))
    }
    // 两两配对商品，计算余弦相似度
    val productRecs = productFeatures.cartesian(productFeatures)
      .filter {
        // 自己与自己做笛卡尔积，结果的相似度一定很高，所以要排除掉自己与自己做积
        // a 就是做笛卡尔积的 this元素，b 就是 other 元素，他们不能是同一个
        case (a, b) => a._1 != b._1
      }
      // 计算余弦相似度
      .map {
      // 然后计算余弦相似度
      case (a, b) =>
        val simScore = consinSim(a._2, b._2)
        (a._1, (b._1, simScore))
    }
      .filter(_._2._2 > 0.4)
      .groupByKey()
      .map {
        case (productId, recs) =>
          ProductRecs(productId, recs.toList.sortWith(_._2 > _._2).map(x => Recommendation(x._1, x._2)))
      }
      .toDF()
    productRecs.write
      .option("uri", mongoConfig.uri)
      .option("collection", PRODUCT_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    spark.stop()
  }

  def consinSim(product1: DoubleMatrix, product2: DoubleMatrix): Double = {
    product1.dot(product2) / (product1.norm2() * product2.norm2())
  }
}
