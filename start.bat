@echo off
echo ========================================
echo        工时管理系统启动脚本
echo ========================================

echo 1. 检查MySQL服务状态...
sc query MySQL >nul 2>&1
if %errorlevel% neq 0 (
    echo MySQL服务未安装或服务名不正确
    echo 请确保MySQL已安装并且服务名为MySQL
    pause
    exit /b 1
)

echo 2. 启动MySQL服务...
net start MySQL >nul 2>&1
if %errorlevel% equ 0 (
    echo MySQL服务启动成功
) else (
    echo MySQL服务可能已经在运行
)

echo.
echo 3. 初始化数据库（如果需要）...
echo 如果是首次运行，请手动执行数据库初始化：
echo mysql -u root -p ^< database/create_tables.sql

echo.
echo 4. 启动后端服务...
echo 正在启动Spring Boot应用...
cd backend
start "后端服务" cmd /k "echo 启动后端服务中... && mvn spring-boot:run"
cd ..

echo.
echo 5. 等待后端服务启动...
echo 请等待30秒让后端服务完全启动...
timeout /t 30 /nobreak

echo.
echo 6. 检查前端依赖...
cd frontend
if not exist node_modules (
    echo 安装前端依赖...
    call npm install
    if %errorlevel% neq 0 (
        echo 前端依赖安装失败
        pause
        exit /b 1
    )
)

echo.
echo 7. 启动前端服务...
echo 正在启动Vue开发服务器...
start "前端服务" cmd /k "echo 启动前端服务中... && npm run dev"
cd ..

echo.
echo 8. 等待前端服务启动...
echo 请等待15秒让前端服务完全启动...
timeout /t 15 /nobreak

echo.
echo 9. 打开浏览器...
start http://localhost:5173

echo ========================================
echo 系统启动完成！
echo ========================================
echo 前端地址: http://localhost:5173
echo 后端地址: http://localhost:8080
echo 
echo 默认账号:
echo 管理员: admin / admin123
echo 项目经理: pm001 / admin123
echo 开发人员: dev001 / admin123
echo 测试人员: test001 / admin123
echo 设计师: design001 / admin123
echo ========================================
echo 
echo 🔍 调试统计分析模块步骤:
echo 1. 使用上述账号登录系统
echo 2. 点击左侧菜单"项目管理"
echo 3. 在项目列表中选择项目
echo 4. 点击操作列"操作" - "统计分析"
echo 5. 查看以下统计数据:
echo    - 项目进度 (任务完成率、工时利用率)
echo    - 成本使用率 (预算使用情况、利润率)
echo    - 工期状态 (按期/延期/提前)
echo    - 团队规模 (成员数量、活跃度)
echo    - 里程碑进度 (关键节点状态)
echo    - 成本分解 (各类成本分布)
echo ========================================
echo 
echo 💡 调试技巧:
echo - 按F12打开浏览器开发者工具查看网络请求
echo - 检查控制台是否有错误信息
echo - 后端日志在启动的命令行窗口中查看
echo - 数据库可通过MySQL客户端连接查看
echo ========================================

pause