#!/bin/bash

echo "========================================"
echo "       工时管理系统启动脚本"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}错误: $1 未安装或不在PATH中${NC}"
        exit 1
    fi
}

echo -e "${BLUE}1. 检查系统依赖...${NC}"
check_command mysql
check_command mvn
check_command npm
check_command java
echo -e "${GREEN}系统依赖检查完成${NC}"

echo -e "${BLUE}2. 启动MySQL服务...${NC}"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    sudo systemctl start mysql
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}MySQL服务启动成功${NC}"
    else
        echo -e "${YELLOW}MySQL服务可能已经在运行${NC}"
    fi
elif [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OS
    brew services start mysql
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}MySQL服务启动成功${NC}"
    else
        echo -e "${YELLOW}MySQL服务可能已经在运行${NC}"
    fi
fi

echo -e "${BLUE}3. 初始化数据库（如果需要）...${NC}"
echo "如果是首次运行，请手动执行数据库初始化："
echo "mysql -u root -p < database/create_tables.sql"

echo -e "${BLUE}4. 启动后端服务...${NC}"
echo "正在启动Spring Boot应用..."
cd backend
gnome-terminal --title="后端服务" -- bash -c "echo '启动后端服务中...'; mvn spring-boot:run; exec bash" 2>/dev/null || \
xterm -title "后端服务" -e "echo '启动后端服务中...'; mvn spring-boot:run; bash" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)'; echo \"启动后端服务中...\"; mvn spring-boot:run"' 2>/dev/null || \
(echo "无法打开新终端窗口，在后台启动后端服务..."; mvn spring-boot:run &)
cd ..

echo -e "${BLUE}5. 等待后端服务启动...${NC}"
echo "请等待30秒让后端服务完全启动..."
sleep 30

echo -e "${BLUE}6. 检查前端依赖...${NC}"
cd frontend
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo -e "${RED}前端依赖安装失败${NC}"
        exit 1
    fi
fi

echo -e "${BLUE}7. 启动前端服务...${NC}"
echo "正在启动Vue开发服务器..."
gnome-terminal --title="前端服务" -- bash -c "echo '启动前端服务中...'; npm run dev; exec bash" 2>/dev/null || \
xterm -title "前端服务" -e "echo '启动前端服务中...'; npm run dev; bash" 2>/dev/null || \
osascript -e 'tell app "Terminal" to do script "cd '$(pwd)'; echo \"启动前端服务中...\"; npm run dev"' 2>/dev/null || \
(echo "无法打开新终端窗口，在后台启动前端服务..."; npm run dev &)
cd ..

echo -e "${BLUE}8. 等待前端服务启动...${NC}"
echo "请等待15秒让前端服务完全启动..."
sleep 15

echo -e "${BLUE}9. 打开浏览器...${NC}"
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    xdg-open http://localhost:5173 2>/dev/null
elif [[ "$OSTYPE" == "darwin"* ]]; then
    open http://localhost:5173 2>/dev/null
fi

echo "========================================"
echo -e "${GREEN}系统启动完成！${NC}"
echo "========================================"
echo "前端地址: http://localhost:5173"
echo "后端地址: http://localhost:8080"
echo ""
echo "默认账号:"
echo "管理员: admin / admin123"
echo "项目经理: pm001 / admin123"
echo "开发人员: dev001 / admin123"
echo "测试人员: test001 / admin123"
echo "设计师: design001 / admin123"
echo "========================================"
echo ""
echo -e "${YELLOW}🔍 调试统计分析模块步骤:${NC}"
echo "1. 使用上述账号登录系统"
echo "2. 点击左侧菜单\"项目管理\""
echo "3. 在项目列表中选择项目"
echo "4. 点击操作列\"操作\" - \"统计分析\""
echo "5. 查看以下统计数据:"
echo "   - 项目进度 (任务完成率、工时利用率)"
echo "   - 成本使用率 (预算使用情况、利润率)"
echo "   - 工期状态 (按期/延期/提前)"
echo "   - 团队规模 (成员数量、活跃度)"
echo "   - 里程碑进度 (关键节点状态)"
echo "   - 成本分解 (各类成本分布)"
echo "========================================"
echo ""
echo -e "${YELLOW}💡 调试技巧:${NC}"
echo "- 按F12打开浏览器开发者工具查看网络请求"
echo "- 检查控制台是否有错误信息"
echo "- 后端日志在启动的终端窗口中查看"
echo "- 数据库可通过MySQL客户端连接查看"
echo "========================================"

echo "按任意键退出..."
read -n 1