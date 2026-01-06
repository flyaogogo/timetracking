package com.timetracking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.timetracking.entity.Project;
import com.timetracking.service.ProjectService;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@CrossOrigin
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping
    public Result getProjectList(@RequestParam(defaultValue = "1") int current,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String keyword,
                                @RequestParam(required = false) Long userId) {
        try {
            // 根据用户权限获取项目列表
            Long currentUserId = PermissionUtil.getCurrentUserId();
            IPage<Project> page = projectService.getProjectListByPermission(current, size, keyword, currentUserId);
            return Result.success("获取项目列表成功", page);
        } catch (Exception e) {
            return Result.error("获取项目列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/managed")
    public Result getManagedProjects(@RequestParam(defaultValue = "1") int current,
                                    @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取用户管理的项目列表
            IPage<Project> page = projectService.getManagedProjects(current, size, currentUserId);
            return Result.success("获取管理项目列表成功", page);
        } catch (Exception e) {
            return Result.error("获取管理项目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户可访问的项目列表（用于下拉选择）
     */
    @GetMapping("/accessible")
    public Result getAccessibleProjects() {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取用户可访问的项目列表
            List<Long> projectIds = projectService.getUserAccessibleProjectIds(currentUserId);
            List<Project> projects = projectService.getProjectsByIds(projectIds);
            
            return Result.success("获取可访问项目成功", projects);
        } catch (Exception e) {
            return Result.error("获取可访问项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定用户的项目列表（分页）
     */
    @GetMapping("/user/{userId}")
    public Result getUserProjects(@PathVariable Long userId,
                                 @RequestParam(defaultValue = "1") int current,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String keyword) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 只能查看自己的项目，除非是管理员
            if (!currentUserId.equals(userId) && !PermissionUtil.isAdmin()) {
                return Result.error("无权限查看其他用户的项目");
            }
            
            // 获取用户项目列表
            IPage<Project> page = projectService.getProjectListByPermission(current, size, keyword, userId);
            return Result.success("获取用户项目列表成功", page);
        } catch (Exception e) {
            return Result.error("获取用户项目列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result getProjectById(@PathVariable Long id) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 检查项目访问权限
            if (!canAccessProject(currentUserId, id)) {
                return Result.error("无权限访问该项目");
            }
            
            Project project = projectService.getProjectWithManager(id);
            if (project == null) {
                return Result.error("项目不存在");
            }
            return Result.success("获取项目信息成功", project);
        } catch (Exception e) {
            return Result.error("获取项目信息失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    public Result createProject(@RequestBody Project project) {
        try {
            if (!PermissionUtil.canManageAllProjects()) {
                return Result.error("无权限创建项目");
            }
            
            Project savedProject = projectService.createProject(project);
            return Result.success("创建项目成功", savedProject);
        } catch (Exception e) {
            return Result.error("创建项目失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result updateProject(@PathVariable Long id, @RequestBody Project project) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 检查项目管理权限
            if (!canManageProject(currentUserId, id)) {
                return Result.error("无权限修改该项目");
            }
            
            project.setId(id);
            Project updatedProject = projectService.updateProject(project);
            return Result.success("更新项目成功", updatedProject);
        } catch (Exception e) {
            return Result.error("更新项目失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result deleteProject(@PathVariable Long id) {
        try {
            if (!PermissionUtil.canManageAllProjects()) {
                return Result.error("无权限删除项目");
            }
            
            projectService.deleteProject(id);
            return Result.success("删除项目成功");
        } catch (Exception e) {
            return Result.error("删除项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否可以访问项目
     */
    private boolean canAccessProject(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 管理员可以访问所有项目
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 检查是否为项目成员或项目经理
        return projectService.isProjectMember(projectId, userId) ||
               EnhancedPermissionUtil.isProjectManager(userId, projectId);
    }
    
    /**
     * 检查用户是否可以管理项目
     */
    private boolean canManageProject(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        // 管理员可以管理所有项目
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 检查是否为项目经理
        return EnhancedPermissionUtil.isProjectManager(userId, projectId);
    }
}