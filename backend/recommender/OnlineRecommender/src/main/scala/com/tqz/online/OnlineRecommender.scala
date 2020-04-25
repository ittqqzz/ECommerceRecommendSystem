package com.tqz.online

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.{MongoClient, MongoClientURI}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

// 定义一个连接助手对象，建立到redis和mongodb的连接
object ConnHelper extends Serializable {
  // 懒变量定义，使用的时候才初始化
  lazy val jedis = new Jedis("127.0.0.1")
  lazy val mongoClient = MongoClient(MongoClientURI("mongodb://127.0.0.1:27017/recommender"))
}

case class MongoConfig(uri: String, db: String)

// 定义标准推荐对象
case class Recommendation(productId: Int, score: Double)

// 定义用户的推荐列表
case class UserRecs(userId: Int, recs: Seq[Recommendation])

// 定义商品相似度列表
case class ProductRecs(productId: Int, recs: Seq[Recommendation])

object OnlineRecommender {
  // 定义常量和表名
  val MONGODB_RATING_COLLECTION = "Rating"
  val STREAM_RECS = "StreamRecs"
  val PRODUCT_RECS = "ProductRecs"

  val MAX_USER_RATING_NUM = 20
  val MAX_SIM_PRODUCTS_NUM = 20

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://127.0.0.1:27017/recommender",
      "mongo.db" -> "recommender",
      "kafka.topic" -> "recommender"
    )

    // 创建spark conf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OnlineRecommender")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()
    val sc = spark.sparkContext
    // Duration 应该足够长，能够支撑实时计算结束
    val ssc = new StreamingContext(sc, Seconds(2))

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 加载数据：相似度矩阵，矩阵应该广播出去
    val simProductsMatrix = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", PRODUCT_RECS)
      .format("com.mongodb.spark.sql")
      .load()
      .as[ProductRecs]
      .rdd
      // 为了后续查询相似度方便，把数据转换成 map 形式
      .map { item =>
      (item.productId, item.recs.map(x => (x.productId, x.score)).toMap)
    }
      .collectAsMap()

    // 定义广播变量，将相似度矩阵广播出去，也就是把 ProductRecs 数据集广播出去
    // simProductsMatrix 是一个 map，KEY 是 productId ，VALUE 是与该 productId 相似的 productId 以及 score
    val simProductsMatrixBC = sc.broadcast(simProductsMatrix)

    // 创建 kafka 配置参数
    val kafkaParam = Map(
      "bootstrap.servers" -> "127.0.0.1:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "recommender",
      "auto.offset.reset" -> "latest"
    )
    // 创建一个 DataStream
    val kafkaStream = KafkaUtils.createDirectStream[String, String](ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array(config("kafka.topic")), kafkaParam)
    )
    // 对kafkaStream进行处理，产生评分流，userId|productId|score|timestamp
    val ratingStream = kafkaStream.map { msg =>
      var attr = msg.value().split("\\|")
      (attr(0).toInt, attr(1).toInt, attr(2).toDouble, attr(3).toInt)
    }

    // 核心算法部分，定义评分流的处理流程
    // ratingStream 是一个数据集，也就是用户的多个评分数据 (userId, productId, score, timestamp)
    ratingStream.foreachRDD { // foreachRDD 拿到的是一定时间间隔内的一组 RDD （评分数据）
      rdds =>
        rdds.foreach { // 然后遍历这些评分数据
          case (userId, productId, score, timestamp) =>
            println("<<<<<<<<<<<<<<<<<< rating data coming ! >>>>>>>>>>>>>>>>>>")
            println("<<<<<<<<<<<<<<<<<<" + userId + "|" + productId + "|" + score + "|" + timestamp + ">>>>>>>>>>>>>>>>>>")

            // 核心算法流程
            // 1. 从redis里取出当前用户的最近评分，保存成一个数组 Array[(productId, score)]
            val userRecentlyRatings = getUserRecentlyRatings(MAX_USER_RATING_NUM, userId, ConnHelper.jedis)

            // 2. 从相似度矩阵中获取当前商品最相似的商品列表，作为备选列表，保存成一个数组 Array[productId]
            val candidateProducts = getTopSimProducts(MAX_SIM_PRODUCTS_NUM, productId, userId, simProductsMatrixBC.value)

            // 3. 计算每个备选商品的推荐优先级，得到当前用户的实时推荐列表，保存成 Array[(productId, score)]
            val streamRecs = computeProductScore(candidateProducts, userRecentlyRatings, simProductsMatrixBC.value)

            // 4. 把推荐列表保存到 mongodb
            saveDataToMongoDB(userId, streamRecs)
        }
    }

    // 启动streaming
    ssc.start()

    println("streaming started !")

    ssc.awaitTermination()

  }

  /**
    * 从redis里取出当前用户的最近评分，保存成一个数组 Array[(productId, score)]
    * 从redis里获取最近num次评分
    * Redis 里面存储的数据格式是：KEY(  userId: id  ) ----- VALUE(   productId:score  )
    */

  import scala.collection.JavaConversions._

  def getUserRecentlyRatings(num: Int, userId: Int, jedis: Jedis): Array[(Int, Double)] = {
    // 从 redis 中用户的评分队列里获取评分数据，list键名为 uid:USERID，值格式是 PRODUCTID:SCORE
    jedis.lrange("userId:" + userId.toString, 0, num)
      .map { item =>
        val attr = item.split("\\:")
        (attr(0).trim.toInt, attr(1).trim.toDouble)
      }
      .toArray
  }

  /**
    * 从相似度矩阵中获取当前商品最相似的商品列表，作为备选列表，保存成一个数组 Array[productId]
    * 获取当前商品的相似列表，并过滤掉用户已经评分过的，作为备选列表
    */
  /**
    *
    * @param num         取前 num 个数据
    * @param productId   需要寻找与之相似商品的 productId
    * @param userId      商品推荐人
    * @param simProducts 相似度矩阵，从 ProductRecs 里面获取的，与该 productId 相似的 ( productId, score )
    * @param mongoConfig MongoDB 连接配置
    * @return
    */
  def getTopSimProducts(num: Int,
                        productId: Int,
                        userId: Int,
                        simProducts: scala.collection.Map[Int, scala.collection.immutable.Map[Int, Double]]) // Map 里面套 Map
                       (implicit mongoConfig: MongoConfig): Array[Int] = {
    // 从广播变量相似度矩阵中（simProducts，他是一个 map，KEY 是 productId，VALUE 是 score）拿到当前商品的相似度列表
    val allSimProducts = simProducts(productId).toArray // 取出对应 productId 的评分列表

    // 从 Rating 中获得用户已经评分过的商品，过滤掉，排序输出
    val ratingCollection = ConnHelper.mongoClient(mongoConfig.db)(MONGODB_RATING_COLLECTION)
    val ratingExist = ratingCollection.find(MongoDBObject("userId" -> userId)) // 从已获取的 Rating 中过滤出只含 userId 的数据
      .toArray
      .map { item => // 在（userId,productId,score,timestamp）中只需要 productId
        item.get("productId").toString.toInt
      }
    // 从所有的相似商品中进行过滤
    // 如果 allSimProducts 里面的某一项在 ratingExist 里面的话就要过滤，应该 productId 取不在 ratingExist 里面的数据
    allSimProducts.filter(x => !ratingExist.contains(x._1)) // x 的格式上岗面提到过，就是(productId, score)
      .sortWith(_._2 > _._2) // 按照 score 降序排序
      .take(num)
      .map(x => x._1) // 最后只需要得到 productId 就行了，所以要将二元组转为一元组
  }

  /**
    * 计算每个备选商品的推荐优先级，得到当前用户的实时推荐列表，保存成 Array[(productId, score)]
    * 计算每个备选商品的推荐得分
    *
    * @param candidateProducts   上面算出来的相似 productId
    * @param userRecentlyRatings 从 redis 里面获取的 (productId, score)
    * @param simProducts         相似度矩阵，从 ProductRecs 里面获取到的
    * @return
    */
  def computeProductScore(candidateProducts: Array[Int],
                          userRecentlyRatings: Array[(Int, Double)],
                          simProducts: scala.collection.Map[Int, scala.collection.immutable.Map[Int, Double]])
  : Array[(Int, Double)] = {
    // 定义一个长度可变数组 ArrayBuffer，用于保存每一个备选商品的基础得分，(productId, score)
    val scores = scala.collection.mutable.ArrayBuffer[(Int, Double)]()

    // 定义两个 map，用于保存每个商品的高分和低分的计数器，productId -> count
    // 增强因子
    val increMap = scala.collection.mutable.HashMap[Int, Int]()
    // 削弱因子
    val decreMap = scala.collection.mutable.HashMap[Int, Int]()

    // 遍历每个备选商品，计算和已评分商品的相似度
    for (candidateProduct <- candidateProducts; userRecentlyRating <- userRecentlyRatings) {
      // 从相似度矩阵中获取当前备选商品和当前已评分商品间的相似度
      val simScore = getProductsSimScore(candidateProduct, userRecentlyRating._1, simProducts)
      if (simScore > 0.4) {
        // 按照公式进行加权计算，得到基础评分
        scores += ((candidateProduct, simScore * userRecentlyRating._2))
        if (userRecentlyRating._2 > 3) {
          // 在增强因子 map 中给 productId 的 score 加 1
          increMap(candidateProduct) = increMap.getOrDefault(candidateProduct, 0) + 1
        } else {
          decreMap(candidateProduct) = decreMap.getOrDefault(candidateProduct, 0) + 1
        }
      }
    }

    // 根据公式计算所有的推荐优先级，首先以 productId 做 groupBy
    scores.groupBy(_._1).map {
      // 注意：在上面获取的 scores 虽然是一个二元组 (productId, score)，但是通过 groupBy 后，相同的 productId 的 score 或被聚合成 List
      case (productId, scoreList) =>
        // 按照公式给当前的 peoductId 计算 Euq，先求和再得到均值，再计算 lg
        (productId, scoreList.map(_._2).sum / scoreList.length + log(increMap.getOrDefault(productId, 1)) - log(decreMap.getOrDefault(productId, 1)))
    }
      // 返回推荐列表，按照得分排序
      .toArray
      .sortWith(_._2 > _._2)
  }

  /**
    *
    * @param product1    从 mongoDB 里面得到的 productId
    * @param product2    从 Redis 里面得到的 productId
    * @param simProducts 相似度矩阵
    * @return
    */
  def getProductsSimScore(product1: Int, product2: Int,
                          simProducts: scala.collection.Map[Int, scala.collection.immutable.Map[Int, Double]]): Double = {
    // 通过模式匹配寻找

    // 如果在相似度矩阵里面发现含有 p1 的数据，就从这些数据里面寻找 是否还有 p2 的数据，都有的话返回 score，否则返回 0.0
    simProducts.get(product1) match {
      case Some(sims) => sims.get(product2) match {
        case Some(score) => score
        case None => 0.0
      }
      case None => 0.0
    }
  }

  // 自定义log函数，以 N 为底
  def log(m: Int): Double = {
    // 底数
    val N = 10
    // 采用数学中的换底公式，将 log 默认的以 e 为底改为 以 N 为底
    math.log(m) / math.log(N) // 转为以 N 为底的 logN(m)
  }

  // 写入mongodb
  def saveDataToMongoDB(userId: Int, streamRecs: Array[(Int, Double)])(implicit mongoConfig: MongoConfig): Unit = {
    val streamRecsCollection = ConnHelper.mongoClient(mongoConfig.db)(STREAM_RECS)
    // 按照userId查询并更新
    streamRecsCollection.findAndRemove(MongoDBObject("userId" -> userId))
    streamRecsCollection.insert(MongoDBObject("userId" -> userId,
      "recs" -> streamRecs.map(x => MongoDBObject("productId" -> x._1, "score" -> x._2))))
  }

}
