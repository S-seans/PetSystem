<h1 align="center" style="margin: 30px 0 30px; font-weight: bold;">动物救助站宠物领养管理系统</h1>
<h4 align="center">基于 RuoYi v3.9.0 — SpringBoot + Vue3 前后端分离</h4>

## 项目简介

基于 **RuoYi v3.9.0** 快速开发框架二次构建的 **动物救助站宠物领养管理系统**，围绕"救助 → 展示 → 领养 → 回访"流程，提供宠物档案管理、领养申请审核、健康档案维护等核心业务，并面向普通用户开放公开宠物展示、在线申请领养与 AI 智能客服入口。

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
│   │   ├── ai/                  #   AI 智能客服 (DeepSeek)
│   │   └── publicapi/           #   用户端公开接口 (匿名只读)
│   └── sql/                     # 数据库初始化脚本
│
├── frontend/                    # 前端 (Vue 3 + Element Plus)
│   ├── src/
│   │   ├── api/                 # API 接口
│   │   ├── views/               # 页面视图
│   │   │   ├── public/          #   ★ 用户端页面 (画廊/申请/我的申请等)
│   │   │   ├── pet/             #   管理端 - 宠物管理
│   │   │   ├── adoption/        #   管理端 - 领养申请审核
│   │   │   └── health/          #   管理端 - 健康档案
│   │   ├── components/          # 通用组件
│   │   │   └── AiChat/          #   AI 客服浮窗
│   │   ├── store/               # Pinia 状态管理
│   │   └── router/              # 路由配置
│   └── package.json
│
└── uploadPath/                  # 文件上传目录 (已忽略)
```

## 角色与权限

| 角色 | 能力 |
|------|------|
| 游客 | 匿名浏览公开宠物列表、宠物详情、健康记录 |
| 普通用户 | 注册登录后提交领养申请、跟踪/撤销自己的申请、维护个人资料 |
| 管理员/工作人员 | 维护宠物档案与健康记录、审核领养申请、系统后台管理 |

## 自定义业务功能

- **宠物管理**：宠物信息录入、编辑、查询、状态管理（可领养 / 已领养）；宠物图片上传至后端静态目录，支持 PNG→JPG 自动回退；公开接口仅返回"可领养"宠物
- **领养管理**：领养申请提交（填领养理由）、管理员审核（待审核 → 通过 / 拒绝）、状态跟踪；同一宠物同一时刻最多一条待审核申请（Service 校验 + 数据库唯一索引兜底）；审核通过后宠物状态自动更新为"已领养"，撤销审核则自动回退
- **健康档案**：宠物健康记录、疫苗接种、绝育信息、健康状态
- **AI 智能客服**：基于 DeepSeek 的流式问答，命中宠物关键词/品种时自动检索库内宠物数据拼入上下文回答，避免编造；按 IP 限流（Redis 优先，不可用时回退本地内存限流）；游客匿名可用

## 用户端功能

- **公开宠物画廊**：按分类浏览待领养宠物，卡片式展示 + 宠物详情弹窗
- **在线申请领养**：游客可浏览，登录后一键填写理由提交申请
- **我的领养申请**：跟踪申请审核进度，可撤销/修改待审核状态的申请
- **宠物健康记录**：公开展示宠物健康档案
- **个人资料**：用户自助维护个人资料
- **AI 智能客服**：全局悬浮窗，随时咨询领养流程与宠物信息

## 内置系统功能

基于 RuoYi 平台提供的后台管理能力（`系统管理` 菜单）：用户管理、部门管理、岗位管理、菜单管理、角色管理、字典管理、参数管理、通知公告、操作日志、登录日志、在线用户、定时任务、代码生成、系统接口 (Swagger)、服务监控、缓存监控、在线构建器、连接池监视。

## 环境配置

### 环境变量（生产环境必配）

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `TOKEN_SECRET` | 随机强密钥 | JWT 签名密钥，生产必须覆盖；修改后所有已登录用户需重新登录 |
| `DB_USERNAME` | `root` | 数据库连接账号（对应 `application-druid.yml` 中 `master.username`） |
| `DB_PASSWORD` | 无 | 数据库连接密码，默认留空，必须通过环境变量提供，仓库不保存明文密码 |
| `DRUID_USER` | `ruoyi` | Druid 监控台登录账号 |
| `DRUID_PASSWORD` | 强随机密码 | Druid 监控台登录密码，生产必须覆盖，勿使用弱口令 |

### AI 客服配置

编辑 `backend/ruoyi-admin/src/main/resources/application.yml` 中 `ai` 配置项，填入 `api-key: sk-xxx` 即启用 AI 客服（留空则功能禁用），可按需调整 `model`、单 IP 限流 `rate-limit` 等参数。

## 数据库初始化

使用单文件全量脚本一键完成（自动创建数据库 `ry_pet_adoption` 及全部表、菜单与演示数据）：

```bash
mysql -uroot -p < backend/sql/ry_full.sql
```

> 该命令为 cmd / Git Bash / Linux 语法。若在 Windows PowerShell 中执行，`<` 是保留运算符会报错，请改用：
> ```powershell
> Get-Content backend/sql/ry_full.sql | mysql -uroot -p
> ```
> 或 `cmd /c "mysql -uroot -p < backend/sql/ry_full.sql"`。

> 脚本包含 DROP + 重建表操作，仅可用于全新数据库；默认管理员账号 `admin / admin123`，并预置 7 只演示宠物。执行前请确保本地 MySQL 服务已启动，且库名与 `backend/ruoyi-admin/src/main/resources/application-druid.yml` 中 `master.url` 一致（本分支默认指向 `ry_pet_adoption`）。

### 数据库内容（主分支默认库 `ry_pet_adoption`）

`ry_full.sql` 执行后创建的 `ry_pet_adoption`（utf8mb4）包含：

- **平台基础表**：若依 `sys_*`（用户/部门/岗位/角色/菜单/字典/参数/日志/通知/任务）及 `gen_*`（代码生成）
- **定时任务表**：`QRTZ_*`
- **业务表**：`tb_pet`（宠物）、`tb_adoption_request`（领养申请，含防重复申请唯一索引与触发器）、`tb_pet_health`（健康记录）、`tb_adoption_success`（成功故事）
- **种子与演示数据**：业务菜单（2000~2045）及普通角色关联、7 只演示宠物（图片位于 `static/images/pets`）、健康记录、领养申请与成功故事示例

对应数据源见 `application-druid.yml` 的 `master.url`（默认 `jdbc:mysql://localhost:3306/ry_pet_adoption`），账号密码通过环境变量 `DB_USERNAME` / `DB_PASSWORD` 注入（见上文环境变量表），仓库不保存明文。

## 本地运行

### 后端

> 前置依赖：本地 MySQL 服务运行中（数据源见下方说明）、Redis 运行在 `localhost:6379`（登录/缓存/AI 限流依赖，未启动将无法登录）；端口 `8080` 未被占用。

```bash
# 数据库连接：application-druid.yml 中 master 默认指向库 ry_pet_adoption。
# 账号密码通过环境变量注入（仓库不含明文密码）：
#   Windows PowerShell:  $env:DB_USERNAME="root"; $env:DB_PASSWORD="你的密码"
#   Linux / macOS:       export DB_USERNAME=root DB_PASSWORD='你的密码'

# 启动后端 (Maven, JDK8+)
cd backend
mvn clean package -DskipTests
java -jar ruoyi-admin/target/ruoyi-admin.jar
# 访问 http://localhost:8080
```

### 前端

> 前置依赖：Node.js ≥ 18（Vite 6 要求）、npm；开发端口为 80（见 `vite.config.js`），启动前请确保端口未被占用。

```bash
cd frontend
npm install --registry=https://registry.npmmirror.com
npm run dev
# 访问 http://localhost:80（开发环境已配置代理转发至后端 8080）
```

默认管理员账号：`admin / admin123`

## 工程实践

- 领养审核状态流转（通过 → 宠物状态更新为已领养）已加事务保护，保证数据一致性
- AI 客服流式调用基于 WebClient 响应式实现（SSE 逐 token 转发），无手工线程
- AI 限流为 Redis 计数限流（多实例共享），Redis 不可用时自动回退本地内存限流
- 业务状态值（宠物/领养/健康）收敛为统一常量，前后端保持一致
- 业务模块含核心单元测试，并配置 GitHub Actions 在 push/PR 时自动执行后端 Maven 构建测试与前端 Vite 生产构建
