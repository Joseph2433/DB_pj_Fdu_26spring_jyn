# 2026 Spring 数据库应用开发大作业 Lab5

## 技术栈

- Backend: Spring Boot REST API, MyBatis, MySQL
- Frontend: Vue3, Vite, Axios
- AI: DeepSeek Chat Completions API through backend only

## 分支约定

禁止直接在 `main/master` 分支编辑文件。功能开发使用：

- `feat/核心后端开发`
- `feat/核心前端开发`
- `feat/朋友圈搜索与AI助手开发`

当前扩展分支负责朋友圈关键词搜索与 DeepSeek AI 文案助手。

## 数据库初始化

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/seed.sql
```

初始化账号：

- 用户：`alice / 123456`
- 用户：`bob / 123456`
- 用户：`cathy / 123456`
- 管理员：`admin / admin123`

## 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认数据库连接：

- `MYSQL_URL=jdbc:mysql://localhost:3306/lab5_social?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai`
- `MYSQL_USER=root`
- `MYSQL_PASSWORD=`

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

Vite 会把 `/api` 代理到 `http://localhost:8080`。

## DeepSeek 配置

```bash
set DEEPSEEK_API_KEY=你的密钥
set DEEPSEEK_MODEL=deepseek-v4-flash
```

未配置 API Key 时，AI 文案助手会提示错误，但注册、登录、好友、朋友圈、评论、搜索和管理员审核仍可演示。

## 验证

```bash
cd backend
mvn test
```

```bash
cd frontend
npm test
npm run build
```
