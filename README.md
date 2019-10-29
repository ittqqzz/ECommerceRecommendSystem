# ECommerceRecommendSystem

# 前端

启动前端代码

```
npm i
npm run serve
```

# 服务端

服务端分为 Web 模块以及推荐模块

Web 模块与前端交互，接收与反馈数据

推荐模块监听 Kafka 的用户行为数据，然后进行实施计算，将结果写回 MongoDB，并周期性执行离线计算将用户最近的操作数据写入到 MongoDB

按顺序启动服务端组件

1. zookeeper
2. kafka
3. redis
4. MongoDB

然后启动 Web 模块

最后是推荐模块