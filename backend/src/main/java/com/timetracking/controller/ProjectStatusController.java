package com.timetracking.controller;

import com.timetracking.entity.Project;
import com.timetracking.service.ProjectService;
import com.timetracking.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 项目状态管理控制器
 */
@RestController
@RequestMapping("/api/project-status")
public class ProjectStatusController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectStatusController.class);
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 获取项目状态说明
     */
    @GetMapping("/definitions")
    public Result<Map<String, Object>> getProjectStatusDefinitions() {
        Map<String, Object> definitions = new HashMap<>();
        
        // 项目状态定义
        Map<String, Object> statusDefinitions = new HashMap<>();
        statusDefinitions.put("PLANNING", createStatusInfo("规划中", "项目正在进行需求分析、设计和计划制定", "#909399", new String[]{"IN_PROGRESS", "CANCELLED"}));
        statusDefinitions.put("IN_PROGRESS", createStatusInfo("进行中", "项目正在开发实施阶段", "#E6A23C", new String[]{"COMPLETED", "PAUSED", "CANCELLED"}));
        statusDefinitions.put("COMPLETED", createStatusInfo("已完成", "项目已成功交付并验收", "#67C23A", new String[]{}));
        statusDefinitions.put("PAUSED", createStatusInfo("已暂停", "项目因各种原因暂时停止", "#F56C6C", new String[]{"IN_PROGRESS", "CANCELLED"}));
        statusDefinitions.put("CANCELLED", createStatusInfo("已取消", "项目被永久取消", "#909399", new String[]{}));
        
        definitions.put("statusDefinitions", statusDefinitions);
        
        // 状态转换规则
        Map<String, String[]> transitionRules = new HashMap<>();
        transitionRules.put("PLANNING", new String[]{"IN_PROGRESS", "CANCELLED"});
        transitionRules.put("IN_PROGRESS", new String[]{"COMPLETED", "PAUSED", "CANCELLED"});
        transitionRules.put("COMPLETED", new String[]{});
        transitionRules.put("PAUSED", new String[]{"IN_PROGRESS", "CANCELLED"});
        transitionRules.put("CANCELLED", new String[]{});
        
        definitions.put("transitionRules", transitionRules);
        
        // 状态变更权限
        Map<String, String[]> permissionRules = new HashMap<>();
        permissionRules.put("ADMIN", new String[]{"PLANNING", "IN_PROGRESS", "COMPLETED", "PAUSED", "CANCELLED"});
        permissionRules.put("PROJECT_MANAGER", new String[]{"PLANNING", "IN_PROGRESS", "COMPLETED", "PAUSED"});
        permissionRules.put("DEVELOPER", new String[]{});
        
        definitions.put("permissionRules", permissionRules);
        
        return Result.success("获取项目状态定义成功", definitions);
    }
    
    /**
     * 更改项目状态
     */
    @PostMapping("/{projectId}/change-status")
    public Result<String> changeProjectStatus(
            @PathVariable Long projectId,
            @RequestParam String newStatus,
            @RequestParam(required = false) String reason) {
        
        logger.info("更改项目状态，项目ID: {}, 新状态: {}, 原因: {}", projectId, newStatus, reason);
        
        try {
            // 验证状态值
            Project.ProjectStatus status;
            try {
                status = Project.ProjectStatus.valueOf(newStatus);
            } catch (IllegalArgumentException e) {
                return Result.error("无效的项目状态: " + newStatus);
            }
            
            // 获取当前项目
            Project project = projectService.getById(projectId);
            if (project == null) {
                return Result.error("项目不存在");
            }
            
            // 验证状态转换是否合法
            if (!isValidStatusTransition(project.getStatus(), status)) {
                return Result.error("不允许从 " + getStatusText(project.getStatus()) + " 转换到 " + getStatusText(status));
            }
            
            // 更新项目状态
            project.setStatus(status);
            boolean success = projectService.updateById(project);
            
            if (success) {
                logger.info("项目状态更新成功，项目ID: {}, 新状态: {}", projectId, newStatus);
                return Result.success("项目状态更新成功");
            } else {
                return Result.error("项目状态更新失败");
            }
            
        } catch (Exception e) {
            logger.error("更改项目状态失败", e);
            return Result.error("更改项目状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目状态历史
     */
    @GetMapping("/{projectId}/history")
    public Result<Map<String, Object>> getProjectStatusHistory(@PathVariable Long projectId) {
        // 这里可以实现状态变更历史记录功能
        // 目前返回当前状态信息
        try {
            Project project = projectService.getById(projectId);
            if (project == null) {
                return Result.error("项目不存在");
            }
            
            Map<String, Object> history = new HashMap<>();
            history.put("currentStatus", project.getStatus().name());
            history.put("currentStatusText", getStatusText(project.getStatus()));
            history.put("projectName", project.getProjectName());
            
            return Result.success("获取项目状态历史成功", history);
        } catch (Exception e) {
            logger.error("获取项目状态历史失败", e);
            return Result.error("获取项目状态历史失败");
        }
    }
    
    /**
     * 创建状态信息对象
     */
    private Map<String, Object> createStatusInfo(String text, String description, String color, String[] allowedTransitions) {
        Map<String, Object> info = new HashMap<>();
        info.put("text", text);
        info.put("description", description);
        info.put("color", color);
        info.put("allowedTransitions", allowedTransitions);
        return info;
    }
    
    /**
     * 验证状态转换是否合法
     */
    private boolean isValidStatusTransition(Project.ProjectStatus currentStatus, Project.ProjectStatus newStatus) {
        if (currentStatus == newStatus) {
            return true; // 相同状态允许
        }
        
        switch (currentStatus) {
            case PLANNING:
                return newStatus == Project.ProjectStatus.IN_PROGRESS || newStatus == Project.ProjectStatus.CANCELLED;
            case IN_PROGRESS:
                return newStatus == Project.ProjectStatus.COMPLETED || 
                       newStatus == Project.ProjectStatus.PAUSED || 
                       newStatus == Project.ProjectStatus.CANCELLED;
            case PAUSED:
                return newStatus == Project.ProjectStatus.IN_PROGRESS || newStatus == Project.ProjectStatus.CANCELLED;
            case COMPLETED:
            case CANCELLED:
                return false; // 已完成和已取消的项目不允许状态变更
            default:
                return false;
        }
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(Project.ProjectStatus status) {
        switch (status) {
            case PLANNING: return "规划中";
            case IN_PROGRESS: return "进行中";
            case COMPLETED: return "已完成";
            case PAUSED: return "已暂停";
            case CANCELLED: return "已取消";
            default: return status.name();
        }
    }
}