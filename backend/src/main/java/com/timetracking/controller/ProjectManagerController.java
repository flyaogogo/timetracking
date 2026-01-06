package com.timetracking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.timetracking.entity.Project;
import com.timetracking.entity.TimeEntry;
import com.timetracking.entity.Task;
import com.timetracking.service.ProjectService;
import com.timetracking.service.TimeEntryService;
import com.timetracking.service.TaskService;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * 项目经理专用控制器
 */
@RestController
@RequestMapping("/project-manager")
@CrossOrigin
public class ProjectManagerController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private TimeEntryService timeEntryService;
    
    @Autowired
    private TaskService taskService;
    
    /**
     * 检查当前用户的项目经理权限
     */
    @GetMapping("/check-permission")
    public Result checkPermission() {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> permissionInfo = new HashMap<>();
            
            // 检查全局项目经理权限
            boolean isGlobalProjectManager = PermissionUtil.getCurrentUserRole() == com.timetracking.entity.User.UserRole.PROJECT_MANAGER;
            permissionInfo.put("isGlobalProjectManager", isGlobalProjectManager);
            
            // 检查项目级别项目经理权限
            boolean isProjectLevelManager = false;
            int managedProjectsCount = 0;
            try {
                IPage<Project> managedProjects = projectService.getManagedProjects(1, 1000, currentUserId);
                managedProjectsCount = (int) managedProjects.getTotal();
                isProjectLevelManager = managedProjectsCount > 0;
            } catch (Exception e) {
                // 忽略查询错误，继续检查
            }
            
            permissionInfo.put("isProjectLevelManager", isProjectLevelManager);
            permissionInfo.put("managedProjectsCount", managedProjectsCount);
            
            // 最终项目经理权限判断：只有真正管理项目的用户才有权限
            boolean isProjectManager = isProjectLevelManager;
            permissionInfo.put("isProjectManager", isProjectManager);
            
            return Result.success("权限检查成功", permissionInfo);
        } catch (Exception e) {
            return Result.error("权限检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目经理工作台数据
     */
    @GetMapping("/dashboard")
    public Result getDashboard() {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> dashboard = new HashMap<>();
            
            // 获取管理的项目数量
            IPage<Project> managedProjects = projectService.getManagedProjects(1, 1000, currentUserId);
            dashboard.put("managedProjectsCount", managedProjects.getTotal());
            dashboard.put("managedProjects", managedProjects.getRecords());
            
            // 获取待审批工时数量
            IPage<TimeEntry> pendingApprovals = timeEntryService.getMyPendingApprovals(1, 1000, currentUserId);
            dashboard.put("pendingApprovalsCount", pendingApprovals.getTotal());
            dashboard.put("pendingApprovals", pendingApprovals.getRecords());
            
            // 获取项目任务统计
            Map<String, Object> taskStats = taskService.getProjectManagerTaskStats(currentUserId);
            dashboard.put("taskStats", taskStats);
            
            return Result.success("获取项目经理工作台数据成功", dashboard);
        } catch (Exception e) {
            return Result.error("获取项目经理工作台数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取管理的项目列表
     */
    @GetMapping("/projects")
    public Result getManagedProjects(@RequestParam(defaultValue = "1") int current,
                                    @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            IPage<Project> page = projectService.getManagedProjects(current, size, currentUserId);
            return Result.success("获取管理项目列表成功", page);
        } catch (Exception e) {
            return Result.error("获取管理项目列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户管理的项目ID列表（用于权限检查）
     */
    @GetMapping("/managed-projects")
    public Result getManagedProjectIds() {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            IPage<Project> page = projectService.getManagedProjects(1, 1000, currentUserId);
            List<Project> projects = page.getRecords();
            
            return Result.success("获取管理项目ID列表成功", projects);
        } catch (Exception e) {
            return Result.error("获取管理项目ID列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取待审批的工时记录
     */
    @GetMapping("/pending-approvals")
    public Result getPendingApprovals(@RequestParam(defaultValue = "1") int current,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) Long projectId) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            IPage<TimeEntry> page;
            if (projectId != null) {
                // 检查项目管理权限
                if (!EnhancedPermissionUtil.canApproveTimesheet(currentUserId, projectId)) {
                    return Result.error("无权限审批该项目的工时记录");
                }
                page = timeEntryService.getProjectPendingApprovals(current, size, projectId);
            } else {
                page = timeEntryService.getMyPendingApprovals(current, size, currentUserId);
            }
            
            return Result.success("获取待审批工时记录成功", page);
        } catch (Exception e) {
            return Result.error("获取待审批工时记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目任务概览
     */
    @GetMapping("/tasks-overview")
    public Result getTasksOverview(@RequestParam(required = false) Long projectId) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            Map<String, Object> overview;
            if (projectId != null) {
                // 检查项目管理权限
                if (!EnhancedPermissionUtil.canManageTasks(currentUserId, projectId)) {
                    return Result.error("无权限查看该项目的任务");
                }
                overview = taskService.getProjectTasksOverview(projectId);
            } else {
                overview = taskService.getProjectManagerTasksOverview(currentUserId);
            }
            
            return Result.success("获取任务概览成功", overview);
        } catch (Exception e) {
            return Result.error("获取任务概览失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目成员绩效统计
     */
    @GetMapping("/team-performance")
    public Result getTeamPerformance(@RequestParam Long projectId,
                                    @RequestParam(required = false) String period) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            // 检查项目管理权限
            if (!EnhancedPermissionUtil.canViewReports(currentUserId, projectId)) {
                return Result.error("无权限查看该项目的团队绩效");
            }
            
            Map<String, Object> performance = timeEntryService.getTeamPerformance(projectId, period);
            return Result.success("获取团队绩效统计成功", performance);
        } catch (Exception e) {
            return Result.error("获取团队绩效统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量审批工时记录
     */
    @PostMapping("/batch-approve")
    public Result batchApproveTimeEntries(@RequestBody Map<String, Object> request) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Long> ids = (java.util.List<Long>) request.get("ids");
            String comment = (String) request.get("comment");
            
            // 检查每个工时记录的审批权限
            for (Long id : ids) {
                TimeEntry timeEntry = timeEntryService.getById(id);
                if (timeEntry == null) {
                    return Result.error("工时记录不存在: " + id);
                }
                
                if (!EnhancedPermissionUtil.canApproveTimesheet(currentUserId, timeEntry.getProjectId())) {
                    return Result.error("无权限审批工时记录: " + id);
                }
            }
            
            timeEntryService.batchApproveTimeEntries(ids, currentUserId, comment);
            return Result.success("批量审批工时记录成功");
        } catch (Exception e) {
            return Result.error("批量审批工时记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目权限摘要
     */
    @GetMapping("/permissions/{projectId}")
    public Result getProjectPermissions(@PathVariable Long projectId) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            EnhancedPermissionUtil.ProjectPermissionSummary summary = 
                EnhancedPermissionUtil.getCurrentUserProjectPermissions(projectId);
            
            return Result.success("获取项目权限摘要成功", summary);
        } catch (Exception e) {
            return Result.error("获取项目权限摘要失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成项目报表
     */
    @GetMapping("/reports")
    public Result generateProjectReport(@RequestParam Long projectId,
                                       @RequestParam(required = false) String startDate,
                                       @RequestParam(required = false) String endDate) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            // 检查项目管理权限
            if (!EnhancedPermissionUtil.canViewReports(currentUserId, projectId)) {
                return Result.error("无权限查看该项目的报表");
            }
            
            Map<String, Object> report = new HashMap<>();
            
            // 获取项目基本信息
            Project project = projectService.getById(projectId);
            if (project == null) {
                return Result.error("项目不存在");
            }
            report.put("project", project);
            
            // 获取项目统计数据
            Map<String, Object> overview = new HashMap<>();
            
            // 任务统计
            Map<String, Object> taskStats = taskService.getProjectTasksOverview(projectId);
            overview.put("taskStats", taskStats);
            
            // 工时统计
            Map<String, Object> timeStats = timeEntryService.getProjectTimeStats(projectId, startDate, endDate);
            overview.put("timeStats", timeStats);
            
            // 团队绩效
            Map<String, Object> teamPerformance = timeEntryService.getTeamPerformance(projectId, "month");
            overview.put("teamPerformance", teamPerformance);
            
            // 项目进度
            Map<String, Object> progressStats = new HashMap<>();
            if (taskStats != null) {
                // 处理可能的类型转换问题
                Object totalTasksObj = taskStats.get("totalTasks");
                Object completedTasksObj = taskStats.get("completedTasks");
                
                long totalTasks = totalTasksObj != null ? Long.parseLong(totalTasksObj.toString()) : 0;
                long completedTasks = completedTasksObj != null ? Long.parseLong(completedTasksObj.toString()) : 0;
                
                if (totalTasks > 0) {
                    double progress = completedTasks * 100.0 / totalTasks;
                    progressStats.put("progressPercentage", Math.round(progress * 100.0) / 100.0);
                } else {
                    progressStats.put("progressPercentage", 0.0);
                }
                progressStats.put("totalTasks", totalTasks);
                progressStats.put("completedTasks", completedTasks);
            }
            overview.put("progressStats", progressStats);
            
            // 成本统计
            Map<String, Object> costStats = new HashMap<>();
            costStats.put("budgetAmount", project.getBudgetAmount());
            costStats.put("actualCost", project.getActualLaborCost().add(project.getActualOtherCost()));
            costStats.put("laborCost", project.getActualLaborCost());
            costStats.put("otherCost", project.getActualOtherCost());
            overview.put("costStats", costStats);
            
            report.put("overview", overview);
            report.put("reportDate", java.time.LocalDateTime.now());
            
            Map<String, String> dateRangeMap = new HashMap<>();
            dateRangeMap.put("startDate", startDate);
            dateRangeMap.put("endDate", endDate);
            report.put("dateRange", dateRangeMap);
            
            return Result.success("生成项目报表成功", report);
        } catch (Exception e) {
            return Result.error("生成项目报表失败: " + e.getMessage());
        }
    }
}