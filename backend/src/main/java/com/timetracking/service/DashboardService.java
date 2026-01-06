package com.timetracking.service;

import com.timetracking.mapper.DashboardMapper;
import com.timetracking.mapper.ProjectMemberMapper;
import com.timetracking.mapper.TaskMapper;
import com.timetracking.mapper.TimeEntryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台服务
 */
@Service
public class DashboardService {
    
    @Autowired
    private DashboardMapper dashboardMapper;
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private TimeEntryMapper timeEntryMapper;
    
    /**
     * 获取用户工作台数据
     */
    public Map<String, Object> getUserDashboardData(Long userId) {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // 获取用户参与的项目数量
        Integer projectCount = dashboardMapper.getUserProjectCount(userId);
        dashboardData.put("projectCount", projectCount != null ? projectCount : 0);
        
        // 获取用户参与项目中分配给用户的任务数量
        Integer taskCount = dashboardMapper.getUserTaskCount(userId);
        dashboardData.put("taskCount", taskCount != null ? taskCount : 0);
        
        // 获取用户在参与项目上花费的总工时
        Double totalHours = dashboardMapper.getUserTotalHours(userId);
        dashboardData.put("totalHours", totalHours != null ? totalHours : 0.0);
        
        // 获取用户作为项目经理需要审核的工时数量
        Integer pendingCount = dashboardMapper.getUserPendingTimeEntries(userId);
        dashboardData.put("pendingCount", pendingCount != null ? pendingCount : 0);
        
        // 获取用户最近的任务（Top10）
        List<Map<String, Object>> recentTasks = dashboardMapper.getUserRecentTasks(userId, 10);
        dashboardData.put("recentTasks", recentTasks);
        
        // 获取用户最近的工时记录（Top10）
        List<Map<String, Object>> recentTimeEntries = dashboardMapper.getUserRecentTimeEntries(userId, 10);
        dashboardData.put("recentTimeEntries", recentTimeEntries);
        
        // 获取用户最近参与的项目（Top10）
        List<Map<String, Object>> recentProjects = dashboardMapper.getUserRecentProjects(userId, 10);
        dashboardData.put("recentProjects", recentProjects);
        
        // 获取用户作为项目经理需要审核的工时记录（Top10）
        List<Map<String, Object>> pendingApprovals = dashboardMapper.getUserPendingApprovals(userId, 10);
        dashboardData.put("pendingApprovals", pendingApprovals);
        
        return dashboardData;
    }
    
    /**
     * 获取用户参与的项目列表
     */
    public List<Map<String, Object>> getUserProjects(Long userId) {
        return dashboardMapper.getUserProjects(userId);
    }
    
    /**
     * 获取用户的任务统计
     */
    public Map<String, Object> getUserTaskStats(Long userId) {
        Map<String, Object> taskStats = new HashMap<>();
        
        // 按状态统计任务
        Integer todoCount = dashboardMapper.getUserTaskCountByStatus(userId, "TODO");
        Integer inProgressCount = dashboardMapper.getUserTaskCountByStatus(userId, "IN_PROGRESS");
        Integer completedCount = dashboardMapper.getUserTaskCountByStatus(userId, "COMPLETED");
        Integer reviewCount = dashboardMapper.getUserTaskCountByStatus(userId, "REVIEW");
        
        taskStats.put("todoCount", todoCount != null ? todoCount : 0);
        taskStats.put("inProgressCount", inProgressCount != null ? inProgressCount : 0);
        taskStats.put("completedCount", completedCount != null ? completedCount : 0);
        taskStats.put("reviewCount", reviewCount != null ? reviewCount : 0);
        
        return taskStats;
    }
    
    /**
     * 获取用户的工时统计
     */
    public Map<String, Object> getUserTimeStats(Long userId) {
        Map<String, Object> timeStats = new HashMap<>();
        
        // 本周工时
        Double thisWeekHours = dashboardMapper.getUserThisWeekHours(userId);
        timeStats.put("thisWeekHours", thisWeekHours != null ? thisWeekHours : 0.0);
        
        // 本月工时
        Double thisMonthHours = dashboardMapper.getUserThisMonthHours(userId);
        timeStats.put("thisMonthHours", thisMonthHours != null ? thisMonthHours : 0.0);
        
        // 平均日工时
        Double avgDailyHours = dashboardMapper.getUserAvgDailyHours(userId);
        timeStats.put("avgDailyHours", avgDailyHours != null ? avgDailyHours : 0.0);
        
        return timeStats;
    }
}