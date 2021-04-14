# ECommerceRecommendSystemIntroduction

电商网站商品推荐系统采用前后端分离开发的方式，通过 JSON 交互数据。

~~在线浏览：[liruiha.com:8080/business/]( http://liruiha.com:8080/business/ )~~

视频简介：[点击打开视频演示](https://www.bilibili.com/video/bv1M7411m7e6)

![M5npj0.png](https://s2.ax1x.com/2019/11/21/M5npj0.png)

![M5mz3n.png](https://s2.ax1x.com/2019/11/21/M5mz3n.png)



**前端**使用 Vue + TypeScript + ElementUI 构建，build 的时候自动部署到后端业务工程的 webapps/static 目录下，随 Tomcat 一同启动

[点击查看前端工程目录及详细介绍]( https://github.com/ittqqzz/ECommerceRecommendSystem/tree/master/front )

**后端**又分为业务模块和推荐模块，业务模块与前端交互、接收与反馈数据。推荐模块监听 Kafka 的用户行为数据，然后进行实时计算，将结果写回 MongoDB，并周期性执行离线计算，根据用户最近的操作记录进行离线推荐，并将推荐结果写入到 MongoDB 

[点击查看后端工程目录及详细介绍]( https://github.com/ittqqzz/ECommerceRecommendSystem/tree/master/backend )

**开发工具**

1. 环境：Win 10、JDK-1.8、Scala-2.11、Spark-2.1
2. IDE：IDEA
3. 组件：Kafka、Redis、MongoDB
4. ~~其他：Flink-1.7（计划已关闭）~~



