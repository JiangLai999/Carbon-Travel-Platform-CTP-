#!/bin/bash

# 低碳出行激励平台 - 启动脚本

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

clear
echo ""
echo "  ╔═══════════════════════════════════════════════════════════╗"
echo "  ║                                                           ║"
echo "  ║           🌱 低碳出行激励平台 - 启动管理器 🌱              ║"
echo "  ║                                                           ║"
echo "  ╚═══════════════════════════════════════════════════════════╝"
echo ""

# 环境检查
echo -e "  ${BLUE}[1/5] 检查运行环境...${NC}"
echo ""

# 检查Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo -e "  ${GREEN}[√] Java 已安装: $JAVA_VERSION${NC}"
else
    echo -e "  ${RED}[X] 错误: 未找到 Java${NC}"
    echo "      请安装 JDK 17 或更高版本"
    exit 1
fi

# 检查MySQL
if command -v mysql &> /dev/null; then
    echo -e "  ${GREEN}[√] MySQL 已安装${NC}"
else
    echo -e "  ${YELLOW}[!] MySQL 命令未找到${NC}"
fi

# 检查Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    echo -e "  ${GREEN}[√] Node.js 已安装: $NODE_VERSION${NC}"
else
    echo -e "  ${YELLOW}[!] Node.js 未安装（管理后台需要）${NC}"
fi

# 检查后端JAR
if [ -f "backend/target/carbon-platform-1.0.0.jar" ]; then
    echo -e "  ${GREEN}[√] 后端程序包已存在${NC}"
else
    echo -e "  ${YELLOW}[!] 后端程序包不存在，首次启动需要编译${NC}"
fi

echo ""
echo "  ═══════════════════════════════════════════════════════════"
echo ""

# 功能菜单
show_menu() {
    echo ""
    echo "  请选择操作:"
    echo ""
    echo "    [1] 一键启动所有服务"
    echo "    [2] 仅启动后端服务"
    echo "    [3] 仅启动管理后台"
    echo "    [4] 编译后端项目"
    echo "    [5] 初始化数据库"
    echo "    [6] 安装前端依赖"
    echo "    [7] 查看服务状态"
    echo "    [0] 退出"
    echo ""
    read -p "  请输入选项 [0-7]: " choice
}

# 编译JAR
build_jar() {
    cd "$PROJECT_DIR/backend"
    echo "  [*] 编译后端项目..."
    mvn clean package -DskipTests -q
    if [ $? -ne 0 ]; then
        echo -e "  ${RED}[X] 编译失败${NC}"
        exit 1
    fi
    echo -e "  ${GREEN}[√] 编译完成${NC}"
    cd "$PROJECT_DIR"
}

# 一键启动
start_all() {
    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo "  [2/5] 启动所有服务"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""

    # 检查后端JAR
    if [ ! -f "backend/target/carbon-platform-1.0.0.jar" ]; then
        echo "  [!] 后端程序包不存在，正在编译..."
        build_jar
    fi

    # 检查Redis
    echo "  [*] 检查 Redis 服务..."
    if redis-cli ping &> /dev/null; then
        echo -e "  ${GREEN}[√] Redis 服务运行中${NC}"
    else
        echo -e "  ${YELLOW}[!] Redis 服务未运行，请先启动${NC}"
        read -p "      是否继续启动其他服务? (y/n): " continue
        if [ "$continue" != "y" ]; then
            return
        fi
    fi

    # 启动后端
    echo ""
    echo "  [*] 启动后端服务..."
    cd "$PROJECT_DIR/backend"
    nohup java -jar target/carbon-platform-1.0.0.jar > app.log 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > backend.pid
    echo -e "  ${GREEN}[√] 后端服务已启动 (PID: $BACKEND_PID)${NC}"

    # 等待后端启动
    echo "  [*] 等待后端服务就绪..."
    sleep 15

    # 检查后端
    if curl -s http://localhost:8080/api/shop/products > /dev/null; then
        echo -e "  ${GREEN}[√] 后端服务运行正常${NC}"
    else
        echo -e "  ${YELLOW}[!] 后端服务可能仍在启动中${NC}"
    fi

    # 启动管理后台
    echo ""
    echo "  [*] 启动管理后台..."
    cd "$PROJECT_DIR/backend/admin"

    if [ ! -d "node_modules" ]; then
        echo "  [!] 检测到首次运行，正在安装依赖..."
        npm install
    fi

    nohup npm run dev > admin.log 2>&1 &
    ADMIN_PID=$!
    echo $ADMIN_PID > admin.pid
    echo -e "  ${GREEN}[√] 管理后台已启动 (PID: $ADMIN_PID)${NC}"

    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo -e "  ${GREEN}[√] 所有服务启动完成！${NC}"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""
    echo "  后端 API:    http://localhost:8080/api"
    echo "  管理后台:    http://localhost:5173"
    echo ""
    echo "  测试账号:"
    echo "    用户端: 13800138000 / 123456"
    echo "    管理端: 13800138001 / 123456"
    echo ""
    echo "  小程序: 使用微信开发者工具打开 newprogram 目录"
    echo ""
    echo "  停止服务: kill \$(cat backend/backend.pid) \$(cat backend/admin/admin.pid)"
    echo "  ═══════════════════════════════════════════════════════════"
}

# 仅启动后端
start_backend() {
    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo "  启动后端服务"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""

    if [ ! -f "backend/target/carbon-platform-1.0.0.jar" ]; then
        echo "  [!] 后端程序包不存在，正在编译..."
        build_jar
    fi

    cd "$PROJECT_DIR/backend"
    java -jar target/carbon-platform-1.0.0.jar
}

# 仅启动管理后台
start_admin() {
    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo "  启动管理后台"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""

    cd "$PROJECT_DIR/backend/admin"

    if [ ! -d "node_modules" ]; then
        echo "  [!] 检测到首次运行，正在安装依赖..."
        npm install
    fi

    npm run dev
}

# 编译后端
build_backend() {
    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo "  编译后端项目"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""

    if ! command -v mvn &> /dev/null; then
        echo -e "  ${RED}[X] 错误: 未找到 Maven${NC}"
        return
    fi

    cd "$PROJECT_DIR/backend"
    echo "  [*] 开始编译..."
    mvn clean package -DskipTests

    if [ $? -eq 0 ]; then
        echo ""
        echo -e "  ${GREEN}[√] 编译成功！${NC}"
        echo "      输出: backend/target/carbon-platform-1.0.0.jar"
    else
        echo ""
        echo -e "  ${RED}[X] 编译失败，请检查错误信息${NC}"
    fi
}

# 初始化数据库
init_database() {
    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo "  初始化数据库"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""

    if [ ! -f "backend/database.sql" ]; then
        echo -e "  ${RED}[X] 错误: 未找到 database.sql 文件${NC}"
        return
    fi

    read -p "  主机地址 (默认: localhost): " mysql_host
    mysql_host=${mysql_host:-localhost}

    read -p "  端口 (默认: 3306): " mysql_port
    mysql_port=${mysql_port:-3306}

    read -p "  用户名 (默认: root): " mysql_user
    mysql_user=${mysql_user:-root}

    read -sp "  密码 (默认: 123456): " mysql_pass
    mysql_pass=${mysql_pass:-123456}
    echo ""

    echo ""
    echo "  [*] 正在导入数据库..."

    mysql -h"$mysql_host" -P"$mysql_port" -u"$mysql_user" -p"$mysql_pass" < backend/database.sql 2>/dev/null

    if [ $? -eq 0 ]; then
        echo ""
        echo -e "  ${GREEN}[√] 数据库初始化成功！${NC}"
        echo ""
        echo "  数据库名: carbon_platform"
        echo "  测试账号:"
        echo "    用户: 13800138000 / 123456"
        echo "    管理员: 13800138001 / 123456"
    else
        echo ""
        echo -e "  ${RED}[X] 数据库初始化失败${NC}"
        echo "      请检查 MySQL 连接信息和密码是否正确"
    fi
}

# 安装依赖
install_deps() {
    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo "  安装前端依赖"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""

    if ! command -v npm &> /dev/null; then
        echo -e "  ${RED}[X] 错误: 未找到 npm${NC}"
        return
    fi

    echo "  [*] 安装管理后台依赖..."
    cd "$PROJECT_DIR/backend/admin"
    npm install
    echo -e "  ${GREEN}[√] 管理后台依赖安装完成${NC}"

    echo ""
    echo "  [*] 安装小程序依赖..."
    cd "$PROJECT_DIR/newprogram"
    npm install
    echo -e "  ${GREEN}[√] 小程序依赖安装完成${NC}"

    echo ""
    echo -e "  ${GREEN}[√] 所有依赖安装完成！${NC}"
}

# 检查服务状态
check_status() {
    echo ""
    echo "  ═══════════════════════════════════════════════════════════"
    echo "  服务状态检查"
    echo "  ═══════════════════════════════════════════════════════════"
    echo ""

    # 检查后端
    if curl -s http://localhost:8080/api/shop/products > /dev/null; then
        echo -e "  ${GREEN}[√] 后端服务: 运行中 (http://localhost:8080/api)${NC}"
    else
        echo "  [ ] 后端服务: 未运行"
    fi

    # 检查管理后台
    if curl -s http://localhost:5173 > /dev/null; then
        echo -e "  ${GREEN}[√] 管理后台: 运行中 (http://localhost:5173)${NC}"
    else
        echo "  [ ] 管理后台: 未运行"
    fi

    # 检查MySQL
    if mysql -uroot -p123456 -e "SELECT 1" &> /dev/null; then
        echo -e "  ${GREEN}[√] MySQL: 已连接${NC}"
    else
        echo "  [ ] MySQL: 未连接"
    fi

    # 检查Redis
    if redis-cli ping &> /dev/null; then
        echo -e "  ${GREEN}[√] Redis: 已连接${NC}"
    else
        echo "  [ ] Redis: 未连接"
    fi

    echo ""
}

# 主循环
while true; do
    show_menu
    case $choice in
        1) start_all ;;
        2) start_backend ;;
        3) start_admin ;;
        4) build_backend ;;
        5) init_database ;;
        6) install_deps ;;
        7) check_status ;;
        0) 
            echo ""
            echo "  ═══════════════════════════════════════════════════════════"
            echo "  感谢使用低碳出行激励平台！"
            echo "  ═══════════════════════════════════════════════════════════"
            echo ""
            exit 0
            ;;
        *) echo -e "  ${RED}无效选项，请重新选择${NC}" ;;
    esac
done
