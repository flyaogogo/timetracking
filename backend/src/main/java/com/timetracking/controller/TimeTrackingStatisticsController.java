package com.timetracking.controller;

import com.timetracking.service.TimeTrackingStatisticsService;
import com.timetracking.entity.Task;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 工时统计控制器
 */
@RestController
@RequestMapping("/time-tracking-statistics")
public class TimeTrackingStatisticsController {
    
    @Autowired
    private TimeTrackingStatisticsService statisticsService;
    
    /**
     * 更新任务的实际工时和进度
     */
    @PostMapping("/tasks/{taskId}/update-hours")
    public Result<Task> updateTaskActualHours(@PathVariable Long taskId) {
        try {
            Task updatedTask = statisticsService.updateTaskActualHoursAndProgress(taskId);
            if (updatedTask != null) {
                return Result.success("更新成功", updatedTask);
            } else {
                return Result.error("任务不存在");
            }
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新项目的实际工时汇总
     */
    @PostMapping("/projects/{projectId}/update-hours")
    public Result<String> updateProjectActualHours(@PathVariable Long projectId) {
        try {
            statisticsService.updateProjectActualHours(projectId);
            return Result.success("项目工时汇总更新成功");
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新所有任务的实际工时和进度
     */
    @PostMapping("/tasks/update-all-hours")
    public Result<String> updateAllTasksActualHours() {
        try {
            statisticsService.updateAllTasksActualHours();
            return Result.success("所有任务工时更新成功");
        } catch (Exception e) {
            return Result.error("批量更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量更新所有项目的实际工时汇总
     */
    @PostMapping("/projects/update-all-hours")
    public Result<String> updateAllProjectsActualHours() {
        try {
            statisticsService.updateAllProjectsActualHours();
            return Result.success("所有项目工时汇总更新成功");
        } catch (Exception e) {
            return Result.error("批量更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取任务工时统计详情
     */
    @GetMapping("/tasks/{taskId}/statistics")
    public Result<Map<String, Object>> getTaskTimeStatistics(@PathVariable Long taskId) {
        try {
            Map<String, Object> statistics = statisticsService.getTaskTimeStatistics(taskId);
            return Result.success("获取成功", statistics);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目工时统计详情
     */
    @GetMapping("/projects/{projectId}/statistics")
    public Result<Map<String, Object>> getProjectTimeStatistics(@PathVariable Long projectId) {
        try {
            Map<String, Object> statistics = statisticsService.getProjectTimeStatistics(projectId);
            return Result.success("获取成功", statistics);
        } catch (Exception e) {
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}