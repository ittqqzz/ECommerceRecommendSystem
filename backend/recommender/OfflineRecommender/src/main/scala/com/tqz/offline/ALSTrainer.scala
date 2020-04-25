package com.tqz.offline

import breeze.numerics.sqrt
import com.tqz.offline.OfflineRecommender.MONGODB_RATING_COLLECTION
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object ALSTrainer {
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
        rating => Rating(rating.userId, rating.productId, rating.score)
      ).cache()

    // 数据集切分成训练集和测试集
    val splits = ratingRDD.randomSplit(Array(0.8, 0.2))
    val trainingRDD = splits(0)
    val testingRDD = splits(1)

    // 核心实现：输出最优参数
    adjustALSParams(trainingRDD, testingRDD)

    spark.stop()
  }

  def adjustALSParams(trainData: RDD[Rating], testData: RDD[Rating]): Unit = {
    // 遍历数组中定义的参数取值
    // 尝试给 rank、lambda 赋予不同的值来求解最佳参数
    val result = for (rank <- Array(5, 10, 20, 50); lambda <- Array(1, 0.1, 0.01))
    // yield：把for循环中每一次的中间结果都保存下来
      yield {
        val model = ALS.train(trainData, rank, 10, lambda)
        // 训练出来模型后就可以计算 rmse 了
        val rmse = getRMSE(model, testData)
        (rank, lambda, rmse)
      }
    // 按照 rmse 排序并输出最优参数
    println("=============================================")
    println(result.minBy(_._3)) // (5,0.1,1.31962429882249)：（rank、；lambda、rmse）
  }

  // 为什么要通过构造二元组来处理该公式的解释：https://www.aboutyun.com/thread-19999-1-1.html
  def getRMSE(model: MatrixFactorizationModel, testData: RDD[Rating]): Double = {
    // 构建 userProducts，得到预测评分矩阵
    val userProducts = testData.map(item => (item.user, item.product))
    val predictRating = model.predict(userProducts)

    // 按照公式计算rmse，首先把预测评分和实际评分表按照(userId, productId)做一个连接
    val observed = testData.map(item => ((item.user, item.product), item.rating))
    val predict = predictRating.map(item => ((item.user, item.product), item.rating))

    sqrt(
      // join 就是内连接，返回两个表的交集（阴影）部分
      observed.join(predict).map {
        // 前面是 key，后面是 value
        case ((userId, productId), (actual, pre)) =>
          val err = actual - pre
          err * err
      }.mean() // 计算此 RDD 元素的均值
    )
  }

}
