# 低碳出行激励平台

[![MIT License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.3-brightgreen.svg)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-6.0-red.svg)](https://redis.io/)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-blue.svg)](https://github.com)

一个完整的低碳出行激励系统，包含微信小程序用户端和Web管理后台。

## 项目结构

```
carbon-travel-platform/
├── backend/                      # 后端服务
│   ├── src/                      # Spring Boot 源代码
│   │   └── main/java/com/carbon/platform/
│   │       ├── controller/       # 控制器层
│   │       ├── service/          # 业务逻辑层
│   │       ├── mapper/           # 数据访问层
│   │       ├── entity/           # 实体类
│   │       ├── dto/              # 数据传输对象
│   │       └── config/           # 配置类
│   ├── admin/                    # Vue3 管理后台
│   │   ├── src/                  # 前端源码
│   │   ├── package.json
│   │   └── vite.config.js
│   ├── uploads/                  # 图片存储目录
│   │   ├── avatars/              # 用户头像
│   │   ├── products/             # 商品图片
│   │   └── activities/           # 活动图片
│   ├── database.sql              # 数据库脚本
│   └── pom.xml                   # Maven 配置
│
├── newprogram/                   # 微信小程序
│   ├── pages/                    # 页面
│   ├── components/               # 组件
│   ├── app.js                    # 入口文件
│   ├── app.json                  # 小程序配置
│   └── config.js                 # API 配置
│
├── start.bat                     # Windows 一键启动
├── start.sh                      # Linux/Mac 一键启动
└── README.md
```

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端 | Spring Boot | 3.2.0 |
| ORM | MyBatis Plus | 3.5.5 |
| 数据库 | MySQL | 8.0+ |
| 缓存 | Redis | - |
| 认证 | JWT | 0.12.3 |
| 管理前端 | Vue 3 + Element Plus | 3.3 / 2.4 |
| 构建工具 | Vite | 5.0 |
| 小程序 | 微信原生 + TDesign | - |

## 关键依赖版本

| 组件 | 版本 |
|------|------|
| Spring Boot | 3.2.0 |
| MyBatis Plus | 3.5.5 |
| JWT | 0.12.3 |
| Vue | 3.3+ |
| Element Plus | 2.4.0 |
| Vite | 5.0 |
| Pinia | 2.1.0 |
| Axios | 1.6.0 |
| ECharts | 5.4.0 |
| TDesign Miniprogram | 1.13.0 |

## 核心功能

### 用户端（微信小程序）

- 用户注册/登录（手机号+验证码）
- GPS轨迹追踪与距离计算
- 碳减排量自动统计
- 积分获取与商城兑换
- 活动参与与凭证上传
- 社区论坛互动
- 个人中心管理

### 管理端（Web后台）

- 数据统计仪表盘
- 用户管理
- 出行记录审核
- 商品与订单管理
- 活动发布与管理
- 论坛内容管理
- 系统公告发布

## 快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Redis
- Node.js 16+（管理后台）
- 微信开发者工具（小程序）

### 一键启动（Windows）

双击 `start.bat` 或在项目根目录执行：

```bash
start.bat
```

### 手动启动

#### 1. 初始化数据库

```bash
mysql -uroot -p123456 < backend/database.sql
```

#### 2. 启动后端

```bash
cd backend

# 方式一：Maven运行
mvn spring-boot:run

# 方式二：打包运行
mvn clean package -DskipTests
java -jar target/carbon-platform-1.0.0.jar
```

后端地址：http://localhost:8080/api

#### 3. 启动管理后台

```bash
cd backend/admin
npm install
npm run dev
```

管理后台地址：http://localhost:5173

#### 4. 启动小程序

1. 打开微信开发者工具
2. 导入项目：选择 `newprogram` 目录
3. 填写 AppID 或使用测试号
4. 点击编译预览

## 配置说明

### 后端配置

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/carbon_platform
    username: root
    password: 123456
  redis:
    host: localhost
    port: 6379

server:
  port: 8080
  servlet:
    context-path: /api

jwt:
  secret: your-secret-key
  expiration: 86400000

upload:
  path: uploads        # 图片存储路径
  max-size: 5242880    # 最大5MB
```

### 小程序配置

编辑 `newprogram/config.js`：

```javascript
const API_BASE = 'http://localhost:8080/api';
```

### 管理后台配置

编辑 `backend/admin/vite.config.js`：

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 测试账号

| 角色 | 手机号 | 密码 |
|------|--------|------|
| 用户 | 13800138000 | 123456 |
| 管理员 | 13800138001 | 123456 |

## 图片存储

项目支持本地图片存储：

- 用户上传的图片存储在 `backend/uploads/yyyy/MM/dd/` 目录
- 默认图片（头像、商品、活动）存储在对应子目录
- 访问路径：`/api/uploads/xxx.jpg`

## API 接口

### 认证模块

| 接口 | 方法 | 说明 |
|------|------|------|
| /auth/send-code | POST | 发送验证码 |
| /auth/login | POST | 登录 |
| /auth/register | POST | 注册 |

### 用户模块

| 接口 | 方法 | 说明 |
|------|------|------|
| /user/profile | GET/PUT | 个人信息 |
| /user/stats | GET | 统计数据 |
| /user/points | GET | 积分信息 |

### 出行模块

| 接口 | 方法 | 说明 |
|------|------|------|
| /travel/record | POST | 提交出行记录 |
| /travel/records | GET | 出行记录列表 |
| /travel/stats | GET | 出行统计 |

### 商城模块

| 接口 | 方法 | 说明 |
|------|------|------|
| /shop/products | GET | 商品列表 |
| /shop/exchange | POST | 积分兑换 |
| /shop/orders | GET | 订单列表 |

### 上传模块

| 接口 | 方法 | 说明 |
|------|------|------|
| /upload/image | POST | 单图上传 |
| /upload/images | POST | 多图上传 |

## 部署说明

### 打包部署

```bash
# 后端打包
cd backend
mvn clean package -DskipTests

# 管理后台打包
cd backend/admin
npm run build

# 部署文件
backend/target/carbon-platform-1.0.0.jar
backend/admin/dist/
backend/uploads/
```

### 注意事项

1. 确保 MySQL 和 Redis 已启动
2. 首次部署需执行 `database.sql` 初始化数据库
3. 图片目录 `uploads/` 需要保留
4. 生产环境请修改 JWT 密钥和数据库密码

## 常见问题

**Q: 启动报数据库连接失败？**
A: 检查 MySQL 是否启动，确认 `application.yml` 中的数据库配置正确。

**Q: 管理后台登录失败？**
A: 确认后端服务已启动，检查浏览器控制台网络请求。

**Q: 小程序请求失败？**
A: 检查 `config.js` 中的 API 地址，确保后端服务可访问。

**Q: 图片上传失败？**
A: 检查 `uploads/` 目录是否存在且有写入权限。

## 贡献指南

欢迎提交 Pull Request 或创建 Issue。

1. Fork 本仓库
2. 创建分支 (`git checkout -b feature/新功能`)
3. 提交更改 (`git commit -m '添加新功能'`)
4. 推送到分支 (`git push origin feature/新功能`)
5. 创建 Pull Request

## 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件
