<p align="center">
	<img alt="logo" src="https://oscimg.oschina.net/oscnet/up-d3d0a9303e11d522a06cd263f3079027715.png">
</p>
<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">动物救助站宠物领养管理系统</h1>
<h4 align="center">基于 RuoYi v3.9.0 — SpringBoot + Vue3 前后端分离</h4>
<p align="center">
	<a href="https://gitee.com/y_project/RuoYi-Vue/stargazers"><img src="https://gitee.com/y_project/RuoYi-Vue/badge/star.svg?theme=dark"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue"><img src="https://img.shields.io/badge/RuoYi-v3.9.0-brightgreen.svg"></a>
	<a href="https://gitee.com/y_project/RuoYi-Vue/blob/master/LICENSE"><img src="https://img.shields.io/github/license/mashape/apistatus.svg"></a>
</p>

## 项目简介

基于 **RuoYi** 快速开发框架构建的 **动物救助站宠物领养管理系统**，实现了宠物管理、领养申请、健康档案、成功故事等核心业务功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端语言 | Java 8 / 17 |
| 后端框架 | Spring Boot 2.5.15, Spring Security, MyBatis |
| 数据库 | MySQL + Druid 连接池 + Redis 缓存 |
| 前端框架 | Vue 3 (Composition API) + Element Plus 2.10 |
| 前端工具 | Vite 6, Pinia, Vue Router 4, Axios |
| 任务调度 | Quartz |
| 代码生成 | Velocity 模板引擎 |

## 项目结构

```
D:\RuoYi
├── backend/                     # 后端 (Spring Boot 多模块 Maven)
│   ├── ruoyi-admin              # Web 入口 (Controller, 配置)
│   ├── ruoyi-common             # 公共工具 (注解, 常量, 工具类)
│   ├── ruoyi-framework          # 核心框架 (Security, AOP, 拦截器)
│   ├── ruoyi-system             # 系统业务 (用户/角色/菜单/部门)
│   ├── ruoyi-quartz             # 定时任务 (Quartz)
│   ├── ruoyi-generator          # 代码生成器
│   ├── ruoyi-manage2            # ★ 自定义业务模块
│   │   ├── pet/                 #   宠物管理
│   │   ├── adoption/            #   领养管理
│   │   ├── health/              #   健康档案
│   │   └── success/             #   成功故事
│   └── sql/                     # 数据库初始化脚本
│
├── frontend/                    # 前端 (Vue 3 + Element Plus)
│   ├── src/
│   │   ├── api/                 # API 接口
│   │   ├── views/               # 页面视图
│   │   ├── components/          # 通用组件
│   │   ├── store/               # Pinia 状态管理
│   │   └── router/              # 路由配置
│   └── package.json
│
└── uploadPath/                  # 文件上传目录 (已忽略)
```

## 内置功能

1. 用户管理：系统用户配置
2. 部门管理：组织机构树结构展现
3. 岗位管理：用户所属职务
4. 菜单管理：菜单与权限标识
5. 角色管理：角色菜单权限分配
6. 字典管理：固定数据维护
7. 参数管理：系统动态参数
8. 通知公告：信息发布维护
9. 操作日志：操作日志记录查询
10. 登录日志：登录日志记录查询
11. 在线用户：活跃用户监控
12. 定时任务：任务调度与执行日志
13. 代码生成：前后端代码一键生成
14. 系统接口：Swagger API 文档
15. 服务监控：CPU/内存/磁盘监控
16. 缓存监控：缓存信息查询
17. 在线构建器：表单拖拽生成
18. 连接池监视：Druid SQL 分析

## 自定义业务功能

- **宠物管理**：宠物信息录入、编辑、查询、状态管理
- **领养管理**：领养申请提交、审核、状态跟踪
- **健康档案**：宠物健康记录、疫苗接种、体检信息
- **成功故事**：领养成功案例分享展示

## 本地运行

### 后端

```bash
# 导入数据库
后端/sql/ry_20250522.sql
后端/sql/quartz.sql

# 启动后端 (Maven)
cd backend
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
```

### 前端

```bash
cd frontend
yarn --registry=https://registry.npmmirror.com
yarn dev
# 访问 http://localhost:80
```

## 在线体验

演示地址：http://vue.ruoyi.vip  
文档地址：http://doc.ruoyi.vip  
账号/密码：admin / admin123

## 演示图

<table>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/cd1f90be5f2684f4560c9519c0f2a232ee8.jpg"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/1cbcf0e6f257c7d3a063c0e3f2ff989e4b3.jpg"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-8074972883b5ba0622e13246738ebba237a.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-9f88719cdfca9af2e58b352a20e23d43b12.png"/></td>
    </tr>
    <tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-39bf2584ec3a529b0d5a3b70d15c9b37646.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-936ec82d1f4872e1bc980927654b6007307.png"/></td>
    </tr>
	<tr>
        <td><img src="https://oscimg.oschina.net/oscnet/up-b2d62ceb95d2dd9b3fbe157bb70d26001e9.png"/></td>
        <td><img src="https://oscimg.oschina.net/oscnet/up-d67451d308b7a79ad6819723396f7c3d77a.png"/></td>
    </tr>
</table>

## 开源协议

本项目基于 MIT 协议开源。
