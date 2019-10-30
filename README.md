# ECommerceRecommendSystem

# front

前端源码

```
npm i
npm run serve
npm run build
```

# backend

服务端源码

服务端分为业务模块以及推荐模块

业务模块与前端交互，接收与反馈数据

推荐模块监听 Kafka 的用户行为数据，然后进行实时计算，将结果写回 MongoDB，并周期性执行离线计算，计算用户最近的操作数据，并将推荐结果写入到 MongoDB

