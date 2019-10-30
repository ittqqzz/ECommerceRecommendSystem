# 简介

## business

与前端交互的业务代码

技术栈：

1. Spring
2. Redis
3. MongoDB
4. Zookeeper
5. Kafka
6. Elasticsearch

## recommend

独立运行的推荐源代码，分为实时推荐以及离线推荐

技术栈：

1. Spark
2. SparkMLlib
3. Flink
4. FlinkML

## 启动

运行前需要安装的组件：

1. Zookeeper
2. Kafka
3. Redis
4. MongoDB

源码里面涉及的 ip 是我的真实阿里云地址，服务已经搭建完毕，你可以直接运行 business 模块体验。也可以自行安装这些组件，不过需要自己翻阅源码将 ip 地址替换掉

### 启动 business

在 IDEA 里面直接将 business 模块部署到 tomcat 或者使用 maven 的 tomcat 插件运行即可，最后访问 http://localhost:8088

### 启动 recommender

确保已经安装 scala，版本号 2.11 +

1. 首次运行需要加载冷数据到 MongoDB 里面

进入到 DataLoader 里面执行  DataLoader.scala

2. 然后依次执行离线计算 main 程序

StatisticsRecommender、OfflineRecommender、ItemCFRecommender、ContentRecommender

3. 最后执行实时计算 main 程序

OnlineRecommender

## 部署

分别执行 recommender 与 businessServer 的 package 命令，会对这两个项目分别打包

![image-20191030140455319](./assets/image-20191030140455319.png)

![image-20191030141303449](./assets/image-20191030141303449.png)

```
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary:
[INFO] 
[INFO] recommender ........................................ SUCCESS [  4.006 s]
[INFO] DataLoader ......................................... SUCCESS [ 28.439 s]
[INFO] StatisticsRecommender .............................. SUCCESS [ 12.756 s]
[INFO] OfflineRecommender ................................. SUCCESS [ 10.193 s]
[INFO] OnlineRecommender .................................. SUCCESS [  9.045 s]
[INFO] ContentRecommender ................................. SUCCESS [  7.880 s]
[INFO] ItemCFRecommender .................................. SUCCESS [  6.769 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 01:19 min
[INFO] Finished at: 2019-10-30T14:04:57+08:00
[INFO] Final Memory: 24M/171M
[INFO] ------------------------------------------------------------------------
```



BusinessServe.war 部署到 tomcat 等 web 容器里面

recommender 里面的各个 jar 包提交给 spark  以及 Azkaban 来调度