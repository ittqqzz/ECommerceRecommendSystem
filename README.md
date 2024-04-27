# ECommerceRecommendSystemIntroduction

想要获得作者帮助？+vx: **Yanicenini** 

作者将提供如下帮助：

【基础指导】：作者将在业余时间远程指导你来部署**（免费）**

【进阶指导】：获得作者一对一详细指导

【高级指导】：获得作者准备的镜像，支持一键部署，直接使用，省去自己配置环境的痛苦过程

【尊享指导】：获得作者手把手部署演示教学以及万字**论文模板指导&推荐算法原理分析**

***

VMWare 镜像截图

[![pkiMlbn.png](https://s21.ax1x.com/2024/04/27/pkiMlbn.png)](https://imgse.com/i/pkiMlbn)

VMWare信息：VMware® Workstation 14 Pro  版本号 14.1.1 build-7528167

VMWare部署完成之后的效果如本文件目录内  vmware_video.mp4  所示

视频简介：[点击打开视频演示](https://www.bilibili.com/video/bv1M7411m7e6)

![M5npj0.png](https://s2.ax1x.com/2019/11/21/M5npj0.png)

![M5mz3n.png](https://s2.ax1x.com/2019/11/21/M5mz3n.png)



电商网站商品推荐系统采用前后端分离开发的方式，通过 JSON 交互数据，如果你是要体验的话，直接部署http服务器即可体验，如果你想二次开发的话，后端用IDEA打开，前端用VSCode打开，前后端需要分开开发。

**前端**使用 Vue + TypeScript + ElementUI 构建，build 的时候自动部署到后端业务工程的 webapps/static 目录下，随 Tomcat 一同启动

[点击查看前端工程目录及详细介绍]( https://github.com/ittqqzz/ECommerceRecommendSystem/tree/master/front )

**后端**又分为业务模块和推荐模块，业务模块与前端交互、接收与反馈数据。推荐模块监听 Kafka 的用户行为数据，然后进行实时计算，将结果写回 MongoDB，并周期性执行离线计算，根据用户最近的操作记录进行离线推荐，并将推荐结果写入到 MongoDB 

[点击查看后端工程目录及详细介绍]( https://github.com/ittqqzz/ECommerceRecommendSystem/tree/master/backend )

**开发工具**

1. 环境：Win 10、JDK-1.8、Scala-2.11
2. IDE：IDEA
3. 组件：Kafka、Redis、MongoDB、Zookeeper



