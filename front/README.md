# 简介

## 技术栈

1. Vue （version 3.6.3）
2. TypeScript
3. ElementUI
4. Axios
5. Vue-Cli 3

如何搭建 Vue + TS 的工程，我这里已有介绍： https://zhuanlan.zhihu.com/p/88816466 

## 启动 dev 模式

```
npm i
npm run serve
```

## 编译部署到后端

```
npm run build
```

通过使用 `del-cli` 以及 `cp-cli` 工具，在 `build` 之前，Vue 会**自动**将之前的 `dist` 目录以及后端 `business `模块里面的 `static` 目录清空，然后开始 `build`，`build` 结束后会将最新的产出文件复制到 `business` 模块里面的 `static` 目录里面