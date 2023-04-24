package com.tqz.content

import org.apache.spark.SparkConf
import org.apache.spark.ml.feature.{HashingTF, IDF, Tokenizer}
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.sql.SparkSession
import org.jblas.DoubleMatrix

case class Product(productId: Int, name: String, imageUrl: String, categories: String, tags: String)

case class MongoConfig(uri: String, db: String)

// 定义标准推荐对象
case class Recommendation(productId: Int, score: Double)

// 定义商品相似度列表
case class ProductRecs(productId: Int, recs: Seq[Recommendation])

object ContentRecommender {
  // 定义mongodb中存储的表名
  val MONGODB_PRODUCT_COLLECTION = "Product"
  val CONTENT_PRODUCT_RECS = "ContentBasedProductRecs"

  def main(args: Array[String]): Unit = {
    val config = Map(
      "spark.cores" -> "local[*]",
      "mongo.uri" -> "mongodb://127.0.0.1:27017/recommender",
      "mongo.db" -> "recommender"
    )
    // 创建一个spark config
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("ContentRecommender")
    // 创建spark session
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._
    implicit val mongoConfig = MongoConfig(config("mongo.uri"), config("mongo.db"))

    // 载入数据，做预处理
    val productTagsDF = spark.read
      .option("uri", mongoConfig.uri)
      .option("collection", MONGODB_PRODUCT_COLLECTION)
      .format("com.mongodb.spark.sql")
      .load()
      .as[Product]
      .map(
        // 分词器默认按照空格分词，这里把 tag 的 | 替换为空格
        x => (x.productId, x.name, x.tags.map(c => if (c == '|') ' ' else c))
      )
      .toDF("productId", "name", "tags")
      .cache() // 使用 cache 优化性能

    // 用 TF-IDF 提取商品特征向量
    // 1. 实例化一个分词器，用来做分词，默认按照空格分词。下面的意思就是输入的数据的列名叫 tags，分词结束后输出的列叫 words
    val tokenizer = new Tokenizer().setInputCol("tags").setOutputCol("words")
    // 用分词器做转换，得到增加一个新列 words 的 DF
    val wordsDataDF = tokenizer.transform(productTagsDF)


    /*
      2. 定义一个 HashingTF 工具，计算频次

      原始特征通过 hash 函数，映射到一个索引值。
      后面只需要统计这些索引值的频率，就可以知道对应词的频率。

      transform 方法会把词哈希成向量，结果类似于这样：(800,[67,259,267,350,579,652],[1.0,1.0,1.0,1.0,1.0,1.0])
      800 表示纬度、特征向量数、哈希表的桶数
      [67,259,267,350,579,652] 表示哈希到下标为这些数字上的词语
      [1.0,1.0,1.0,1.0,1.0,1.0] 表示上面这些词出现的次数

      注意：这是一个稀疏矩阵，只显示非 0 的结果
     */
    val hashingTF = new HashingTF().setInputCol("words").setOutputCol("rawFeatures").setNumFeatures(800)
    val featurizedDataDF = hashingTF.transform(wordsDataDF)


    /*
      3. 定义一个 IDF 工具，计算 TF-IDF
        调用 IDF 的方法来重新构造特征向量的规模，生成的 idf 是一个 Estimator，
        在特征向量上应用它的 fit() 方法，会产生一个 IDFModel

     */
    val idf = new IDF().setInputCol("rawFeatures").setOutputCol("features")
    val idfModel = idf.fit(featurizedDataDF)

    /*
      4.同时，调用 IDFModel 的 transform 方法，可以得到每一个单词对应的 TF-IDF 度量值。

      得到增加新列 features 的 DF,该 DF 的格式是

     ( productId, name, tags, words, rawFeatures, features)

     ( 259637, 小狗钱钱, 书 少儿图书 教育类 童书 不错 孩子很喜欢
     , [书, 少儿图书, 教育类, 童书, 不错, 孩子很喜欢]
     , (800,[67,259,267,350,579,652],[1.0,1.0,1.0,1.0,1.0,1.0])
     , (800,[67,259,267,350,579,652],[0.4638371143300716,2.272125885509337,3.188416617383492,3.4760986898352733,1.8021222562636017,3.8815637979434374])
        |
        |--> 这里最后一行的最后一组代表的就是 TF-IDF 度量值，通过观察发现 3.88 是最大值，也就是哈希到 652 桶里面的词最能代表本数据
    */
    val rescaledDataDF = idfModel.transform(featurizedDataDF)
    /*
       经过 TF-IDF 提取之后，会过滤掉热门标签等因子对数据的干扰，现在的数据会更加符合用户喜好
       然后可以开始操作现在的数据了，对数据进行转换，得到 RDD 形式的 features
       二元组的形式：(productId, features)
     */
    val productFeatures = rescaledDataDF.map {
      row => (row.getAs[Int]("productId"), row.getAs[SparseVector]("features").toArray)
    }
      .rdd
      .map {
        case (productId, features) => (productId, new DoubleMatrix(features))
      }

    // 此处与 OfflineRecommender 第三步一致，利用商品的特征向量，计算商品的相似度列表
    // 两两配对商品，计算余弦相似度
    val productRecs = productFeatures.cartesian(productFeatures)
      .filter {
        case (a, b) => a._1 != b._1
      }
      // 计算余弦相似度
      .map {
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
      .option("collection", CONTENT_PRODUCT_RECS)
      .mode("overwrite")
      .format("com.mongodb.spark.sql")
      .save()

    spark.stop()
  }

  def consinSim(product1: DoubleMatrix, product2: DoubleMatrix): Double = {
    product1.dot(product2) / (product1.norm2() * product2.norm2())
  }
}
