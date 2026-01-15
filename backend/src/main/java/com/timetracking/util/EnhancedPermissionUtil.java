package com.timetracking.util;

import com.timetracking.entity.User;
import com.timetracking.entity.ProjectMember;
import com.timetracking.mapper.ProjectMemberMapper;
import com.timetracking.mapper.ProjectRolePermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Collections;

/**
 * 增强权限工具类 - 支持项目级权限控制
 */
@Component
public class EnhancedPermissionUtil {
    
    private static ProjectMemberMapper projectMemberMapper;
    private static ProjectRolePermissionMapper permissionMapper;
    
    @Autowired
    public void setProjectMemberMapper(ProjectMemberMapper projectMemberMapper) {
        EnhancedPermissionUtil.projectMemberMapper = projectMemberMapper;
    }
    
    @Autowired
    public void setPermissionMapper(ProjectRolePermissionMapper permissionMapper) {
        EnhancedPermissionUtil.permissionMapper = permissionMapper;
    }
    
    /**
     * 权限类型枚举
     */
    public enum ProjectPermissionType {
        PROJECT_MANAGEMENT("项目管理"),
        TASK_MANAGEMENT("任务管理"),
        TIMESHEET_APPROVAL("工时审核"),
        REPORT_VIEW("报表查看"),
        COST_MANAGEMENT("成本管理"),
        MILESTONE_MANAGEMENT("里程碑管理"),
        TEAM_MANAGEMENT("团队管理");
        
        private final String description;
        
        ProjectPermissionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 检查用户在指定项目中是否有特定权限
     */
    public static boolean hasProjectPermission(Long userId, Long projectId, ProjectPermissionType permission) {
        if (userId == null || projectId == null || permission == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内特定权限
        return checkProjectSpecificPermission(userId, projectId, permission);
    }
    
    /**
     * 检查用户是否为项目经理（全局角色或项目内角色）
     */
    public static boolean isProjectManager(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内项目经理权限
        try {
            ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
            return member != null && Boolean.TRUE.equals(member.getIsProjectManager()) && isPermissionActive(member);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查用户是否为技术负责人
     */
    public static boolean isTechLeader(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内技术负责人权限
        try {
            ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
            return member != null && Boolean.TRUE.equals(member.getIsTechLeader()) && isPermissionActive(member);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查用户是否可以审核工时
     */
    public static boolean canApproveTimesheet(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内权限
        return hasProjectPermission(userId, projectId, ProjectPermissionType.TIMESHEET_APPROVAL) ||
               isProjectManager(userId, projectId);
    }
    
    /**
     * 检查用户是否可以管理任务
     */
    public static boolean canManageTasks(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内权限
        return hasProjectPermission(userId, projectId, ProjectPermissionType.TASK_MANAGEMENT) ||
               isProjectManager(userId, projectId) ||
               isTechLeader(userId, projectId);
    }
    
    /**
     * 检查用户是否可以查看报表
     */
    public static boolean canViewReports(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内权限
        return hasProjectPermission(userId, projectId, ProjectPermissionType.REPORT_VIEW) ||
               isProjectManager(userId, projectId) ||
               isTechLeader(userId, projectId);
    }
    
    /**
     * 检查用户是否可以管理成本
     */
    public static boolean canManageCosts(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内权限
        return hasProjectPermission(userId, projectId, ProjectPermissionType.COST_MANAGEMENT) ||
               isProjectManager(userId, projectId);
    }
    
    /**
     * 检查用户是否可以管理里程碑
     */
    public static boolean canManageMilestones(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内权限
        return hasProjectPermission(userId, projectId, ProjectPermissionType.MILESTONE_MANAGEMENT) ||
               isProjectManager(userId, projectId) ||
               isTechLeader(userId, projectId);
    }
    
    /**
     * 检查用户是否可以管理团队
     */
    public static boolean canManageTeam(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 2. 检查项目内权限
        return hasProjectPermission(userId, projectId, ProjectPermissionType.TEAM_MANAGEMENT) ||
               isProjectManager(userId, projectId);
    }
    
    /**
     * 检查用户是否可以授予权限
     */
    public static boolean canGrantPermission(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        return PermissionUtil.isAdmin() || isProjectManager(userId, projectId);
    }
    
    /**
     * 获取用户在项目中的所有权限
     */
    public static List<ProjectPermissionType> getUserProjectPermissions(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return Collections.emptyList();
        }
        
        try {
            return permissionMapper.selectUserProjectPermissions(userId, projectId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户在项目中的角色描述
     */
    public static String getUserProjectRoleDescription(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return "无权限";
        }
        
        StringBuilder roles = new StringBuilder();
        
        if (PermissionUtil.isAdmin()) {
            return "系统管理员";
        }
        
        if (isProjectManager(userId, projectId)) {
            roles.append("项目经理");
        }
        
        if (isTechLeader(userId, projectId)) {
            if (roles.length() > 0) roles.append("、");
            roles.append("技术负责人");
        }
        
        if (roles.length() == 0) {
            try {
                ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
                if (member != null) {
                    roles.append("项目成员");
                } else {
                    roles.append("无权限");
                }
            } catch (Exception e) {
                roles.append("无权限");
            }
        }
        
        return roles.toString();
    }
    
    /**
     * 获取用户管理的所有项目ID列表
     */
    public static List<Long> getManagedProjectIds(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        // 1. 检查全局管理员权限
        if (PermissionUtil.isAdmin()) {
            // 管理员可以管理所有项目，返回空列表表示所有项目
            return Collections.emptyList();
        }
        
        try {
            // 2. 获取用户作为项目经理的项目ID列表
            return projectMemberMapper.selectManagedProjectIdsByUser(userId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    
    /**
     * 检查项目内特定权限
     */
    private static boolean checkProjectSpecificPermission(Long userId, Long projectId, ProjectPermissionType permission) {
        try {
            // 1. 检查项目成员表中的快捷权限字段
            ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
            if (member == null || !isPermissionActive(member)) {
                return false;
            }
            
            // 检查快捷权限字段
            switch (permission) {
                case TIMESHEET_APPROVAL:
                    if (Boolean.TRUE.equals(member.getCanApproveTimesheet())) {
                        return true;
                    }
                    break;
                case TASK_MANAGEMENT:
                    if (Boolean.TRUE.equals(member.getCanManageTasks())) {
                        return true;
                    }
                    break;
                case REPORT_VIEW:
                    if (Boolean.TRUE.equals(member.getCanViewReports())) {
                        return true;
                    }
                    break;
            }
            
            // 2. 检查详细权限表
            return permissionMapper.hasActivePermission(userId, projectId, permission.name());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查权限是否在有效期内
     */
    private static boolean isPermissionActive(ProjectMember member) {
        if (member == null) {
            return false;
        }
        
        LocalDate now = LocalDate.now();
        
        // 检查生效日期
        if (member.getEffectiveDate() != null && now.isBefore(member.getEffectiveDate())) {
            return false;
        }
        
        // 检查失效日期
        if (member.getExpiryDate() != null && now.isAfter(member.getExpiryDate())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取当前用户在指定项目中的权限摘要
     */
    public static ProjectPermissionSummary getCurrentUserProjectPermissions(Long projectId) {
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (currentUserId == null || projectId == null) {
            return new ProjectPermissionSummary();
        }
        
        ProjectPermissionSummary summary = new ProjectPermissionSummary();
        summary.setUserId(currentUserId);
        summary.setProjectId(projectId);
        summary.setIsAdmin(PermissionUtil.isAdmin());
        summary.setIsProjectManager(isProjectManager(currentUserId, projectId));
        summary.setIsTechLeader(isTechLeader(currentUserId, projectId));
        summary.setCanApproveTimesheet(canApproveTimesheet(currentUserId, projectId));
        summary.setCanManageTasks(canManageTasks(currentUserId, projectId));
        summary.setCanViewReports(canViewReports(currentUserId, projectId));
        summary.setCanManageCosts(canManageCosts(currentUserId, projectId));
        summary.setCanManageMilestones(canManageMilestones(currentUserId, projectId));
        summary.setCanManageTeam(canManageTeam(currentUserId, projectId));
        summary.setCanGrantPermission(canGrantPermission(currentUserId, projectId));
        summary.setRoleDescription(getUserProjectRoleDescription(currentUserId, projectId));
        summary.setPermissions(getUserProjectPermissions(currentUserId, projectId));
        
        return summary;
    }
    
    /**
     * 项目权限摘要类
     */
    public static class ProjectPermissionSummary {
        private Long userId;
        private Long projectId;
        private Boolean isAdmin = false;
        private Boolean isProjectManager = false;
        private Boolean isTechLeader = false;
        private Boolean canApproveTimesheet = false;
        private Boolean canManageTasks = false;
        private Boolean canViewReports = false;
        private Boolean canManageCosts = false;
        private Boolean canManageMilestones = false;
        private Boolean canManageTeam = false;
        private Boolean canGrantPermission = false;
        private String roleDescription;
        private List<ProjectPermissionType> permissions;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        
        public Boolean getIsAdmin() { return isAdmin; }
        public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }
        
        public Boolean getIsProjectManager() { return isProjectManager; }
        public void setIsProjectManager(Boolean isProjectManager) { this.isProjectManager = isProjectManager; }
        
        public Boolean getIsTechLeader() { return isTechLeader; }
        public void setIsTechLeader(Boolean isTechLeader) { this.isTechLeader = isTechLeader; }
        
        public Boolean getCanApproveTimesheet() { return canApproveTimesheet; }
        public void setCanApproveTimesheet(Boolean canApproveTimesheet) { this.canApproveTimesheet = canApproveTimesheet; }
        
        public Boolean getCanManageTasks() { return canManageTasks; }
        public void setCanManageTasks(Boolean canManageTasks) { this.canManageTasks = canManageTasks; }
        
        public Boolean getCanViewReports() { return canViewReports; }
        public void setCanViewReports(Boolean canViewReports) { this.canViewReports = canViewReports; }
        
        public Boolean getCanManageCosts() { return canManageCosts; }
        public void setCanManageCosts(Boolean canManageCosts) { this.canManageCosts = canManageCosts; }
        
        public Boolean getCanManageMilestones() { return canManageMilestones; }
        public void setCanManageMilestones(Boolean canManageMilestones) { this.canManageMilestones = canManageMilestones; }
        
        public Boolean getCanManageTeam() { return canManageTeam; }
        public void setCanManageTeam(Boolean canManageTeam) { this.canManageTeam = canManageTeam; }
        
        public Boolean getCanGrantPermission() { return canGrantPermission; }
        public void setCanGrantPermission(Boolean canGrantPermission) { this.canGrantPermission = canGrantPermission; }
        
        public String getRoleDescription() { return roleDescription; }
        public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }
        
        public List<ProjectPermissionType> getPermissions() { return permissions; }
        public void setPermissions(List<ProjectPermissionType> permissions) { this.permissions = permissions; }
    }
}