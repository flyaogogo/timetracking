# 工时管理系统

一个基于Vue3 + Spring Boot的企业级工时管理系统，支持项目管理、任务分配、工时记录、审批流程和统计分析等功能。

## 项目结构
```
timetracking-system/
├── backend/                 # Java后端
│   ├── src/main/java/
│   │   ├── controller/      # 控制器层
│   │   ├── service/         # 业务逻辑层
│   │   ├── mapper/          # 数据访问层
│   │   ├── entity/          # 实体类
│   │   ├── vo/              # 视图对象
│   │   ├── config/          # 配置类
│   │   └── util/            # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml  # 应用配置
│   │   └── mapper/          # MyBatis映射文件
│   └── pom.xml             # Maven依赖配置
├── frontend/               # Vue3前端
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── components/     # 公共组件
│   │   ├── views/          # 页面组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # 状态管理
│   │   └── utils/          # 工具函数
│   ├── public/
│   └── package.json        # 前端依赖配置
└── database/              # 数据库脚本
    ├── init.sql           # 初始化脚本
    ├── create_tables.sql  # 建表脚本
    └── init_en.sql        # 英文版初始化
```

## 技术栈

### 后端技术
- **框架**: Spring Boot 2.7.x
- **数据库**: MySQL 8.0
- **ORM**: MyBatis Plus 3.5.x
- **安全**: Spring Security + JWT
- **构建工具**: Maven 3.8+
- **JDK版本**: JDK 1.8+

### 前端技术
- **框架**: Vue 3.x
- **UI组件**: Element Plus
- **HTTP客户端**: Axios
- **路由**: Vue Router 4.x
- **状态管理**: Pinia
- **构建工具**: Vite
- **包管理**: npm

## 环境要求

### 开发环境
- **JDK**: 1.8 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 16.x 或更高版本
- **npm**: 8.x 或更高版本
- **MySQL**: 8.0 或更高版本

### 推荐IDE
- **后端**: IntelliJ IDEA / Eclipse
- **前端**: VS Code / WebStorm
- **数据库**: MySQL Workbench / Navicat

## 部署方式

### 1. 数据库部署

#### 1.1 安装MySQL
```bash
# Windows (使用MySQL Installer)
下载并安装 MySQL 8.0 Community Server

# Linux (Ubuntu/Debian)
sudo apt update
sudo apt install mysql-server

# Linux (CentOS/RHEL)
sudo yum install mysql-server
```

#### 1.2 创建数据库
```sql
-- 连接MySQL
mysql -u root -p

-- 创建数据库
CREATE DATABASE timetracking CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户（可选）
CREATE USER 'timetracking'@'localhost' IDENTIFIED BY 'timetracking123';
GRANT ALL PRIVILEGES ON timetracking.* TO 'timetracking'@'localhost';
FLUSH PRIVILEGES;
```

#### 1.3 执行初始化脚本
```bash
# 方式1：命令行执行
mysql -u root -p timetracking < database/create_tables.sql

# 方式2：MySQL客户端执行
# 在MySQL Workbench或其他客户端中依次执行：
# 1. database/create_tables.sql
# 2. database/init.sql (可选，包含示例数据)
```

### 2. 后端部署

#### 2.1 配置数据库连接
编辑 `backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/timetracking?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root  # 修改为你的数据库用户名
    password: your_password  # 修改为你的数据库密码
    driver-class-name: com.mysql.cj.jdbc.Driver
```

#### 2.2 编译和运行
```bash
# 进入后端目录
cd backend

# 清理并编译
mvn clean compile

# 运行测试（可选）
mvn test

# 启动应用
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/timetracking-backend-1.0.0.jar
```

#### 2.3 验证后端启动
```bash
# 检查应用是否启动成功
curl http://localhost:8080/health

# 预期返回：{"status":"UP"}
```

### 3. 前端部署

#### 3.1 安装依赖
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 如果网络较慢，可使用淘宝镜像
npm install --registry=https://registry.npmmirror.com
```

#### 3.2 配置API地址
检查 `frontend/src/utils/request.js` 中的baseURL配置：
```javascript
const request = axios.create({
  baseURL: 'http://localhost:8080', // 确保与后端地址一致
  timeout: 10000
})
```

#### 3.3 启动开发服务器
```bash
# 开发模式启动
npm run dev

# 预期输出：
# Local:   http://localhost:5173/
# Network: http://192.168.x.x:5173/
```

#### 3.4 生产环境构建
```bash
# 构建生产版本
npm run build

# 构建完成后，dist目录包含所有静态文件
# 可以部署到Nginx、Apache等Web服务器
```

## 启动系统

### 完整启动流程

#### 1. 启动数据库服务
```bash
# Windows
net start mysql80

# Linux
sudo systemctl start mysql
# 或
sudo service mysql start
```

#### 2. 启动后端服务
```bash
cd backend
mvn spring-boot:run
```
等待看到以下日志表示启动成功：
```
Started TimeTrackingApplication in 15.234 seconds (JVM running for 16.789)
```

#### 3. 启动前端服务
```bash
cd frontend
npm run dev
```
看到以下输出表示启动成功：
```
Local:   http://localhost:5173/
Network: http://192.168.1.100:5173/
```

#### 4. 访问系统
打开浏览器访问：`http://localhost:5173`

### 快速启动脚本

#### Windows (start.bat)
```batch
@echo off
echo 启动工时管理系统...

echo 1. 启动MySQL服务...
net start mysql80

echo 2. 启动后端服务...
start cmd /k "cd /d backend && mvn spring-boot:run"

echo 3. 等待后端启动...
timeout /t 30

echo 4. 启动前端服务...
start cmd /k "cd /d frontend && npm run dev"

echo 5. 打开浏览器...
timeout /t 10
start http://localhost:5173

echo 系统启动完成！
pause
```

#### Linux/Mac (start.sh)
```bash
#!/bin/bash
echo "启动工时管理系统..."

echo "1. 启动MySQL服务..."
sudo systemctl start mysql

echo "2. 启动后端服务..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

echo "3. 等待后端启动..."
sleep 30

echo "4. 启动前端服务..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo "5. 系统启动完成！"
echo "后端PID: $BACKEND_PID"
echo "前端PID: $FRONTEND_PID"
echo "访问地址: http://localhost:5173"
```

## 默认账号
系统预置了以下测试账号：

| 角色 | 用户名 | 密码 | 权限说明 |
|------|--------|------|----------|
| 系统管理员 | admin | admin123 | 所有功能权限 |
| 项目经理 | pm001 | admin123 | 项目管理、审批权限 |
| 开发人员 | dev001 | admin123 | 基础功能权限 |
| 测试人员 | test001 | admin123 | 测试相关权限 |
| 设计师 | design001 | admin123 | 设计相关权限 |

## 调试项目管理模块中的统计分析模块

### 1. 功能概述
项目统计分析模块提供以下功能：
- **进度统计**: 任务完成率、工时利用率
- **财务统计**: 成本使用率、利润率分析
- **工期统计**: 进度状态、延期分析
- **团队统计**: 团队规模、成员活跃度
- **里程碑跟踪**: 关键节点进度
- **成本分解**: 各类成本分布

### 2. 访问路径
1. 登录系统：`http://localhost:5173`
2. 进入项目管理：点击左侧菜单"项目管理"
3. 选择项目：在项目列表中找到目标项目
4. 打开统计分析：点击操作列的"操作" → "统计分析"

### 3. 调试步骤

#### 3.1 前端调试
```bash
# 1. 打开浏览器开发者工具 (F12)
# 2. 切换到Network标签页
# 3. 执行统计分析操作，观察网络请求

# 关键API请求：
GET /project-statistics/{projectId}  # 获取项目统计数据
GET /projects                        # 获取项目列表
```

#### 3.2 后端调试
在IDE中设置断点调试：

**关键调试点：**
```java
// 1. 控制器入口
ProjectStatisticsController.getProjectStatistics()

// 2. 服务层逻辑
ProjectStatisticsService.getProjectStatistics()

// 3. 数据访问层
ProjectStatisticsMapper.getProgressStats()
ProjectStatisticsMapper.getFinancialStats()
ProjectStatisticsMapper.getScheduleStats()
ProjectStatisticsMapper.getTeamStats()
```

#### 3.3 数据库调试
```sql
-- 检查项目数据
SELECT * FROM projects WHERE id = 1;

-- 检查任务数据
SELECT * FROM tasks WHERE project_id = 1;

-- 检查工时数据
SELECT * FROM time_entries WHERE project_id = 1;

-- 检查团队成员
SELECT * FROM project_members WHERE project_id = 1;

-- 检查里程碑
SELECT * FROM project_milestones WHERE project_id = 1;

-- 检查成本数据
SELECT * FROM project_costs WHERE project_id = 1;
```

### 4. 常见问题排查

#### 4.1 统计数据为空
**问题**: 统计页面显示空数据或默认值

**排查步骤**:
```bash
# 1. 检查项目是否存在
curl http://localhost:8080/projects

# 2. 检查统计API
curl http://localhost:8080/project-statistics/1

# 3. 检查数据库数据
mysql -u root -p -e "SELECT COUNT(*) FROM timetracking.tasks WHERE project_id = 1;"
```

**解决方案**:
- 确保项目ID正确
- 检查数据库中是否有相关数据
- 验证SQL查询是否正确执行

#### 4.2 权限问题
**问题**: 提示"没有访问权限"

**解决方案**:
```javascript
// 检查用户权限
localStorage.getItem('token')  // 确保token存在
localStorage.getItem('user')   // 确认用户信息

// 重新登录获取新token
```

#### 4.3 接口调用失败
**问题**: 网络请求失败或超时

**排查步骤**:
1. 检查后端服务是否正常运行
2. 验证API地址配置是否正确
3. 查看浏览器控制台错误信息
4. 检查跨域配置

### 5. 性能优化建议

#### 5.1 数据库优化
```sql
-- 添加索引优化查询性能
CREATE INDEX idx_tasks_project_status ON tasks(project_id, status);
CREATE INDEX idx_time_entries_project_date ON time_entries(project_id, work_date);
CREATE INDEX idx_project_members_project ON project_members(project_id);
```

#### 5.2 前端优化
```javascript
// 缓存统计数据，避免重复请求
const statisticsCache = new Map()

// 使用防抖避免频繁请求
import { debounce } from 'lodash'
const debouncedRefresh = debounce(refreshData, 300)
```

### 6. 日志配置
在 `application.yml` 中配置日志级别：
```yaml
logging:
  level:
    com.timetracking: DEBUG
    org.springframework.web: DEBUG
    org.mybatis: DEBUG
```

这样可以在控制台看到详细的SQL执行日志和调试信息。

## 故障排除

### 常见启动问题

1. **端口占用**
   ```bash
   # 检查端口占用
   netstat -ano | findstr :8080  # Windows
   lsof -i :8080                 # Linux/Mac
   
   # 杀死占用进程
   taskkill /PID <PID> /F        # Windows
   kill -9 <PID>                 # Linux/Mac
   ```

2. **数据库连接失败**
   - 检查MySQL服务是否启动
   - 验证数据库用户名密码
   - 确认数据库名称正确

3. **依赖安装失败**
   ```bash
   # 清理缓存重新安装
   npm cache clean --force
   rm -rf node_modules package-lock.json
   npm install
   ```

## 系统架构

```
┌─────────────────┐    HTTP/HTTPS    ┌─────────────────┐
│   前端 (Vue3)    │ ◄──────────────► │  后端 (Spring)   │
│  Element Plus   │                  │   MyBatis Plus  │
└─────────────────┘                  └─────────────────┘
                                              │
                                              │ JDBC
                                              ▼
                                     ┌─────────────────┐
                                     │  数据库 (MySQL)  │
                                     │   工时管理数据   │
                                     └─────────────────┘
```

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。