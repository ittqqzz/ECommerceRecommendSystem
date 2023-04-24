# 后端简介

## 一、recommend

独立运行的推荐源代码，分为实时推荐以及离线推荐，综合利用了协同过滤算法以及基于内容的推荐方法来提供混合推荐  

### 技术栈简介

1. Spark
2. Spark SQL
3. SparkMLlib
4. ~~Flink（尚未启用，2.0 版本开发即将启动）~~
5. ~~FlinkML（2.0 版本即将迁移到 Flink 上）~~
6. MongoDB：存储文本数据
7. Redis：保存用户的评分数据，为实时处理提供高速的数据传输，数据格式为每一个 userId 里面保存多个 productId:rates，即
   1. userId:123456
      1. 10010:4.5
      2. 10011:2.3
   2. userId:654321
      1. XXXX
8. 项目各个模块的原理详解，请点击[这里]( https://github.com/ittqqzz/ECommerceRecommendSystem/blob/master/backend/detail.md )

### 项目总览

离线计算均为批处理任务，使用 SparkSQL 与 SparkMLib 辅助处理

实时计算需要 SparkStreaming 辅助处理

![总览](./assets/recommendsystempng.png)

### 离线推荐部分

1. 统计服务（Spark Core + Spark SQL）
2. 推荐服务（Spark Core + Spark MLlib + ALS 算法）

### 实时推荐部分：

1. 获取消息服务（Redis + Kafka）
2. 推荐服务（Spark Streaming）

### 数据集格式

商品数据集

| **字段名**     | **字段类型** | **字段描述**  | **字段备注**     |
| -------------- | ------------ | ------------- | ---------------- |
| **productId**  | Int          | 商品的ID      |                  |
| **name**       | String       | 商品的名称    |                  |
| **categories** | String       | 商品所属类别  | 每一项用“\|”分割 |
| **imageUrl**   | String       | 商品图片的URL |                  |
| **tags**       | String       | 商品的UGC标签 | 每一项用“\|”分割 |

用户评分数据集

| **字段名**    | **字段类型** | **字段描述** |
| ------------- | ------------ | ------------ |
| **userId**    | Int          | 用户的ID     |
| **productId** | Int          | 商品的ID     |
| **score**     | Double       | 商品的分值   |
| **timestamp** | Long         | 评分的时间   |

### 算法简介

ALS：通过观察到的所有用户给产品的打分，来推断每个用户的喜好并向用户推荐适合的产品

TF-IDF：提取最能代表文本的关键词
