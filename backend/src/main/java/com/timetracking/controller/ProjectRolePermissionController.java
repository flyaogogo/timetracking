package com.timetracking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.timetracking.entity.ProjectRolePermission;
import com.timetracking.service.ProjectRolePermissionService;
import com.timetracking.util.EnhancedPermissionUtil;
import com.timetracking.util.PermissionUtil;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/project-permissions")
public class ProjectRolePermissionController {
    
    @Autowired
    private ProjectRolePermissionService permissionService;
    
    /**
     * 获取项目成员列表（包含权限信息）
     */
    @GetMapping("/members")
    public Result<List<Map<String, Object>>> getProjectMembers(@RequestParam Long projectId) {
        try {
            // 检查权限
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("无法获取当前用户信息");
            }
            
            if (!EnhancedPermissionUtil.canManageTeam(currentUserId, projectId)) {
                return Result.error("无权限查看项目成员");
            }
            
            List<Map<String, Object>> members = permissionService.getProjectMembersWithPermissions(projectId);
            return Result.<List<Map<String, Object>>>success("获取项目成员成功", members);
        } catch (Exception e) {
            return Result.error("获取项目成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 添加项目成员
     */
    @PostMapping("/add-member")
    public Result<String> addProjectMember(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = Long.valueOf(request.get("projectId").toString());
            Long userId = Long.valueOf(request.get("userId").toString());
            String role = request.get("role").toString();
            Integer allocationPercentage = request.get("allocationPercentage") != null ? 
                Integer.valueOf(request.get("allocationPercentage").toString()) : 100;
            Double hourlyRate = request.get("hourlyRate") != null ? 
                Double.valueOf(request.get("hourlyRate").toString()) : 0.0;
            
            // 检查权限
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (!EnhancedPermissionUtil.canManageTeam(currentUserId, projectId)) {
                return Result.error("无权限添加项目成员");
            }
            
            boolean success = permissionService.addProjectMember(projectId, userId, role, allocationPercentage, hourlyRate);
            if (success) {
                return Result.success("添加项目成员成功");
            } else {
                return Result.error("添加项目成员失败");
            }
        } catch (Exception e) {
            return Result.error("添加项目成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 移除项目成员
     */
    @PostMapping("/remove-member")
    public Result<String> removeProjectMember(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = Long.valueOf(request.get("projectId").toString());
            Long memberId = Long.valueOf(request.get("memberId").toString());
            
            // 检查权限
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (!EnhancedPermissionUtil.canManageTeam(currentUserId, projectId)) {
                return Result.error("无权限移除项目成员");
            }
            
            boolean success = permissionService.removeProjectMember(memberId);
            if (success) {
                return Result.success("移除项目成员成功");
            } else {
                return Result.error("移除项目成员失败");
            }
        } catch (Exception e) {
            return Result.error("移除项目成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新成员权限
     */
    @PostMapping("/update-permissions")
    public Result<String> updateMemberPermissions(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = Long.valueOf(request.get("projectId").toString());
            Long memberId = Long.valueOf(request.get("memberId").toString());
            Boolean isProjectManager = (Boolean) request.get("isProjectManager");
            Boolean isTechLeader = (Boolean) request.get("isTechLeader");
            Boolean canApproveTimesheet = (Boolean) request.get("canApproveTimesheet");
            Boolean canManageTasks = (Boolean) request.get("canManageTasks");
            Boolean canViewReports = (Boolean) request.get("canViewReports");
            
            // 检查权限
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (!EnhancedPermissionUtil.canManageTeam(currentUserId, projectId)) {
                return Result.error("无权限更新成员权限");
            }
            
            boolean success = permissionService.updateMemberPermissions(
                memberId, isProjectManager, isTechLeader, canApproveTimesheet, canManageTasks, canViewReports);
            
            if (success) {
                return Result.success("更新成员权限成功");
            } else {
                return Result.error("更新成员权限失败");
            }
        } catch (Exception e) {
            return Result.error("更新成员权限失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目权限列表
     */
    @GetMapping("/project/{projectId}")
    public Result<IPage<ProjectRolePermission>> getProjectPermissions(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        
        // 检查权限
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (!EnhancedPermissionUtil.canManageTeam(currentUserId, projectId)) {
            return Result.error("无权限查看项目权限配置");
        }
        
        IPage<ProjectRolePermission> permissions = permissionService.getProjectPermissions(current, size, projectId);
        return Result.<IPage<ProjectRolePermission>>success("获取项目权限列表成功", permissions);
    }
    
    /**
     * 获取用户权限列表
     */
    @GetMapping("/user/{userId}")
    public Result<IPage<ProjectRolePermission>> getUserPermissions(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        
        // 检查权限
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (!PermissionUtil.canViewUserData(userId)) {
            return Result.error("无权限查看用户权限配置");
        }
        
        IPage<ProjectRolePermission> permissions = permissionService.getUserPermissions(current, size, userId);
        return Result.<IPage<ProjectRolePermission>>success("获取用户权限列表成功", permissions);
    }
    
    /**
     * 设置用户为项目经理
     */
    @PostMapping("/set-project-manager")
    public Result<String> setProjectManager(@RequestBody Map<String, Object> request) {
        Long projectId = Long.valueOf(request.get("projectId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        LocalDate expiryDate = request.get("expiryDate") != null ? 
            LocalDate.parse(request.get("expiryDate").toString()) : null;
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            boolean success = permissionService.setAsProjectManager(projectId, userId, currentUserId, expiryDate);
            if (success) {
                return Result.success("设置项目经理成功");
            } else {
                return Result.error("设置项目经理失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设置用户为技术负责人
     */
    @PostMapping("/set-tech-leader")
    public Result<String> setTechLeader(@RequestBody Map<String, Object> request) {
        Long projectId = Long.valueOf(request.get("projectId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        LocalDate expiryDate = request.get("expiryDate") != null ? 
            LocalDate.parse(request.get("expiryDate").toString()) : null;
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            boolean success = permissionService.setAsTechLeader(projectId, userId, currentUserId, expiryDate);
            if (success) {
                return Result.success("设置技术负责人成功");
            } else {
                return Result.error("设置技术负责人失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 撤销项目经理权限
     */
    @PostMapping("/revoke-project-manager")
    public Result<String> revokeProjectManager(@RequestBody Map<String, Object> request) {
        Long projectId = Long.valueOf(request.get("projectId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            boolean success = permissionService.revokeProjectManager(projectId, userId, currentUserId);
            if (success) {
                return Result.success("撤销项目经理权限成功");
            } else {
                return Result.error("撤销项目经理权限失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 撤销技术负责人权限
     */
    @PostMapping("/revoke-tech-leader")
    public Result<String> revokeTechLeader(@RequestBody Map<String, Object> request) {
        Long projectId = Long.valueOf(request.get("projectId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            boolean success = permissionService.revokeTechLeader(projectId, userId, currentUserId);
            if (success) {
                return Result.success("撤销技术负责人权限成功");
            } else {
                return Result.error("撤销技术负责人权限失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 授予特定权限
     */
    @PostMapping("/grant-permission")
    public Result<String> grantPermission(@RequestBody Map<String, Object> request) {
        Long projectId = Long.valueOf(request.get("projectId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String permissionType = request.get("permissionType").toString();
        LocalDate expiryDate = request.get("expiryDate") != null ? 
            LocalDate.parse(request.get("expiryDate").toString()) : null;
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            ProjectRolePermission.PermissionType permission = 
                ProjectRolePermission.PermissionType.valueOf(permissionType);
            boolean success = permissionService.grantProjectPermission(
                projectId, userId, permission, currentUserId, expiryDate);
            
            if (success) {
                return Result.success("授予权限成功");
            } else {
                return Result.error("授予权限失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量授予权限
     */
    @PostMapping("/grant-multiple-permissions")
    public Result<String> grantMultiplePermissions(@RequestBody Map<String, Object> request) {
        Long projectId = Long.valueOf(request.get("projectId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<String> permissionTypes = (List<String>) request.get("permissions");
        LocalDate expiryDate = request.get("expiryDate") != null ? 
            LocalDate.parse(request.get("expiryDate").toString()) : null;
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            List<ProjectRolePermission.PermissionType> permissions = permissionTypes.stream()
                .map(ProjectRolePermission.PermissionType::valueOf)
                .collect(Collectors.toList());
            
            boolean success = permissionService.grantMultiplePermissions(
                projectId, userId, permissions, currentUserId, expiryDate);
            
            if (success) {
                return Result.success("批量授予权限成功");
            } else {
                return Result.error("批量授予权限失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 撤销特定权限
     */
    @PostMapping("/revoke-permission")
    public Result<String> revokePermission(@RequestBody Map<String, Object> request) {
        Long projectId = Long.valueOf(request.get("projectId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String permissionType = request.get("permissionType").toString();
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            ProjectRolePermission.PermissionType permission = 
                ProjectRolePermission.PermissionType.valueOf(permissionType);
            boolean success = permissionService.revokePermission(
                projectId, userId, permission, currentUserId);
            
            if (success) {
                return Result.success("撤销权限成功");
            } else {
                return Result.error("撤销权限失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户在项目中的权限摘要
     */
    @GetMapping("/summary/{projectId}")
    public Result<EnhancedPermissionUtil.ProjectPermissionSummary> getPermissionSummary(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long userId) {
        
        Long targetUserId = userId != null ? userId : PermissionUtil.getCurrentUserId();
        
        // 检查权限
        if (!PermissionUtil.canViewUserData(targetUserId)) {
            return Result.error("无权限查看用户权限信息");
        }
        
        EnhancedPermissionUtil.ProjectPermissionSummary summary = 
            permissionService.getUserProjectPermissionSummary(targetUserId, projectId);
        
        return Result.<EnhancedPermissionUtil.ProjectPermissionSummary>success("获取权限摘要成功", summary);
    }
    
    /**
     * 获取即将过期的权限
     */
    @GetMapping("/expiring")
    public Result<List<ProjectRolePermission>> getExpiringPermissions(
            @RequestParam(defaultValue = "7") int days) {
        
        // 只有管理员可以查看即将过期的权限
        if (!PermissionUtil.isAdmin()) {
            return Result.error("无权限查看即将过期的权限");
        }
        
        List<ProjectRolePermission> permissions = permissionService.getExpiringPermissions(days);
        return Result.<List<ProjectRolePermission>>success("获取即将过期权限成功", permissions);
    }
    
    /**
     * 清理过期权限
     */
    @PostMapping("/cleanup-expired")
    public Result<String> cleanupExpiredPermissions() {
        // 只有管理员可以清理过期权限
        if (!PermissionUtil.isAdmin()) {
            return Result.error("无权限执行清理操作");
        }
        
        int count = permissionService.cleanupExpiredPermissions();
        return Result.success("清理完成，共清理 " + count + " 条过期权限");
    }
    
    /**
     * 复制权限配置
     */
    @PostMapping("/copy-permissions")
    public Result<String> copyPermissions(@RequestBody Map<String, Object> request) {
        Long sourceUserId = Long.valueOf(request.get("sourceUserId").toString());
        Long targetUserId = Long.valueOf(request.get("targetUserId").toString());
        Long projectId = Long.valueOf(request.get("projectId").toString());
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            boolean success = permissionService.copyPermissions(
                sourceUserId, targetUserId, projectId, currentUserId);
            
            if (success) {
                return Result.success("权限复制成功");
            } else {
                return Result.error("权限复制失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新权限过期时间
     */
    @PutMapping("/update-expiry/{permissionId}")
    public Result<String> updatePermissionExpiry(
            @PathVariable Long permissionId,
            @RequestBody Map<String, Object> request) {
        
        LocalDate newExpiryDate = request.get("expiryDate") != null ? 
            LocalDate.parse(request.get("expiryDate").toString()) : null;
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        
        try {
            boolean success = permissionService.updatePermissionExpiry(
                permissionId, newExpiryDate, currentUserId);
            
            if (success) {
                return Result.success("权限过期时间更新成功");
            } else {
                return Result.error("权限过期时间更新失败");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}