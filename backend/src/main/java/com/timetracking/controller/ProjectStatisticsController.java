package com.timetracking.controller;

import com.timetracking.service.ProjectStatisticsService;
import com.timetracking.service.ProjectService;
import com.timetracking.util.PermissionUtil;
import com.timetracking.vo.ProjectStatisticsVO;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/project-statistics")
@CrossOrigin
public class ProjectStatisticsController {
    
    @Autowired
    private ProjectStatisticsService projectStatisticsService;
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 获取项目完整统计分析
     */
    @GetMapping("/{projectId}")
    public Result getProjectStatistics(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目统计");
            }
            
            ProjectStatisticsVO statistics = projectStatisticsService.getProjectStatistics(projectId);
            if (statistics == null) {
                return Result.error("项目不存在");
            }
            return Result.success("获取项目统计成功", statistics);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取项目统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取项目进度统计
     */
    @GetMapping("/{projectId}/progress")
    public Result getProgressStats(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目统计");
            }
            
            ProjectStatisticsVO statistics = projectStatisticsService.getProjectStatistics(projectId);
            if (statistics == null) {
                return Result.error("项目不存在");
            }
            return Result.success("获取进度统计成功", statistics.getProgressStats());
        } catch (Exception e) {
            return Result.error("获取进度统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取项目财务统计
     */
    @GetMapping("/{projectId}/financial")
    public Result getFinancialStats(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目统计");
            }
            
            ProjectStatisticsVO statistics = projectStatisticsService.getProjectStatistics(projectId);
            if (statistics == null) {
                return Result.error("项目不存在");
            }
            return Result.success("获取财务统计成功", statistics.getFinancialStats());
        } catch (Exception e) {
            return Result.error("获取财务统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取项目工期统计
     */
    @GetMapping("/{projectId}/schedule")
    public Result getScheduleStats(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目统计");
            }
            
            ProjectStatisticsVO statistics = projectStatisticsService.getProjectStatistics(projectId);
            if (statistics == null) {
                return Result.error("项目不存在");
            }
            return Result.success("获取工期统计成功", statistics.getScheduleStats());
        } catch (Exception e) {
            return Result.error("获取工期统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取项目团队统计
     */
    @GetMapping("/{projectId}/team")
    public Result getTeamStats(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目统计");
            }
            
            ProjectStatisticsVO statistics = projectStatisticsService.getProjectStatistics(projectId);
            if (statistics == null) {
                return Result.error("项目不存在");
            }
            return Result.success("获取团队统计成功", statistics.getTeamStats());
        } catch (Exception e) {
            return Result.error("获取团队统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取项目质量统计
     */
    @GetMapping("/{projectId}/quality")
    public Result getQualityStats(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目统计");
            }
            
            ProjectStatisticsVO statistics = projectStatisticsService.getProjectStatistics(projectId);
            if (statistics == null) {
                return Result.error("项目不存在");
            }
            return Result.success("获取质量统计成功", statistics.getQualityStats());
        } catch (Exception e) {
            return Result.error("获取质量统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取项目报表摘要信息
     * 用于项目报表查看页面的快速统计展示
     */
    @GetMapping("/{projectId}/summary")
    public Result getProjectSummary(@PathVariable Long projectId) {
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目统计");
            }
            
            ProjectStatisticsVO statistics = projectStatisticsService.getProjectStatistics(projectId);
            if (statistics == null) {
                return Result.error("项目不存在");
            }
            
            ProjectStatisticsVO.ProgressStats progressStats = statistics.getProgressStats();
            
            // 构建报表摘要数据
            Map<String, Object> summary = new HashMap<>();
            summary.put("totalTasks", progressStats.getTotalTasks());
            summary.put("completedTasks", progressStats.getCompletedTasks());
            summary.put("totalHours", progressStats.getActualHours());
            summary.put("completionRate", progressStats.getProgressPercentage().intValue());
            
            return Result.success("获取项目报表摘要成功", summary);
        } catch (Exception e) {
            return Result.error("获取项目报表摘要失败：" + e.getMessage());
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