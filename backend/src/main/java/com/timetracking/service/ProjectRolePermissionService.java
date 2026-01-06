package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timetracking.entity.ProjectMember;
import com.timetracking.entity.ProjectRolePermission;
import com.timetracking.entity.User;
import com.timetracking.mapper.ProjectMemberMapper;
import com.timetracking.mapper.ProjectRolePermissionMapper;
import com.timetracking.util.EnhancedPermissionUtil;
import com.timetracking.util.PermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ProjectRolePermissionService extends ServiceImpl<ProjectRolePermissionMapper, ProjectRolePermission> {
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    /**
     * 为用户在项目中授予权限
     */
    @Transactional
    public boolean grantProjectPermission(Long projectId, Long userId, 
                                        ProjectRolePermission.PermissionType permission,
                                        Long grantedBy, LocalDate expiryDate) {
        // 检查授权人是否有权限
        if (!canGrantPermission(grantedBy, projectId)) {
            throw new RuntimeException("无权限进行授权操作");
        }
        
        // 检查用户是否为项目成员
        if (!projectMemberMapper.isProjectMember(projectId, userId)) {
            throw new RuntimeException("用户不是项目成员，无法授予权限");
        }
        
        // 查找现有权限记录
        QueryWrapper<ProjectRolePermission> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId)
               .eq("user_id", userId)
               .eq("permission_type", permission);
        
        ProjectRolePermission existingPerm = getOne(wrapper);
        
        if (existingPerm != null) {
            // 更新现有权限
            existingPerm.setIsActive(true);
            existingPerm.setGrantedBy(grantedBy);
            existingPerm.setGrantedDate(LocalDateTime.now());
            existingPerm.setExpiryDate(expiryDate);
            return updateById(existingPerm);
        } else {
            // 创建新权限记录
            ProjectRolePermission perm = new ProjectRolePermission();
            perm.setProjectId(projectId);
            perm.setUserId(userId);
            perm.setPermissionType(permission);
            perm.setGrantedBy(grantedBy);
            perm.setGrantedDate(LocalDateTime.now());
            perm.setEffectiveDate(LocalDate.now());
            perm.setExpiryDate(expiryDate);
            perm.setIsActive(true);
            
            return save(perm);
        }
    }
    
    /**
     * 批量授予权限
     */
    @Transactional
    public boolean grantMultiplePermissions(Long projectId, Long userId,
                                          List<ProjectRolePermission.PermissionType> permissions,
                                          Long grantedBy, LocalDate expiryDate) {
        for (ProjectRolePermission.PermissionType permission : permissions) {
            if (!grantProjectPermission(projectId, userId, permission, grantedBy, expiryDate)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 设置用户为项目经理
     */
    @Transactional
    public boolean setAsProjectManager(Long projectId, Long userId, Long grantedBy) {
        return setAsProjectManager(projectId, userId, grantedBy, null);
    }
    
    /**
     * 设置用户为项目经理（带过期时间）
     */
    @Transactional
    public boolean setAsProjectManager(Long projectId, Long userId, Long grantedBy, LocalDate expiryDate) {
        // 检查授权人权限
        if (!canGrantPermission(grantedBy, projectId)) {
            throw new RuntimeException("无权限进行授权操作");
        }
        
        // 1. 更新项目成员表
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
        if (member == null) {
            throw new RuntimeException("用户不是项目成员");
        }
        
        member.setIsProjectManager(true);
        member.setCanApproveTimesheet(true);
        member.setCanManageTasks(true);
        member.setCanViewReports(true);
        member.setExpiryDate(expiryDate);
        projectMemberMapper.updateById(member);
        
        // 2. 授予项目管理相关权限
        List<ProjectRolePermission.PermissionType> managerPermissions = Arrays.asList(
            ProjectRolePermission.PermissionType.PROJECT_MANAGEMENT,
            ProjectRolePermission.PermissionType.TASK_MANAGEMENT,
            ProjectRolePermission.PermissionType.TIMESHEET_APPROVAL,
            ProjectRolePermission.PermissionType.REPORT_VIEW,
            ProjectRolePermission.PermissionType.TEAM_MANAGEMENT
        );
        
        return grantMultiplePermissions(projectId, userId, managerPermissions, grantedBy, expiryDate);
    }
    
    /**
     * 设置用户为技术负责人
     */
    @Transactional
    public boolean setAsTechLeader(Long projectId, Long userId, Long grantedBy, LocalDate expiryDate) {
        // 检查授权人权限
        if (!canGrantPermission(grantedBy, projectId)) {
            throw new RuntimeException("无权限进行授权操作");
        }
        
        // 1. 更新项目成员表
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
        if (member == null) {
            throw new RuntimeException("用户不是项目成员");
        }
        
        member.setIsTechLeader(true);
        member.setCanManageTasks(true);
        member.setCanViewReports(true);
        member.setExpiryDate(expiryDate);
        projectMemberMapper.updateById(member);
        
        // 2. 授予技术负责人相关权限
        List<ProjectRolePermission.PermissionType> techLeaderPermissions = Arrays.asList(
            ProjectRolePermission.PermissionType.TASK_MANAGEMENT,
            ProjectRolePermission.PermissionType.REPORT_VIEW,
            ProjectRolePermission.PermissionType.MILESTONE_MANAGEMENT
        );
        
        return grantMultiplePermissions(projectId, userId, techLeaderPermissions, grantedBy, expiryDate);
    }
    
    /**
     * 撤销项目经理权限
     */
    @Transactional
    public boolean revokeProjectManager(Long projectId, Long userId, Long revokedBy) {
        // 检查撤销人权限
        if (!canGrantPermission(revokedBy, projectId)) {
            throw new RuntimeException("无权限进行撤销操作");
        }
        
        // 1. 更新项目成员表 - 完全清除项目经理相关权限
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
        if (member != null) {
            member.setIsProjectManager(false);
            // 撤销项目经理后，如果不是技术负责人，则撤销所有管理权限
            if (!Boolean.TRUE.equals(member.getIsTechLeader())) {
                member.setCanApproveTimesheet(false);
                member.setCanManageTasks(false);
                member.setCanViewReports(false);
            } else {
                // 如果是技术负责人，保留技术负责人应有的权限
                member.setCanApproveTimesheet(false); // 技术负责人不能审核工时
                member.setCanManageTasks(true);       // 保留任务管理权限
                member.setCanViewReports(true);       // 保留报表查看权限
            }
            projectMemberMapper.updateById(member);
        }
        
        // 2. 撤销项目经理特有的权限
        List<ProjectRolePermission.PermissionType> managerOnlyPermissions = Arrays.asList(
            ProjectRolePermission.PermissionType.PROJECT_MANAGEMENT,
            ProjectRolePermission.PermissionType.TIMESHEET_APPROVAL,
            ProjectRolePermission.PermissionType.TEAM_MANAGEMENT
        );
        
        for (ProjectRolePermission.PermissionType permission : managerOnlyPermissions) {
            baseMapper.revokeUserProjectPermission(projectId, userId, permission.name());
        }
        
        return true;
    }
    
    /**
     * 撤销技术负责人权限
     */
    @Transactional
    public boolean revokeTechLeader(Long projectId, Long userId, Long revokedBy) {
        // 检查撤销人权限
        if (!canGrantPermission(revokedBy, projectId)) {
            throw new RuntimeException("无权限进行撤销操作");
        }
        
        // 1. 更新项目成员表
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
        if (member != null) {
            member.setIsTechLeader(false);
            // 如果不是项目经理，则撤销技术负责人的权限
            if (!Boolean.TRUE.equals(member.getIsProjectManager())) {
                member.setCanManageTasks(false);
                member.setCanViewReports(false);
            }
            // 技术负责人本身就不应该有工时审核权限，但为了确保一致性
            if (!Boolean.TRUE.equals(member.getIsProjectManager())) {
                member.setCanApproveTimesheet(false);
            }
            projectMemberMapper.updateById(member);
        }
        
        // 2. 撤销技术负责人特有权限
        List<ProjectRolePermission.PermissionType> techLeaderPermissions = Arrays.asList(
            ProjectRolePermission.PermissionType.MILESTONE_MANAGEMENT
        );
        
        for (ProjectRolePermission.PermissionType permission : techLeaderPermissions) {
            baseMapper.revokeUserProjectPermission(projectId, userId, permission.name());
        }
        
        // 3. 如果既不是项目经理也不是技术负责人，撤销任务管理和报表查看权限
        if (member != null && !Boolean.TRUE.equals(member.getIsProjectManager())) {
            baseMapper.revokeUserProjectPermission(projectId, userId, 
                ProjectRolePermission.PermissionType.TASK_MANAGEMENT.name());
            baseMapper.revokeUserProjectPermission(projectId, userId, 
                ProjectRolePermission.PermissionType.REPORT_VIEW.name());
        }
        
        return true;
    }
    
    /**
     * 撤销特定权限
     */
    public boolean revokePermission(Long projectId, Long userId, 
                                  ProjectRolePermission.PermissionType permission, Long revokedBy) {
        // 检查撤销人权限
        if (!canGrantPermission(revokedBy, projectId)) {
            throw new RuntimeException("无权限进行撤销操作");
        }
        
        return baseMapper.revokeUserProjectPermission(projectId, userId, permission.name()) > 0;
    }
    
    /**
     * 获取项目权限列表
     */
    public IPage<ProjectRolePermission> getProjectPermissions(int current, int size, Long projectId) {
        Page<ProjectRolePermission> page = new Page<>(current, size);
        return baseMapper.selectProjectPermissions(page, projectId);
    }
    
    /**
     * 获取用户权限列表
     */
    public IPage<ProjectRolePermission> getUserPermissions(int current, int size, Long userId) {
        Page<ProjectRolePermission> page = new Page<>(current, size);
        return baseMapper.selectUserPermissions(page, userId);
    }
    
    /**
     * 获取即将过期的权限
     */
    public List<ProjectRolePermission> getExpiringPermissions(int days) {
        return baseMapper.selectExpiringPermissions(days);
    }
    
    /**
     * 清理过期权限
     */
    @Transactional
    public int cleanupExpiredPermissions() {
        return baseMapper.cleanupExpiredPermissions();
    }
    
    /**
     * 更新权限过期时间
     */
    public boolean updatePermissionExpiry(Long permissionId, LocalDate newExpiryDate, Long updatedBy) {
        ProjectRolePermission permission = getById(permissionId);
        if (permission == null) {
            return false;
        }
        
        // 检查更新人权限
        if (!canGrantPermission(updatedBy, permission.getProjectId())) {
            throw new RuntimeException("无权限进行更新操作");
        }
        
        permission.setExpiryDate(newExpiryDate);
        return updateById(permission);
    }
    
    /**
     * 获取用户在项目中的权限摘要
     */
    public EnhancedPermissionUtil.ProjectPermissionSummary getUserProjectPermissionSummary(Long userId, Long projectId) {
        return EnhancedPermissionUtil.getCurrentUserProjectPermissions(projectId);
    }
    
    /**
     * 检查是否可以授予权限
     */
    private boolean canGrantPermission(Long userId, Long projectId) {
        return PermissionUtil.isAdmin() || 
               EnhancedPermissionUtil.isProjectManager(userId, projectId);
    }
    
    /**
     * 复制权限配置到其他用户
     */
    @Transactional
    public boolean copyPermissions(Long sourceUserId, Long targetUserId, Long projectId, Long operatedBy) {
        // 检查操作人权限
        if (!canGrantPermission(operatedBy, projectId)) {
            throw new RuntimeException("无权限进行权限复制操作");
        }
        
        // 获取源用户的权限
        List<EnhancedPermissionUtil.ProjectPermissionType> sourcePermissions = 
            baseMapper.selectUserProjectPermissions(sourceUserId, projectId);
        
        if (sourcePermissions.isEmpty()) {
            return true; // 源用户没有权限，复制完成
        }
        
        // 复制权限到目标用户
        for (EnhancedPermissionUtil.ProjectPermissionType permission : sourcePermissions) {
            ProjectRolePermission.PermissionType permType = 
                ProjectRolePermission.PermissionType.valueOf(permission.name());
            grantProjectPermission(projectId, targetUserId, permType, operatedBy, null);
        }
        
        return true;
    }
    
    /**
     * 获取项目成员列表（包含权限信息）
     */
    public List<java.util.Map<String, Object>> getProjectMembersWithPermissions(Long projectId) {
        return projectMemberMapper.selectByProjectId(projectId).stream()
            .map(member -> {
                java.util.Map<String, Object> memberMap = new java.util.HashMap<>();
                memberMap.put("id", member.getId());
                memberMap.put("userId", member.getUserId());
                memberMap.put("userRealName", member.getUserRealName());
                // memberMap.put("username", member.getUsername()); // Skip username for now
                memberMap.put("email", member.getEmail());
                memberMap.put("role", member.getRole());
                memberMap.put("joinDate", member.getJoinDate());
                memberMap.put("isProjectManager", member.getIsProjectManager());
                memberMap.put("isTechLeader", member.getIsTechLeader());
                memberMap.put("canApproveTimesheet", member.getCanApproveTimesheet());
                memberMap.put("canManageTasks", member.getCanManageTasks());
                memberMap.put("canViewReports", member.getCanViewReports());
                memberMap.put("effectiveDate", member.getEffectiveDate());
                memberMap.put("expiryDate", member.getExpiryDate());
                memberMap.put("allocationPercentage", member.getAllocationPercentage());
                memberMap.put("hourlyRate", member.getHourlyRate());
                memberMap.put("skillLevel", member.getSkillLevel());
                memberMap.put("performanceRating", member.getPerformanceRating());
                memberMap.put("productivityIndex", member.getProductivityIndex());
                return memberMap;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 添加项目成员
     */
    @Transactional
    public boolean addProjectMember(Long projectId, Long userId, String role, 
                                   Integer allocationPercentage, Double hourlyRate) {
        try {
            // 检查用户是否已经是项目成员
            if (projectMemberMapper.isProjectMember(projectId, userId)) {
                throw new RuntimeException("用户已经是项目成员");
            }
            
            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(userId);
            member.setRole(ProjectMember.ProjectRole.valueOf(role));
            member.setJoinDate(LocalDate.now());
            member.setAllocationPercentage(java.math.BigDecimal.valueOf(allocationPercentage));
            member.setHourlyRate(java.math.BigDecimal.valueOf(hourlyRate));
            member.setIsProjectManager(false);
            member.setIsTechLeader(false);
            member.setCanApproveTimesheet(false);
            member.setCanManageTasks(false);
            member.setCanViewReports(false);
            member.setSkillLevel(ProjectMember.SkillLevel.INTERMEDIATE);
            member.setPerformanceRating(java.math.BigDecimal.valueOf(3.5));
            member.setProductivityIndex(java.math.BigDecimal.valueOf(1.0));
            
            return projectMemberMapper.insert(member) > 0;
        } catch (Exception e) {
            throw new RuntimeException("添加项目成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 移除项目成员
     */
    @Transactional
    public boolean removeProjectMember(Long memberId) {
        try {
            // 先撤销该成员的所有权限
            ProjectMember member = projectMemberMapper.selectById(memberId);
            if (member != null) {
                baseMapper.revokeUserProjectPermissions(member.getProjectId(), member.getUserId());
            }
            
            // 删除项目成员记录
            return projectMemberMapper.deleteById(memberId) > 0;
        } catch (Exception e) {
            throw new RuntimeException("移除项目成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新成员权限
     */
    @Transactional
    public boolean updateMemberPermissions(Long memberId, Boolean isProjectManager, 
                                         Boolean isTechLeader, Boolean canApproveTimesheet,
                                         Boolean canManageTasks, Boolean canViewReports) {
        try {
            ProjectMember member = projectMemberMapper.selectById(memberId);
            if (member == null) {
                throw new RuntimeException("项目成员不存在");
            }
            
            member.setIsProjectManager(isProjectManager);
            member.setIsTechLeader(isTechLeader);
            member.setCanApproveTimesheet(canApproveTimesheet);
            member.setCanManageTasks(canManageTasks);
            member.setCanViewReports(canViewReports);
            
            return projectMemberMapper.updateById(member) > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新成员权限失败: " + e.getMessage());
        }
    }
}