@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion
title 低碳出行激励平台 - 启动管理器

color 0A
echo.
echo  ╔═══════════════════════════════════════════════════════════╗
echo  ║                                                           ║
echo  ║           🌱 低碳出行激励平台 - 启动管理器 🌱              ║
echo  ║                                                           ║
echo  ╚═══════════════════════════════════════════════════════════╝
echo.

REM 获取当前目录
set "PROJECT_DIR=%~dp0"
cd /d "%PROJECT_DIR%"

REM ============== 环境检查 ==============
echo  [1/5] 检查运行环境...
echo.

REM 检查Java
where java >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo  [X] 错误: 未找到 Java
    echo      请安装 JDK 17 或更高版本
    echo      下载地址: https://adoptium.net/
    pause
    exit /b 1
)
for /f "tokens=3" %%a in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION=%%a
)
echo  [√] Java 已安装: !JAVA_VERSION!

REM 检查MySQL
where mysql >nul 2>&1
if %errorlevel% equ 0 (
    echo  [√] MySQL 已安装
) else (
    echo  [!] MySQL 命令未找到（请确保MySQL已添加到PATH）
)

REM 检查Node.js
where node >nul 2>&1
if %errorlevel% equ 0 (
    for /f "tokens=1" %%a in ('node -v') do set NODE_VERSION=%%a
    echo  [√] Node.js 已安装: !NODE_VERSION!
) else (
    echo  [!] Node.js 未安装（管理后台需要）
)

REM 检查后端JAR
if exist "backend\target\carbon-platform-1.0.0.jar" (
    echo  [√] 后端程序包已存在
) else (
    echo  [!] 后端程序包不存在，首次启动需要编译
)

echo.
echo  ═══════════════════════════════════════════════════════════
echo.

REM ============== 功能菜单 ==============
:menu
echo.
echo  请选择操作:
echo.
echo    [1] 一键启动所有服务
echo    [2] 仅启动后端服务
echo    [3] 仅启动管理后台
echo    [4] 编译后端项目
echo    [5] 初始化数据库
echo    [6] 安装前端依赖
echo    [7] 查看服务状态
echo    [0] 退出
echo.
set /p choice="  请输入选项 [0-7]: "

if "%choice%"=="1" goto start_all
if "%choice%"=="2" goto start_backend
if "%choice%"=="3" goto start_admin
if "%choice%"=="4" goto build_backend
if "%choice%"=="5" goto init_database
if "%choice%"=="6" goto install_deps
if "%choice%"=="7" goto check_status
if "%choice%"=="0" goto end
goto menu

REM ============== 一键启动 ==============
:start_all
echo.
echo  ═══════════════════════════════════════════════════════════
echo  [2/5] 启动所有服务
echo  ═══════════════════════════════════════════════════════════
echo.

REM 检查后端JAR
if not exist "backend\target\carbon-platform-1.0.0.jar" (
    echo  [!] 后端程序包不存在，正在编译...
    call :build_jar
)

REM 检查Redis
echo  [*] 检查 Redis 服务...
redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    color 0E
    echo  [!] 警告: Redis 服务未运行
    echo      请先启动 Redis 服务
    echo      Windows: redis-server
    echo      Linux: systemctl start redis
    echo.
    set /p continue="      是否继续启动其他服务? (y/n): "
    if /i "!continue!" neq "y" goto menu
)

REM 启动后端
echo.
echo  [*] 启动后端服务...
cd /d "%PROJECT_DIR%backend"
start "低碳出行平台 - 后端服务" cmd /k "java -jar target/carbon-platform-1.0.0.jar"
echo  [√] 后端服务已启动

REM 等待后端启动
echo  [*] 等待后端服务就绪...
timeout /t 15 /nobreak >nul

REM 检查后端是否启动成功
curl -s http://localhost:8080/api/shop/products >nul 2>&1
if %errorlevel% equ 0 (
    echo  [√] 后端服务运行正常
) else (
    echo  [!] 后端服务可能仍在启动中，请稍候检查
)

REM 启动管理后台
echo.
echo  [*] 启动管理后台...
cd /d "%PROJECT_DIR%backend\admin"

if not exist "node_modules" (
    echo  [!] 检测到首次运行，正在安装依赖...
    call npm install
)

start "低碳出行平台 - 管理后台" cmd /k "npm run dev"
echo  [√] 管理后台已启动

echo.
echo  ═══════════════════════════════════════════════════════════
echo  [√] 所有服务启动完成！
echo  ═══════════════════════════════════════════════════════════
echo.
echo  后端 API:    http://localhost:8080/api
echo  管理后台:    http://localhost:5173
echo.
echo  测试账号:
echo    用户端: 13800138000 / 123456
echo    管理端: 13800138001 / 123456
echo.
echo  小程序: 使用微信开发者工具打开 newprogram 目录
echo.
echo  提示: 关闭对应窗口即可停止服务
echo  ═══════════════════════════════════════════════════════════
pause
goto menu

REM ============== 仅启动后端 ==============
:start_backend
echo.
echo  ═══════════════════════════════════════════════════════════
echo  启动后端服务
echo  ═══════════════════════════════════════════════════════════
echo.

if not exist "backend\target\carbon-platform-1.0.0.jar" (
    echo  [!] 后端程序包不存在，正在编译...
    call :build_jar
)

cd /d "%PROJECT_DIR%backend"
echo  [*] 启动中...
java -jar target/carbon-platform-1.0.0.jar
goto menu

REM ============== 仅启动管理后台 ==============
:start_admin
echo.
echo  ═══════════════════════════════════════════════════════════
echo  启动管理后台
echo  ═══════════════════════════════════════════════════════════
echo.

cd /d "%PROJECT_DIR%backend\admin"

if not exist "node_modules" (
    echo  [!] 检测到首次运行，正在安装依赖...
    call npm install
)

echo  [*] 启动中...
npm run dev
goto menu

REM ============== 编译后端 ==============
:build_backend
echo.
echo  ═══════════════════════════════════════════════════════════
echo  编译后端项目
echo  ═══════════════════════════════════════════════════════════
echo.

where mvn >nul 2>&1
if %errorlevel% neq 0 (
    color 0C
    echo  [X] 错误: 未找到 Maven
    echo      请安装 Maven 或使用已编译的 JAR 文件
    pause
    goto menu
)

cd /d "%PROJECT_DIR%backend"
echo  [*] 开始编译...
call mvn clean package -DskipTests

if %errorlevel% equ 0 (
    echo.
    echo  [√] 编译成功！
    echo      输出: backend\target\carbon-platform-1.0.0.jar
) else (
    echo.
    echo  [X] 编译失败，请检查错误信息
)
pause
goto menu

REM ============== 初始化数据库 ==============
:init_database
echo.
echo  ═══════════════════════════════════════════════════════════
echo  初始化数据库
echo  ═══════════════════════════════════════════════════════════
echo.

if not exist "backend\database.sql" (
    echo  [X] 错误: 未找到 database.sql 文件
    pause
    goto menu
)

echo  请输入 MySQL 连接信息:
echo.
set /p mysql_host="  主机地址 (默认: localhost): "
if "%mysql_host%"=="" set mysql_host=localhost

set /p mysql_port="  端口 (默认: 3306): "
if "%mysql_port%"=="" set mysql_port=3306

set /p mysql_user="  用户名 (默认: root): "
if "%mysql_user%"=="" set mysql_user=root

set /p mysql_pass="  密码 (默认: 123456): "
if "%mysql_pass%"=="" set mysql_pass=123456

echo.
echo  [*] 正在导入数据库...

mysql -h%mysql_host% -P%mysql_port% -u%mysql_user% -p%mysql_pass% < backend\database.sql 2>nul

if %errorlevel% equ 0 (
    echo.
    echo  [√] 数据库初始化成功！
    echo.
    echo  数据库名: carbon_platform
    echo  测试账号:
    echo    用户: 13800138000 / 123456
    echo    管理员: 13800138001 / 123456
) else (
    echo.
    echo  [X] 数据库初始化失败
    echo      请检查 MySQL 连接信息和密码是否正确
)
pause
goto menu

REM ============== 安装前端依赖 ==============
:install_deps
echo.
echo  ═══════════════════════════════════════════════════════════
echo  安装前端依赖
echo  ═══════════════════════════════════════════════════════════
echo.

where npm >nul 2>&1
if %errorlevel% neq 0 (
    echo  [X] 错误: 未找到 npm
    echo      请先安装 Node.js
    pause
    goto menu
)

echo  [*] 安装管理后台依赖...
cd /d "%PROJECT_DIR%backend\admin"
call npm install
echo  [√] 管理后台依赖安装完成

echo.
echo  [*] 安装小程序依赖...
cd /d "%PROJECT_DIR%newprogram"
call npm install
echo  [√] 小程序依赖安装完成

echo.
echo  [√] 所有依赖安装完成！
pause
goto menu

REM ============== 检查服务状态 ==============
:check_status
echo.
echo  ═══════════════════════════════════════════════════════════
echo  服务状态检查
echo  ═══════════════════════════════════════════════════════════
echo.

REM 检查后端
curl -s http://localhost:8080/api/shop/products >nul 2>&1
if %errorlevel% equ 0 (
    echo  [√] 后端服务: 运行中 (http://localhost:8080/api)
) else (
    echo  [ ] 后端服务: 未运行
)

REM 检查管理后台
curl -s http://localhost:5173 >nul 2>&1
if %errorlevel% equ 0 (
    echo  [√] 管理后台: 运行中 (http://localhost:5173)
) else (
    echo  [ ] 管理后台: 未运行
)

REM 检查MySQL
mysql -uroot -p123456 -e "SELECT 1" >nul 2>&1
if %errorlevel% equ 0 (
    echo  [√] MySQL: 已连接
) else (
    echo  [ ] MySQL: 未连接
)

REM 检查Redis
redis-cli ping >nul 2>&1
if %errorlevel% equ 0 (
    echo  [√] Redis: 已连接
) else (
    echo  [ ] Redis: 未连接
)

echo.
pause
goto menu

REM ============== 编译JAR ==============
:build_jar
cd /d "%PROJECT_DIR%backend"
echo  [*] 编译后端项目...
call mvn clean package -DskipTests -q
if %errorlevel% neq 0 (
    echo  [X] 编译失败
    pause
    exit /b 1
)
echo  [√] 编译完成
cd /d "%PROJECT_DIR%"
exit /b 0

REM ============== 退出 ==============
:end
echo.
echo  ═══════════════════════════════════════════════════════════
echo  感谢使用低碳出行激励平台！
echo  ═══════════════════════════════════════════════════════════
echo.
endlocal
exit /b 0
