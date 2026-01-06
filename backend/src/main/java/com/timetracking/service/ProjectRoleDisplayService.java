package com.timetracking.service;

import com.timetracking.entity.ProjectMember;
import com.timetracking.mapper.ProjectMemberMapper;
import com.timetracking.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 项目角色显示服务
 * 处理项目经理身份与项目角色的显示逻辑
 */
@Service
public class ProjectRoleDisplayService {
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    /**
     * 获取用户在项目中的显示角色
     * 优先级：项目经理 > 技术负责人 > 基础角色
     */
    public String getDisplayRole(Long projectId, Long userId) {
        // 1. 检查是否是项目的正式项目经理（通过projects表）
        Long managerId = projectMapper.getProjectManagerId(projectId);
        if (managerId != null && managerId.equals(userId)) {
            return "MANAGER"; // 项目经理
        }
        
        // 2. 检查项目成员表中的权限设置
        ProjectMember member = projectMemberMapper.selectByProjectAndUser(projectId, userId);
        if (member != null) {
            // 检查是否有项目经理权限
            if (Boolean.TRUE.equals(member.getIsProjectManager())) {
                return "MANAGER"; // 项目经理
            }
            
            // 检查是否是技术负责人
            if (Boolean.TRUE.equals(member.getIsTechLeader())) {
                return "TECH_LEADER"; // 技术负责人
            }
            
            // 返回基础角色
            return member.getRole() != null ? member.getRole().name() : "DEVELOPER";
        }
        
        return "DEVELOPER"; // 默认角色
    }
    
    /**
     * 获取角色的中文显示名称
     */
    public String getRoleDisplayName(String role) {
        if (role == null) return "开发人员";
        
        switch (role.toUpperCase()) {
            case "MANAGER":
                return "项目经理";
            case "TECH_LEADER":
                return "技术负责人";
            case "DEVELOPER":
                return "开发人员";
            case "TESTER":
                return "测试人员";
            case "DESIGNER":
                return "设计师";
            case "PRODUCT_MANAGER":
                return "产品经理";
            case "OTHER":
                return "其他";
            default:
                return role;
        }
    }
    
    /**
     * 获取项目成员列表，包含正确的角色显示
     */
    public List<ProjectMember> getProjectMembersWithDisplayRole(Long projectId) {
        List<ProjectMember> members = projectMemberMapper.selectByProjectId(projectId);
        
        // 为每个成员设置正确的显示角色
        for (ProjectMember member : members) {
            String displayRole = getDisplayRole(projectId, member.getUserId());
            member.setDisplayRole(displayRole);
            member.setDisplayRoleName(getRoleDisplayName(displayRole));
        }
        
        return members;
    }
    
    /**
     * 修复项目成员的角色数据
     * 将项目经理的role字段更新为MANAGER
     */
    public void fixProjectMemberRoles(Long projectId) {
        // 1. 获取项目的正式项目经理
        Long managerId = projectMapper.getProjectManagerId(projectId);
        if (managerId != null) {
            ProjectMember managerMember = projectMemberMapper.selectByProjectAndUser(projectId, managerId);
            if (managerMember != null && !ProjectMember.ProjectRole.MANAGER.equals(managerMember.getRole())) {
                // 更新项目经理的角色为MANAGER
                managerMember.setRole(ProjectMember.ProjectRole.MANAGER);
                managerMember.setIsProjectManager(true);
                projectMemberMapper.updateById(managerMember);
            }
        }
        
        // 2. 检查所有设置了is_project_manager=true的成员
        List<ProjectMember> projectManagers = projectMemberMapper.selectProjectManagers(projectId);
        for (ProjectMember pm : projectManagers) {
            if (!ProjectMember.ProjectRole.MANAGER.equals(pm.getRole())) {
                pm.setRole(ProjectMember.ProjectRole.MANAGER);
                projectMemberMapper.updateById(pm);
            }
        }
    }
    
    /**
     * 获取用户在所有项目中的角色信息
     */
    public List<Map<String, Object>> getUserProjectRoles(Long userId) {
        return projectMemberMapper.getUserProjectRolesWithDisplay(userId);
    }
}