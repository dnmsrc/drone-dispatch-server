# drone-dispatch-server（服务端工程根目录）

> 本目录是**独立 Git 仓库**（仓库名即目录名 `drone-dispatch-server`），提交记录与服务端相关，
> 与顶层文档仓、`drone-dispatch-web`、`drone-dispatch-h5` 互不混杂。

本目录放置**所有服务端工程**（后端 API，以及将来可能的定时任务、支付回调网关等）。前端在仓库根目录的
`drone-dispatch-web/`（管理后台）与 `drone-dispatch-h5/`（用户端 H5 / 小程序）。

## 当前状态

**技术栈已定**：Java 21（Microsoft OpenJDK）+ Maven 3.9.11 + Spring Boot + MySQL 8.4.10（本机环境见 `D:\environment\README.md`）。

**进度基准**：`docs/roadmap.md`（七阶段路线图 + 当前阶段 + 进度日志，每次推进后更新）。
当前处于**阶段 0 · 链路跑通**（环境 → 编译 → 启动 → 连库 → 返回 JSON）。

## 规划中的服务端职责（待定）

- 用户端 API：微信登录、计价配置、档期查询、下单、支付、退款试算/申请、评价
- 管理端 API：登录鉴权、订单流转（接单/拒单/开始/完成）、退款审批、档期与配置、场地字典
- 数据存储：库表结构、迁移、种子数据（替代原型里的 `mock-server/data/db.json`）
- 第三方集成：微信支付 / 微信登录 / 高德地图 / OSS —— 原型阶段均为模拟，正式版在此接入

## 与原型的关系

`drone-dispatch-prototype/mock-server/server.js` 是当前唯一的 API 实现（61 个路由，JSON 文件持久化），
接口契约见 `drone-dispatch-prototype/docs/PRD.md` §11 与 `drone-dispatch-prototype/docs/consult-workflow.md`。
服务端第一步就是**把这套接口契约接过来**，保证 `drone-dispatch-web/`、`drone-dispatch-h5/` 指向真实服务端后行为不变。
