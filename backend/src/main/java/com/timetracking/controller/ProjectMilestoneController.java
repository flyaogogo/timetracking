package com.timetracking.controller;

import com.timetracking.entity.ProjectMilestone;
import com.timetracking.service.ProjectMilestoneService;
import com.timetracking.service.ProjectService;
import com.timetracking.util.PermissionUtil;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目里程碑管理控制器
 */
@RestController
@RequestMapping("/project-milestones")
@CrossOrigin(origins = "*")
public class ProjectMilestoneController {
    
    @Autowired
    private ProjectMilestoneService projectMilestoneService;
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 获取项目里程碑列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<ProjectMilestone>> getProjectMilestones(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目里程碑");
            }
            
            List<ProjectMilestone> milestones = projectMilestoneService.getProjectMilestones(projectId);
            return Result.success("获取项目里程碑成功", milestones);
        } catch (Exception e) {
            return Result.error("获取项目里程碑失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目里程碑统计
     */
    @GetMapping("/project/{projectId}/statistics")
    public Result<Map<String, Object>> getProjectMilestoneStatistics(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目里程碑统计");
            }
            
            Map<String, Object> statistics = projectMilestoneService.getProjectMilestoneStatistics(projectId);
            return Result.success("获取里程碑统计成功", statistics);
        } catch (Exception e) {
            return Result.error("获取里程碑统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建项目里程碑
     */
    @PostMapping
    public Result<String> createProjectMilestone(@RequestBody ProjectMilestone projectMilestone) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectMilestone.getProjectId())) {
                return Result.error("无权限操作该项目里程碑");
            }
            
            projectMilestoneService.createProjectMilestone(projectMilestone);
            return Result.success("里程碑创建成功");
        } catch (Exception e) {
            return Result.error("创建里程碑失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新项目里程碑
     */
    @PutMapping("/{id}")
    public Result<String> updateProjectMilestone(@PathVariable Long id, @RequestBody ProjectMilestone projectMilestone) {
        try {
            // 检查现有记录的项目访问权限
            ProjectMilestone existingMilestone = projectMilestoneService.getById(id);
            if (existingMilestone == null || !hasProjectAccess(existingMilestone.getProjectId())) {
                return Result.error("无权限操作该项目里程碑");
            }
            
            projectMilestone.setId(id);
            projectMilestoneService.updateProjectMilestone(projectMilestone);
            return Result.success("里程碑更新成功");
        } catch (Exception e) {
            return Result.error("更新里程碑失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除项目里程碑
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProjectMilestone(@PathVariable Long id) {
        try {
            // 检查现有记录的项目访问权限
            ProjectMilestone existingMilestone = projectMilestoneService.getById(id);
            if (existingMilestone == null || !hasProjectAccess(existingMilestone.getProjectId())) {
                return Result.error("无权限操作该项目里程碑");
            }
            
            projectMilestoneService.deleteProjectMilestone(id);
            return Result.success("里程碑删除成功");
        } catch (Exception e) {
            return Result.error("删除里程碑失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量保存项目里程碑
     */
    @PostMapping("/project/{projectId}/batch")
    public Result<String> batchSaveProjectMilestones(
            @PathVariable Long projectId, 
            @RequestBody List<ProjectMilestone> milestones) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限操作该项目里程碑");
            }
            
            projectMilestoneService.batchSaveProjectMilestones(projectId, milestones);
            return Result.success("里程碑批量保存成功");
        } catch (Exception e) {
            return Result.error("批量保存里程碑失败: " + e.getMessage());
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
        
        return projectService.canUserAccessProject(currentUserId, projectId);
    }
}