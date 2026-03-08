---
name: "timetracking-system"
description: "工时管理系统技术包，包含系统架构、技术栈、功能模块和开发指南。当用户需要了解系统架构、技术栈或开发相关信息时调用。"
---

# 工时管理系统技术包

## 系统架构

### 前后端分离架构
- **前端**：Vue 3 + Vite + Element Plus
- **后端**：Spring Boot + MyBatis-Plus + MySQL
- **认证**：JWT Token
- **API**：RESTful API

## 技术栈

### 前端技术
- **框架**：Vue 3 (Composition API)
- **构建工具**：Vite
- **状态管理**：Pinia
- **UI组件库**：Element Plus
- **路由**：Vue Router
- **HTTP客户端**：Axios
- **代码风格**：ESLint + Prettier

### 后端技术
- **框架**：Spring Boot 2.7.x
- **ORM**：MyBatis-Plus
- **数据库**：MySQL 8.0+
- **认证**：JWT
- **安全**：Spring Security
- **构建工具**：Maven

## 核心功能模块

### 1. 认证与用户管理
- **用户登录/注册**
- **JWT Token管理**
- **用户信息管理**
- **角色权限控制**

### 2. 项目管理
- **项目创建与编辑**
- **项目成员管理**
- **项目状态管理**
- **项目里程碑管理**
- **项目成本管理**

### 3. 任务管理
- **任务创建与分配**
- **任务状态跟踪**
- **任务优先级管理**
- **任务依赖关系**

### 4. 工时管理
- **工时记录**
- **工时审核**
- **工时统计分析**
- **个人工时统计**

### 5. 项目经理工作台
- **项目概览**
- **团队绩效分析**
- **工时审批**
- **项目报表生成**

## 目录结构

### 前端目录
```
frontend/
├── src/
│   ├── api/          # API调用模块
│   ├── components/   # 组件
│   ├── config/       # 配置文件
│   ├── layout/       # 布局
│   ├── router/       # 路由
│   ├── stores/       # 状态管理
│   ├── utils/        # 工具函数
│   ├── views/        # 页面
│   ├── App.vue       # 根组件
│   └── main.js       # 入口文件
├── index.html        # HTML模板
├── package.json      # 依赖管理
└── vite.config.js    # Vite配置
```

### 后端目录
```
backend/
├── src/main/
│   ├── java/com/timetracking/
│   │   ├── config/        # 配置
│   │   ├── controller/     # 控制器
│   │   ├── entity/         # 实体类
│   │   ├── exception/      # 异常处理
│   │   ├── filter/         # 过滤器
│   │   ├── interceptor/    # 拦截器
│   │   ├── mapper/         # MyBatis映射
│   │   ├── service/        # 业务逻辑
│   │   ├── util/           # 工具类
│   │   ├── vo/             # 数据传输对象
│   │   └── TimeTrackingApplication.java # 应用入口
│   └── resources/
│       ├── mapper/         # XML映射文件
│       └── application.yml # 配置文件
└── pom.xml               # Maven配置
```

## 开发指南

### 前端开发
1. **安装依赖**：`npm install`
2. **启动开发服务器**：`npm run dev`
3. **构建生产版本**：`npm run build`
4. **代码风格检查**：`npm run lint`

### 后端开发
1. **安装依赖**：`mvn install`
2. **启动开发服务器**：`mvn spring-boot:run`
3. **构建生产版本**：`mvn package`
4. **运行测试**：`mvn test`

## 数据库
- **数据库名称**：timetracking
- **初始化脚本**：`database/timetracking.sql`
- **项目状态历史脚本**：`database/project_status_history.sql`

## 权限管理

### 角色定义
- **ADMIN**：系统管理员
- **PROJECT_MANAGER**：项目经理
- **PRODUCT_MANAGER**：产品经理
- **DEPARTMENT_MANAGER**：部门经理
- **DEVELOPER**：开发人员
- **TESTER**：测试人员

### 权限控制
- **菜单权限**：基于角色的菜单访问控制
- **API权限**：基于JWT Token的API访问控制
- **项目级权限**：基于项目成员角色的权限控制

## 系统部署

### 前端部署
1. 构建生产版本：`npm run build`
2. 将`dist`目录部署到Web服务器（如Nginx）

### 后端部署
1. 构建jar包：`mvn package`
2. 运行：`java -jar timetracking-backend-1.0.0.jar`

### 环境变量
- **前端**：通过`.env`文件配置
- **后端**：通过`application.yml`或环境变量配置

## 常见问题

### 登录问题
- 检查用户名密码是否正确
- 检查验证码是否正确
- 检查网络连接

### 权限问题
- 检查用户角色是否正确
- 检查项目成员权限设置
- 检查API访问权限

### 性能问题
- 优化SQL查询
- 缓存频繁访问的数据
- 优化前端渲染

## 技术支持

### 代码结构说明
- **前端**：使用Vue 3 Composition API，组件化开发
- **后端**：使用Spring Boot分层架构，Controller→Service→Mapper
- **数据库**：使用MyBatis-Plus简化数据库操作

### 开发规范
- **前端**：ESLint + Prettier代码规范
- **后端**：Java代码规范，使用@Autowired注入
- **数据库**：使用驼峰命名法，主键使用自增ID

## 版本信息

- **系统版本**：1.0.0
- **前端依赖**：
  - Vue 3.2+
  - Element Plus 2.3+
  - Pinia 2.0+
  - Vue Router 4.0+
  - Axios 1.0+

- **后端依赖**：
  - Spring Boot 2.7.18
  - MyBatis-Plus 3.5.3.1
  - MySQL Connector 8.0.33
  - JWT 0.11.2
  - Spring Security 5.7.10

## 未来规划

- **功能扩展**：添加更多报表和分析功能
- **性能优化**：优化系统性能和响应速度
- **安全增强**：加强系统安全防护
- **用户体验**：改进用户界面和交互体验
- **集成能力**：增加与其他系统的集成能力
