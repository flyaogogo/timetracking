package com.timetracking.util;

import com.timetracking.entity.User;
import com.timetracking.entity.ProjectMember;
import com.timetracking.mapper.ProjectMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 权限工具类
 */
@Component
public class PermissionUtil {
    
    private static ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    public void setProjectMemberMapper(ProjectMemberMapper projectMemberMapper) {
        PermissionUtil.projectMemberMapper = projectMemberMapper;
    }
    
    /**
     * 获取当前登录用户ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Long) {
            return (Long) authentication.getDetails();
        }
        return null;
    }
    
    /**
     * 获取当前登录用户角色
     */
    public static User.UserRole getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            // 移除ROLE_前缀
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            try {
                return User.UserRole.valueOf(role);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * 判断当前用户是否为管理员
     */
    public static boolean isAdmin() {
        User.UserRole role = getCurrentUserRole();
        return role == User.UserRole.ADMIN;
    }
    
    /**
     * 判断当前用户是否为项目经理（角色检查 + 项目检查）
     */
    public static boolean isProjectManager() {
        User.UserRole role = getCurrentUserRole();
        // 先检查角色，如果是项目经理角色，直接返回true
        if (role == User.UserRole.PROJECT_MANAGER) {
            return true;
        }
        
        // 如果不是项目经理角色，再检查是否在任何项目中担任项目经理
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null || projectMemberMapper == null) {
            return false;
        }
        
        try {
            return projectMemberMapper.isProjectManagerInAnyProject(currentUserId) > 0;
        } catch (Exception e) {
            // 如果查询失败，返回false
            return false;
        }
    }
    
    /**
     * 判断当前用户是否为部门经理
     */
    public static boolean isDepartmentManager() {
        User.UserRole role = getCurrentUserRole();
        return role == User.UserRole.DEPARTMENT_MANAGER;
    }
    
    /**
     * 判断当前用户是否有管理权限（管理员或部门经理）
     */
    public static boolean hasManagementPermission() {
        User.UserRole role = getCurrentUserRole();
        return role == User.UserRole.ADMIN || role == User.UserRole.DEPARTMENT_MANAGER;
    }
    
    /**
     * 判断当前用户是否可以查看所有项目
     */
    public static boolean canViewAllProjects() {
        return isAdmin();
    }
    
    /**
     * 判断当前用户是否可以管理所有项目
     */
    public static boolean canManageAllProjects() {
        return isAdmin() || isProjectManager() || isDepartmentManager();
    }
    
    /**
     * 判断当前用户是否可以查看指定用户的数据
     */
    public static boolean canViewUserData(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        // 管理员可以查看所有用户数据
        if (isAdmin()) {
            return true;
        }
        
        // 用户可以查看自己的数据
        if (currentUserId.equals(targetUserId)) {
            return true;
        }
        
        // 部门经理可以查看部门内用户数据（这里简化实现）
        if (isDepartmentManager()) {
            return true;
        }
        
        return false;
    }
}