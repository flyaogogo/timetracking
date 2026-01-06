package com.timetracking.interceptor;

import com.timetracking.entity.User;
import com.timetracking.service.UserService;
import com.timetracking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 数据权限拦截器
 * 根据用户角色和组织关系控制数据访问权限
 */
@Component
public class DataPermissionInterceptor implements HandlerInterceptor {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 获取当前用户
        String token = getTokenFromRequest(request);
        if (token == null) {
            return true; // 让Security处理认证
        }
        
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            User currentUser = userService.findByUsername(username);
            
            if (currentUser != null) {
                // 将用户信息存储到ThreadLocal，供后续使用
                DataPermissionContext.setCurrentUser(currentUser);
                
                // 解析请求参数，设置数据权限上下文
                setDataPermissionContext(request, currentUser);
            }
            
        } catch (Exception e) {
            // Token解析失败，清除上下文
            DataPermissionContext.clear();
        }
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除ThreadLocal，避免内存泄漏
        DataPermissionContext.clear();
    }
    
    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    /**
     * 设置数据权限上下文
     */
    private void setDataPermissionContext(HttpServletRequest request, User currentUser) {
        String uri = request.getRequestURI();
        String method = request.getMethod();
        
        // 根据请求路径和用户角色设置权限范围
        DataPermissionScope scope = determinePermissionScope(uri, method, currentUser);
        DataPermissionContext.setPermissionScope(scope);
        
        // 如果是项目相关请求，检查项目权限
        if (uri.contains("/projects/") || uri.contains("/project-statistics/")) {
            String projectId = extractProjectIdFromUri(uri);
            if (projectId != null) {
                DataPermissionContext.setProjectId(Long.valueOf(projectId));
            }
        }
    }
    
    /**
     * 确定权限范围
     */
    private DataPermissionScope determinePermissionScope(String uri, String method, User currentUser) {
        String role = currentUser.getRole().name();
        
        switch (role) {
            case "ADMIN":
                return DataPermissionScope.ALL; // 系统管理员看所有数据
                
            case "PROJECT_MANAGER":
                // 项目经理看自己负责的项目数据
                if (uri.contains("/projects") || uri.contains("/tasks") || uri.contains("/time-entries")) {
                    return DataPermissionScope.MANAGED_PROJECTS;
                }
                return DataPermissionScope.ORGANIZATION;
                
            case "PRODUCT_MANAGER":
                return DataPermissionScope.ORGANIZATION; // 产品经理看组织数据
                
            case "DEVELOPER":
            case "TESTER":
                // 开发人员和测试人员
                if (uri.contains("/time-entries") && "POST".equals(method)) {
                    return DataPermissionScope.SELF; // 只能创建自己的工时
                }
                if (uri.contains("/time-entries") && "GET".equals(method)) {
                    return DataPermissionScope.TEAM; // 可以查看团队工时
                }
                return DataPermissionScope.TEAM; // 其他情况看团队数据
                
            default:
                return DataPermissionScope.SELF; // 默认只能看自己的数据
        }
    }
    
    /**
     * 从URI中提取项目ID
     */
    private String extractProjectIdFromUri(String uri) {
        // 匹配 /projects/{id} 或 /project-statistics/{id} 格式
        String[] parts = uri.split("/");
        for (int i = 0; i < parts.length - 1; i++) {
            if ("projects".equals(parts[i]) || "project-statistics".equals(parts[i])) {
                if (i + 1 < parts.length && parts[i + 1].matches("\\d+")) {
                    return parts[i + 1];
                }
            }
        }
        return null;
    }
}

/**
 * 数据权限范围枚举
 */
enum DataPermissionScope {
    ALL,              // 所有数据
    ORGANIZATION,     // 组织数据
    MANAGED_PROJECTS, // 管理的项目数据
    TEAM,             // 团队数据
    SELF              // 个人数据
}

/**
 * 数据权限上下文
 */
class DataPermissionContext {
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();
    private static final ThreadLocal<DataPermissionScope> PERMISSION_SCOPE = new ThreadLocal<>();
    private static final ThreadLocal<Long> PROJECT_ID = new ThreadLocal<>();
    
    public static void setCurrentUser(User user) {
        CURRENT_USER.set(user);
    }
    
    public static User getCurrentUser() {
        return CURRENT_USER.get();
    }
    
    public static void setPermissionScope(DataPermissionScope scope) {
        PERMISSION_SCOPE.set(scope);
    }
    
    public static DataPermissionScope getPermissionScope() {
        return PERMISSION_SCOPE.get();
    }
    
    public static void setProjectId(Long projectId) {
        PROJECT_ID.set(projectId);
    }
    
    public static Long getProjectId() {
        return PROJECT_ID.get();
    }
    
    public static void clear() {
        CURRENT_USER.remove();
        PERMISSION_SCOPE.remove();
        PROJECT_ID.remove();
    }
}