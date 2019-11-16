# ECommerceRecommendSystemIntroduction

电商网站商品推荐系统属于前后端分离开发

**前端**使用 Vue + TypeScript + ElementUI 构建，build 之后部署到后端业务工程的 webapps/static 目录下，随 Tomcat 一同启动 [点击查看前端工程目录及详细介绍]( https://github.com/ittqqzz/ECommerceRecommendSystem/tree/master/front )

**后端**又分为业务模块和推荐模块，业务模块与前端交互、接收与反馈数据，推荐模块监听 Kafka 的用户行为数据，然后进行实时计算，将结果写回 MongoDB，并周期性执行离线计算，根据用户最近的操作记录进行离线推荐，并将推荐结果写入到 MongoDB [点击查看后盾工程目录及详细介绍]( https://github.com/ittqqzz/ECommerceRecommendSystem/tree/master/backend )





