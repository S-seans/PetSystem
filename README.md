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

## 安全加固与升级说明（refactor/hardening）

本分支对系统做了以下加固，部署/升级时请注意：

### 环境变量（生产环境必配）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `TOKEN_SECRET` | 随机强密钥 | JWT 签名密钥，生产必须覆盖；修改后所有已登录用户需重新登录 |
| `DRUID_USER` | `ruoyi` | Druid 监控台登录账号 |
| `DRUID_PASSWORD` | 强随机密码 | Druid 监控台登录密码，生产必须覆盖，勿使用弱口令 |
| `AI_API_KEY` | 空 | DeepSeek API Key（填入即启用 AI 客服） |

### 数据库升级（已初始化过的库需手动执行一次）

```bash
# 为领养申请表增加"同一宠物同一时刻最多一条待审核申请"的数据库级兜底
source sql/upgrade_hardening.sql
```

全新安装直接使用 `sql/ry_business.sql`（已包含上述加固）。

### 其他变更

- 领养审核流转（通过→生成成功故事→更新宠物状态）已加事务保护，保证数据一致性
- AI 客服流式调用由手工线程 + HttpURLConnection 改为 WebClient 响应式实现
- AI 限流升级为 Redis 计数限流（多实例共享），Redis 不可用时自动回退本地内存限流
- 业务状态值（宠物/领养/健康/成功故事）收敛为统一常量，前后端保持一致
- 成功故事列表增加宠物名称、领养人账号关联查询
- 新增核心业务单元测试与 GitHub Actions CI 构建校验

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
