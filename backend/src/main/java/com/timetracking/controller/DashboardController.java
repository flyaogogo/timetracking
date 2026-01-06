package com.timetracking.controller;

import com.timetracking.service.DashboardService;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 工作台控制器
 */
@RestController
@RequestMapping("/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {
    
    @Autowired
    private DashboardService dashboardService;
    
    /**
     * 获取用户工作台数据
     */
    @GetMapping("/user/{userId}")
    public Result<Map<String, Object>> getUserDashboardData(@PathVariable Long userId) {
        try {
            Map<String, Object> dashboardData = dashboardService.getUserDashboardData(userId);
            return Result.success("获取工作台数据成功", dashboardData);
        } catch (Exception e) {
            return Result.error("获取工作台数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户参与的项目列表
     */
    @GetMapping("/user/{userId}/projects")
    public Result<Object> getUserProjects(@PathVariable Long userId) {
        try {
            Object projects = dashboardService.getUserProjects(userId);
            return Result.success("获取用户项目成功", projects);
        } catch (Exception e) {
            return Result.error("获取用户项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的任务统计
     */
    @GetMapping("/user/{userId}/task-stats")
    public Result<Map<String, Object>> getUserTaskStats(@PathVariable Long userId) {
        try {
            Map<String, Object> taskStats = dashboardService.getUserTaskStats(userId);
            return Result.success("获取任务统计成功", taskStats);
        } catch (Exception e) {
            return Result.error("获取任务统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的工时统计
     */
    @GetMapping("/user/{userId}/time-stats")
    public Result<Map<String, Object>> getUserTimeStats(@PathVariable Long userId) {
        try {
            Map<String, Object> timeStats = dashboardService.getUserTimeStats(userId);
            return Result.success("获取工时统计成功", timeStats);
        } catch (Exception e) {
            return Result.error("获取工时统计失败: " + e.getMessage());
        }
    }
}