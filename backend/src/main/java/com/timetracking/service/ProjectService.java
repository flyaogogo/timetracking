package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timetracking.entity.Project;
import com.timetracking.entity.User;
import com.timetracking.mapper.ProjectMapper;
import com.timetracking.mapper.ProjectMemberMapper;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService extends ServiceImpl<ProjectMapper, Project> {
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    public IPage<Project> getProjectList(int current, int size, String keyword) {
        return getProjectList(current, size, keyword, null);
    }
    
    public IPage<Project> getProjectList(int current, int size, String keyword, Long userId) {
        Page<Project> page = new Page<>(current, size);
        
        // 获取当前用户信息
        Long currentUserId = PermissionUtil.getCurrentUserId();
        boolean canViewAll = PermissionUtil.canViewAllProjects();
        
        if (canViewAll) {
            // 管理员可以查看所有项目
            QueryWrapper<Project> wrapper = new QueryWrapper<>();
            
            // 添加关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("project_name", keyword)
                        .or().like("project_code", keyword)
                        .or().like("description", keyword));
            }
            
            return baseMapper.selectProjectsWithManager(page);
        } else {
            // 其他角色只能查看参与的项目
            Long targetUserId = userId != null ? userId : currentUserId;
            return baseMapper.selectUserProjects(page, targetUserId, keyword);
        }
    }
    
    /**
     * 根据用户权限获取项目列表
     */
    public IPage<Project> getProjectListByPermission(int current, int size, String keyword, Long userId) {
        Page<Project> page = new Page<>(current, size);
        
        if (userId == null) {
            return page; // 返回空页面
        }
        
        // 只有管理员可以查看所有项目
        if (PermissionUtil.isAdmin()) {
            return baseMapper.selectAllProjectsWithDetails(page, keyword);
        }
        
        // 所有其他用户（包括全局项目经理）只能查看参与的项目和管理的项目
        return baseMapper.selectUserProjectsWithPermission(page, userId, keyword);
    }
    
    /**
     * 获取用户参与的项目ID列表（用于其他模块的项目过滤）
     */
    public List<Long> getUserAccessibleProjectIds(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        // 只有管理员可以访问所有项目
        if (PermissionUtil.isAdmin()) {
            return baseMapper.selectAllProjectIds();
        }
        
        // 所有其他用户（包括全局项目经理）只能访问参与的项目和管理的项目
        return baseMapper.selectUserAccessibleProjectIds(userId);
    }
    
    /**
     * 检查用户是否可以访问指定项目
     */
    public boolean canUserAccessProject(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 管理员可以访问所有项目
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 检查是否为项目成员
        if (isProjectMember(projectId, userId)) {
            return true;
        }
        
        // 检查是否为项目经理（项目内权限）
        if (EnhancedPermissionUtil.isProjectManager(userId, projectId)) {
            return true;
        }
        
        // 检查是否有其他项目权限
        return EnhancedPermissionUtil.hasProjectPermission(userId, projectId, 
                EnhancedPermissionUtil.ProjectPermissionType.PROJECT_MANAGEMENT);
    }
    
    /**
     * 获取用户管理的项目列表 - 只返回用户担任项目经理的项目
     */
    public IPage<Project> getManagedProjects(int current, int size, Long userId) {
        Page<Project> page = new Page<>(current, size);
        
        if (userId == null) {
            return page;
        }
        
        // 所有用户（包括管理员）都只能看到自己担任项目经理的项目
        // 这确保了项目经理工作台只显示用户真正管理的项目
        return baseMapper.selectManagedProjects(page, userId);
    }
    
    public Project getProjectWithManager(Long id) {
        // 检查项目访问权限
        if (!hasProjectAccess(id)) {
            return null;
        }
        
        return baseMapper.selectProjectWithManager(id);
    }
    
    /**
     * 创建项目
     */
    public Project createProject(Project project) {
        // 设置创建者
        Long currentUserId = PermissionUtil.getCurrentUserId();
        project.setManagerId(currentUserId);
        
        // 智能设置初始状态：如果前端没有提供状态
        if (project.getStatus() == null) {
            // 基于项目属性智能判断初始状态
            if (isProjectInProgress(project)) {
                project.setStatus(Project.ProjectStatus.IN_PROGRESS);
            } else {
                project.setStatus(Project.ProjectStatus.PLANNING);
            }
        }
        
        save(project);
        return project;
    }
    
    /**
     * 判断项目是否应该处于进行中状态
     * 基于项目属性自动判断：
     * 1. 有实际开始日期
     * 2. 有实际工时
     * 3. 计划开始日期已过
     * 4. 有团队成员
     * 5. 有任务分配
     */
    private boolean isProjectInProgress(Project project) {
        // 1. 检查是否有实际开始日期
        if (project.getActualStartDate() != null) {
            return true;
        }
        
        // 2. 检查是否有实际工时
        if (project.getActualHours() != null && project.getActualHours().compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        
        // 3. 检查计划开始日期是否已过
        if (project.getPlannedStartDate() != null) {
            LocalDate now = LocalDate.now();
            if (project.getPlannedStartDate().isBefore(now) || project.getPlannedStartDate().isEqual(now)) {
                return true;
            }
        }
        
        // 4. 检查开始日期（兼容旧字段）
        if (project.getStartDate() != null) {
            LocalDate now = LocalDate.now();
            if (project.getStartDate().isBefore(now) || project.getStartDate().isEqual(now)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 更新项目
     */
    public Project updateProject(Project project) {
        updateById(project);
        return getById(project.getId());
    }
    
    /**
     * 删除项目
     */
    public void deleteProject(Long id) {
        removeById(id);
    }
    
    /**
     * 检查用户是否为项目成员
     */
    public boolean isProjectMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        
        try {
            return projectMemberMapper.selectByProjectAndUser(projectId, userId) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查项目访问权限
     */
    private boolean hasProjectAccess(Long projectId) {
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (currentUserId == null || projectId == null) {
            return false;
        }
        
        // 只有管理员可以访问所有项目
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 检查是否为项目成员或项目经理
        return isProjectMember(projectId, currentUserId) ||
               EnhancedPermissionUtil.isProjectManager(currentUserId, projectId);
    }
    
    /**
     * 根据ID列表获取项目信息
     */
    public List<Project> getProjectsByIds(List<Long> projectIds) {
        if (projectIds == null || projectIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return baseMapper.selectBatchIds(projectIds);
    }
}