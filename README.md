# TestProject

## 当前已完成

- Spring Boot 后端登录认证基础
- Spring Security + JWT 双令牌鉴权
- BCrypt 密码加密
- 三层角色基础：SUPER_ADMIN / ADMIN / EMPLOYEE
- Vue 3 登录页、Pinia 状态管理、路由守卫、Axios 自动刷新令牌
- Docker Compose 基础编排

## 默认数据库

项目统一使用 MySQL 数据库名：testproject。

本地直连配置已经启用 `createDatabaseIfNotExist=true`，首次启动会自动创建数据库。

## 默认账号

- superadmin / Admin@123
- admin / Admin@123
- employee / Emp@12345

## 本地启动

### 后端

```bash
cd backend
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
```

前端开发地址：

- http://localhost:5173

后端接口地址：

- http://localhost:8080/api

## 联调脚本

Windows 一键执行（造测试数据 + 校验分页/筛选/排序 + 评价权限规则）：

```bash
scripts\integration\run-seed-and-verify.cmd
```

可指定后端地址：

```bash
scripts\integration\run-seed-and-verify.cmd http://localhost:8080/api
```

## Docker 启动

```bash
docker compose up --build
```

启动后访问：

- 前端：http://localhost:5173
- 后端：http://localhost:8080/api
