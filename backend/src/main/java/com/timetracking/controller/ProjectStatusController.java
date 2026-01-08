package com.timetracking.controller;

import com.timetracking.entity.Project;
import com.timetracking.entity.ProjectStatusHistory;
import com.timetracking.service.ProjectService;
import com.timetracking.service.ProjectStatusHistoryService;
import com.timetracking.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目状态管理控制器
 */
@RestController
@RequestMapping("/project-status")
public class ProjectStatusController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectStatusController.class);
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private ProjectStatusHistoryService projectStatusHistoryService;
    
    /**
     * 获取项目状态说明
     */
    @GetMapping("/definitions")
    public Result<Map<String, Object>> getProjectStatusDefinitions() {
        Map<String, Object> definitions = new HashMap<>();
        
        // 项目状态定义 - 更详细的状态描述
        Map<String, Object> statusDefinitions = new HashMap<>();
        statusDefinitions.put("PLANNING", createDetailedStatusInfo(
            "规划中", 
            "项目处于初始规划阶段，包括需求分析、系统设计、资源规划和进度安排等工作。", 
            "项目团队正在制定详细的项目计划，尚未开始实际开发工作。",
            "适合新启动的项目，或者正在进行前期准备的项目。",
            "#909399", 
            new String[]{"IN_PROGRESS", "CANCELLED"}
        ));
        statusDefinitions.put("IN_PROGRESS", createDetailedStatusInfo(
            "进行中", 
            "项目正在按照计划进行开发实施，团队成员正在执行各项任务。", 
            "项目已正式启动，团队成员正在进行开发、测试、集成等工作。",
            "适合已经开始实施，且有实际工作进展的项目。",
            "#E6A23C", 
            new String[]{"COMPLETED", "PAUSED", "CANCELLED"}
        ));
        statusDefinitions.put("COMPLETED", createDetailedStatusInfo(
            "已完成", 
            "项目已成功交付并通过验收，所有工作已完成。", 
            "项目的所有目标都已实现，交付物已验收通过，项目正式结束。",
            "适合已经完成所有工作并通过验收的项目。",
            "#67C23A", 
            new String[]{}
        ));
        statusDefinitions.put("PAUSED", createDetailedStatusInfo(
            "已暂停", 
            "项目因各种原因暂时停止，等待问题解决后可以恢复。", 
            "项目遇到了暂时的障碍，如资源不足、需求变更、技术难题等，需要暂停等待解决。",
            "适合遇到临时问题需要暂停的项目。",
            "#F56C6C", 
            new String[]{"IN_PROGRESS", "CANCELLED"}
        ));
        statusDefinitions.put("CANCELLED", createDetailedStatusInfo(
            "已取消", 
            "项目被永久取消，不会再继续进行。", 
            "项目因各种原因被终止，如业务需求变化、资源枯竭、技术不可行等。",
            "适合确认不再继续进行的项目。",
            "#909399", 
            new String[]{}
        ));
        
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
            
            // 保存旧状态
            Project.ProjectStatus oldStatus = project.getStatus();
            
            // 更新项目状态
            project.setStatus(status);
            boolean success = projectService.updateById(project);
            
            if (success) {
                // 保存状态变更历史
                ProjectStatusHistory history = new ProjectStatusHistory();
                history.setProjectId(projectId);
                history.setOldStatus(oldStatus.name());
                history.setNewStatus(status.name());
                history.setChangeReason(reason);
                history.setChangedBy("admin"); // 这里应该从JWT中获取当前登录用户
                history.setChangedTime(new java.util.Date());
                projectStatusHistoryService.saveStatusHistory(history);
                
                logger.info("项目状态更新成功，项目ID: {}, 旧状态: {}, 新状态: {}", projectId, oldStatus, newStatus);
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
        try {
            Project project = projectService.getById(projectId);
            if (project == null) {
                return Result.error("项目不存在");
            }
            
            // 获取状态变更历史列表
            List<ProjectStatusHistory> historyList = projectStatusHistoryService.getByProjectId(projectId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("projectId", projectId);
            result.put("projectName", project.getProjectName());
            result.put("currentStatus", project.getStatus().name());
            result.put("currentStatusText", getStatusText(project.getStatus()));
            result.put("historyList", historyList);
            result.put("totalCount", historyList.size());
            
            return Result.success("获取项目状态历史成功", result);
        } catch (Exception e) {
            logger.error("获取项目状态历史失败", e);
            return Result.error("获取项目状态历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建详细状态信息对象
     * 包含：状态文本、简短描述、详细说明、适用场景、颜色、允许的转换
     */
    private Map<String, Object> createDetailedStatusInfo(String text, String shortDesc, String longDesc, String applicable, String color, String[] allowedTransitions) {
        Map<String, Object> info = new HashMap<>();
        info.put("text", text);
        info.put("shortDescription", shortDesc);
        info.put("longDescription", longDesc);
        info.put("applicableScenarios", applicable);
        info.put("color", color);
        info.put("allowedTransitions", allowedTransitions);
        return info;
    }
    
    /**
     * 创建简洁状态信息对象（兼容旧接口）
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