# 低碳出行激励平台 - 完整项目

## 📁 项目结构

```
wxboot-project/
├── src/                    # Spring Boot后端源代码
├── target/                 # 编译输出目录
├── admin/                  # Vue3管理后台
│   ├── src/
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
├── pom.xml                 # Maven配置
├── database.sql            # 数据库脚本
└── README.md              # 本文件
```

## 🚀 快速启动

### 1️⃣ 初始化数据库

```bash
# 创建数据库并导入数据
mysql -u root -p123456 --default-character-set=utf8mb4 < database.sql
```

### 2️⃣ 启动后端服务

```bash
# 方式1：直接运行JAR
java -jar target/carbon-platform-1.0.0.jar

# 方式2：使用Maven
mvn spring-boot:run
```

后端服务运行在：`http://localhost:8080/api`

### 3️⃣ 启动管理后台

```bash
cd admin
npm install
npm run dev
```

访问地址：`http://localhost:5173`

### 4️⃣ 启动微信小程序

- 打开微信开发者工具
- 导入项目：`D:/wxboot`
- 填写AppID
- 点击预览或真机调试

## 📊 核心功能

### 用户端（微信小程序）
- ✅ 用户注册登录
- ✅ 出行记录（手动 + GPS轨迹）
- ✅ 碳积分查看
- ✅ 积分商城兑换
- ✅ 低碳活动参与
- ✅ 出行论坛互动
- ✅ 个人信息管理

### 管理端（Web）
- ✅ 仪表板统计
- ✅ 用户管理
- ✅ 出行记录审核
- ✅ 商城管理
- ✅ 活动管理
- ✅ 论坛管理
- ✅ 公告发布

### 后端API
- ✅ 用户认证（JWT）
- ✅ 出行记录管理
- ✅ 积分系统
- ✅ 商城订单
- ✅ 活动管理
- ✅ 论坛系统
- ✅ 数据统计

## 🔐 测试账号

### 用户端
- 手机号：13800138000
- 密码：123456

### 管理端
- 手机号：13800138001
- 密码：123456

## ⚙️ 配置说明

### 后端配置 (src/main/resources/application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/carbon_platform
    username: root
    password: 123456
```

### 小程序配置 (app.js)
```javascript
const BASE_URL = 'http://localhost:8080/api'
```

### 管理后台配置 (admin/vite.config.js)
```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true
  }
}
```

## 🎨 设计特点

- **主色调**：绿色 (#27ae60) - 体现低碳环保
- **设计风格**：现代简约、美观大气
- **响应式**：适配各种屏幕尺寸
- **无障碍**：符合可访问性标准

## 📱 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端 | Spring Boot | 3.2.0 |
| 数据库 | MySQL | 8.0+ |
| ORM | MyBatis Plus | 3.5.5 |
| 认证 | JWT | 0.12.3 |
| 前端 | Vue 3 | 3.3.0 |
| UI框架 | Element Plus | 2.4.0 |
| 小程序 | 微信原生 | - |

## 🔧 常见问题

### Q: 后端无法启动？
A: 检查MySQL是否运行，数据库密码是否正确（默认123456）

### Q: 小程序无法连接后端？
A: 修改app.js中的BASE_URL为你的服务器地址

### Q: 管理后台无法加载？
A: 检查Node.js版本（需要16+），运行 `npm install` 重新安装依赖

## 📦 部署指南

### 后端部署
```bash
# 打包
mvn clean package

# 运行
java -jar target/carbon-platform-1.0.0.jar
```

### 管理后台部署
```bash
# 构建
cd admin
npm run build

# 将dist目录上传到Web服务器
```

### 小程序发布
- 在微信公众平台提交审核
- 配置生产环境API地址
- 获取微信支付权限

## 📞 支持

如有问题，请检查：
1. 数据库连接
2. 端口占用情况
3. 防火墙设置
4. 依赖版本兼容性

## 📄 许可证

MIT License
