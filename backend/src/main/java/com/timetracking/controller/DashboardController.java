package com.timetracking.controller;

import com.timetracking.service.DashboardService;
import com.timetracking.vo.Result;
import com.timetracking.util.PermissionUtil;
import com.timetracking.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
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
    
    /**
     * 获取用户指定月份的工时统计
     */
    @GetMapping("/user/{userId}/monthly-stats")
    public Result<Map<String, Object>> getUserMonthlyStats(@PathVariable Long userId, 
                                                          @RequestParam Integer year, 
                                                          @RequestParam Integer month) {
        try {
            // 权限检查：只有管理员、部门负责人或用户本人可以查看
            Long currentUserId = PermissionUtil.getCurrentUserId();
            User.UserRole currentUserRole = PermissionUtil.getCurrentUserRole();
            if (!currentUserId.equals(userId) && currentUserRole != User.UserRole.ADMIN && currentUserRole != User.UserRole.DEPARTMENT_MANAGER) {
                return Result.error("无权限查看其他用户的工时统计");
            }
            
            Map<String, Object> monthlyStats = dashboardService.getUserMonthlyStats(userId, year, month);
            return Result.success("获取月度工时统计成功", monthlyStats);
        } catch (Exception e) {
            return Result.error("获取月度工时统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户指定年度的工时统计
     */
    @GetMapping("/user/{userId}/yearly-stats")
    public Result<Map<String, Object>> getUserYearlyStats(@PathVariable Long userId, 
                                                         @RequestParam Integer year) {
        try {
            // 权限检查：只有管理员、部门负责人或用户本人可以查看
            Long currentUserId = PermissionUtil.getCurrentUserId();
            User.UserRole currentUserRole = PermissionUtil.getCurrentUserRole();
            if (!currentUserId.equals(userId) && currentUserRole != User.UserRole.ADMIN && currentUserRole != User.UserRole.DEPARTMENT_MANAGER) {
                return Result.error("无权限查看其他用户的工时统计");
            }
            
            Map<String, Object> yearlyStats = dashboardService.getUserYearlyStats(userId, year);
            return Result.success("获取年度工时统计成功", yearlyStats);
        } catch (Exception e) {
            return Result.error("获取年度工时统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户月度工时趋势（最近12个月）
     */
    @GetMapping("/user/{userId}/monthly-trend")
    public Result<List<Map<String, Object>>> getUserMonthlyTrend(@PathVariable Long userId) {
        try {
            // 权限检查：只有管理员、部门负责人或用户本人可以查看
            Long currentUserId = PermissionUtil.getCurrentUserId();
            User.UserRole currentUserRole = PermissionUtil.getCurrentUserRole();
            if (!currentUserId.equals(userId) && currentUserRole != User.UserRole.ADMIN && currentUserRole != User.UserRole.DEPARTMENT_MANAGER) {
                return Result.error("无权限查看其他用户的工时统计");
            }
            
            List<Map<String, Object>> monthlyTrend = dashboardService.getUserMonthlyTrend(userId);
            return Result.success("获取月度工时趋势成功", monthlyTrend);
        } catch (Exception e) {
            return Result.error("获取月度工时趋势失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户项目工时分布
     */
    @GetMapping("/user/{userId}/project-distribution")
    public Result<List<Map<String, Object>>> getUserProjectHoursDistribution(@PathVariable Long userId, 
                                                                           @RequestParam Integer year, 
                                                                           @RequestParam Integer month) {
        try {
            // 权限检查：只有管理员、部门负责人或用户本人可以查看
            Long currentUserId = PermissionUtil.getCurrentUserId();
            User.UserRole currentUserRole = PermissionUtil.getCurrentUserRole();
            if (!currentUserId.equals(userId) && currentUserRole != User.UserRole.ADMIN && currentUserRole != User.UserRole.DEPARTMENT_MANAGER) {
                return Result.error("无权限查看其他用户的工时统计");
            }
            
            List<Map<String, Object>> projectDistribution = dashboardService.getUserProjectHoursDistribution(userId, year, month);
            return Result.success("获取项目工时分布成功", projectDistribution);
        } catch (Exception e) {
            return Result.error("获取项目工时分布失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有用户的月度工时统计
     */
    @GetMapping("/all-users/monthly-stats")
    public Result<List<Map<String, Object>>> getAllUsersMonthlyStats(@RequestParam Integer year, @RequestParam Integer month) {
        try {
            List<Map<String, Object>> allUsersStats = dashboardService.getAllUsersMonthlyStats(year, month);
            return Result.success("获取所有用户月度工时统计成功", allUsersStats);
        } catch (Exception e) {
            return Result.error("获取所有用户月度工时统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有用户的年度工时统计
     */
    @GetMapping("/all-users/yearly-stats")
    public Result<List<Map<String, Object>>> getAllUsersYearlyStats(@RequestParam Integer year) {
        try {
            List<Map<String, Object>> allUsersStats = dashboardService.getAllUsersYearlyStats(year);
            return Result.success("获取所有用户年度工时统计成功", allUsersStats);
        } catch (Exception e) {
            return Result.error("获取所有用户年度工时统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有用户的季度工时统计
     */
    @GetMapping("/all-users/quarterly-stats")
    public Result<List<Map<String, Object>>> getAllUsersQuarterlyStats(@RequestParam Integer year, @RequestParam Integer quarter) {
        try {
            List<Map<String, Object>> allUsersStats = dashboardService.getAllUsersQuarterlyStats(year, quarter);
            return Result.success("获取所有用户季度工时统计成功", allUsersStats);
        } catch (Exception e) {
            return Result.error("获取所有用户季度工时统计失败: " + e.getMessage());
        }
    }
}