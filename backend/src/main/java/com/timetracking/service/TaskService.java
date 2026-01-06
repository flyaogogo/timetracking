package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timetracking.entity.Task;
import com.timetracking.mapper.TaskMapper;
import com.timetracking.mapper.ProjectMapper;
import com.timetracking.mapper.ProjectMemberMapper;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    private TimeTrackingStatisticsService statisticsService;
    
    public IPage<Task> getTaskList(int current, int size, Long projectId, String keyword) {
        Page<Task> page = new Page<>(current, size);
        
        // 获取当前用户信息
        Long currentUserId = PermissionUtil.getCurrentUserId();
        boolean canViewAll = PermissionUtil.canViewAllProjects();
        
        if (canViewAll) {
            // 管理员可以查看所有任务
            if (projectId != null) {
                // 管理员查看指定项目的任务
                return baseMapper.selectTasksByProjectAndKeyword(page, projectId, keyword);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                // 管理员按关键词搜索所有任务
                return baseMapper.selectTasksWithDetailsAndKeyword(page, keyword);
            } else {
                // 管理员查看所有任务
                return baseMapper.selectTasksWithDetails(page);
            }
        } else {
            // 非管理员用户只能查看参与项目中分配给自己的任务
            if (projectId != null) {
                // 检查项目访问权限
                if (!hasProjectAccess(projectId)) {
                    return new Page<>(current, size); // 返回空页面
                }
                // 查看指定项目中分配给自己的任务
                return baseMapper.selectUserRelatedTasksByProject(page, currentUserId, projectId, keyword);
            } else {
                // 查看所有参与项目中分配给自己的任务
                return baseMapper.selectUserRelatedTasks(page, currentUserId, keyword);
            }
        }
    }
    
    /**
     * 根据用户权限获取任务列表
     */
    public IPage<Task> getTaskListByPermission(int current, int size, Long projectId, String keyword, Long userId) {
        Page<Task> page = new Page<>(current, size);
        
        if (userId == null) {
            return page; // 返回空页面
        }
        
        // 管理员可以查看所有任务
        if (PermissionUtil.isAdmin()) {
            if (projectId != null) {
                return baseMapper.selectTasksByProjectAndKeyword(page, projectId, keyword);
            } else {
                return baseMapper.selectTasksWithDetailsAndKeyword(page, keyword);
            }
        }
        
        // 其他用户只能查看参与项目的任务
        if (projectId != null) {
            return baseMapper.selectUserRelatedTasksByProject(page, userId, projectId, keyword);
        } else {
            return baseMapper.selectUserRelatedTasks(page, userId, keyword);
        }
    }
    
    public Task getTaskWithDetails(Long id) {
        Task task = baseMapper.selectTaskWithDetails(id);
        if (task == null) {
            return null;
        }
        
        // 检查任务访问权限
        if (!hasTaskAccess(task)) {
            return null;
        }
        
        return task;
    }
    
    public IPage<Task> getMyTasks(int current, int size, Long userId) {
        // 检查是否有权限查看指定用户的任务
        if (!PermissionUtil.canViewUserData(userId)) {
            return new Page<>(current, size); // 返回空页面
        }
        
        Page<Task> page = new Page<>(current, size);
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("assignee_id", userId);
        wrapper.orderByDesc("create_time");
        return page(page, wrapper);
    }
    
    public IPage<Task> getUserRelatedTasks(int current, int size, Long userId, String keyword) {
        // 检查是否有权限查看指定用户的任务
        if (!PermissionUtil.canViewUserData(userId)) {
            return new Page<>(current, size); // 返回空页面
        }
        
        Page<Task> page = new Page<>(current, size);
        return baseMapper.selectUserRelatedTasks(page, userId, keyword);
    }
    
    /**
     * 创建任务
     */
    public Task createTask(Task task) {
        save(task);
        
        // 创建任务后，更新项目工时汇总
        if (task.getProjectId() != null) {
            statisticsService.updateProjectActualHours(task.getProjectId());
        }
        
        return task;
    }
    
    /**
     * 更新任务
     */
    public Task updateTask(Task task) {
        updateById(task);
        
        // 更新任务后，重新计算实际工时和进度
        if (task.getId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(task.getId());
        }
        
        return getById(task.getId());
    }
    
    /**
     * 删除任务
     */
    public void deleteTask(Long id) {
        Task task = getById(id);
        removeById(id);
        
        // 删除任务后，更新项目工时汇总
        if (task != null && task.getProjectId() != null) {
            statisticsService.updateProjectActualHours(task.getProjectId());
        }
    }
    
    /**
     * 更新任务状态
     */
    public void updateTaskStatus(Long id, String status) {
        Task task = new Task();
        task.setId(id);
        
        // Convert string status to enum
        Task.TaskStatus taskStatus;
        switch (status.toUpperCase()) {
            case "TODO":
                taskStatus = Task.TaskStatus.TODO;
                break;
            case "IN_PROGRESS":
                taskStatus = Task.TaskStatus.IN_PROGRESS;
                break;
            case "REVIEW":
                taskStatus = Task.TaskStatus.REVIEW;
                break;
            case "COMPLETED":
                taskStatus = Task.TaskStatus.COMPLETED;
                break;
            case "PAUSED":
                taskStatus = Task.TaskStatus.PAUSED;
                break;
            case "CANCELLED":
                taskStatus = Task.TaskStatus.CANCELLED;
                break;
            default:
                taskStatus = Task.TaskStatus.TODO;
                break;
        }
        
        task.setStatus(taskStatus);
        updateById(task);
        
        // 状态更新后，重新计算实际工时和进度
        statisticsService.updateTaskActualHoursAndProgress(id);
    }
    
    /**
     * 检查用户是否为项目成员
     */
    public boolean isProjectMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        
        try {
            return projectMemberMapper.selectByProjectAndUser(projectId, userId) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取项目经理任务统计 - 只统计用户担任项目经理的项目的任务
     */
    public Map<String, Object> getProjectManagerTaskStats(Long managerId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (managerId == null) {
            stats.put("totalTasks", 0);
            stats.put("completedTasks", 0);
            stats.put("pendingTasks", 0);
            stats.put("inProgressTasks", 0);
            return stats;
        }
        
        try {
            // 获取用户担任项目经理的项目的任务统计
            Map<String, Object> taskStats = baseMapper.selectProjectManagerTaskStats(managerId);
            
            int totalTasks = taskStats.get("totalTasks") != null ? 
                ((Number) taskStats.get("totalTasks")).intValue() : 0;
            int completedTasks = taskStats.get("completedTasks") != null ? 
                ((Number) taskStats.get("completedTasks")).intValue() : 0;
            int inProgressTasks = taskStats.get("inProgressTasks") != null ? 
                ((Number) taskStats.get("inProgressTasks")).intValue() : 0;
            int pendingTasks = totalTasks - completedTasks - inProgressTasks;
            
            stats.put("totalTasks", totalTasks);
            stats.put("completedTasks", completedTasks);
            stats.put("inProgressTasks", inProgressTasks);
            stats.put("pendingTasks", Math.max(0, pendingTasks));
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            stats.put("totalTasks", 0);
            stats.put("completedTasks", 0);
            stats.put("pendingTasks", 0);
            stats.put("inProgressTasks", 0);
        }
        
        return stats;
    }
    
    /**
     * 获取项目任务概览
     */
    public Map<String, Object> getProjectTasksOverview(Long projectId) {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            QueryWrapper<Task> wrapper = new QueryWrapper<>();
            wrapper.eq("project_id", projectId);
            
            // 总任务数
            int totalTasks = Math.toIntExact(count(wrapper));
            overview.put("totalTasks", totalTasks);
            
            // 已完成任务数
            QueryWrapper<Task> completedWrapper = new QueryWrapper<>();
            completedWrapper.eq("project_id", projectId).eq("status", "COMPLETED");
            int completedTasks = Math.toIntExact(count(completedWrapper));
            overview.put("completedTasks", completedTasks);
            
            // 进行中任务数
            QueryWrapper<Task> inProgressWrapper = new QueryWrapper<>();
            inProgressWrapper.eq("project_id", projectId).eq("status", "IN_PROGRESS");
            int inProgressTasks = Math.toIntExact(count(inProgressWrapper));
            overview.put("inProgressTasks", inProgressTasks);
            
            // 待开始任务数
            QueryWrapper<Task> todoWrapper = new QueryWrapper<>();
            todoWrapper.eq("project_id", projectId).eq("status", "TODO");
            int todoTasks = Math.toIntExact(count(todoWrapper));
            overview.put("todoTasks", todoTasks);
            
            // 计算完成率
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
            overview.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            
            // 获取项目工时统计
            Map<String, Object> timeStats = statisticsService.getProjectTimeStatistics(projectId);
            overview.putAll(timeStats);
            
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            overview.put("totalTasks", 0);
            overview.put("completedTasks", 0);
            overview.put("inProgressTasks", 0);
            overview.put("todoTasks", 0);
            overview.put("completionRate", 0.0);
        }
        
        return overview;
    }
    
    /**
     * 获取项目经理任务概览
     */
    public Map<String, Object> getProjectManagerTasksOverview(Long managerId) {
        Map<String, Object> overview = new HashMap<>();
        // TODO: Implement actual overview logic
        overview.put("managedProjects", 0);
        overview.put("totalTasks", 0);
        overview.put("completedTasks", 0);
        return overview;
    }
    
    /**
     * 检查项目访问权限
     */
    private boolean hasProjectAccess(Long projectId) {
        // 管理员可以访问所有项目
        if (PermissionUtil.canViewAllProjects()) {
            return true;
        }
        
        // 检查用户是否参与该项目
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        return projectMapper.isUserInProject(currentUserId, projectId) > 0;
    }
    
    /**
     * 检查任务访问权限
     */
    private boolean hasTaskAccess(Task task) {
        // 管理员可以访问所有任务
        if (PermissionUtil.canViewAllProjects()) {
            return true;
        }
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        // 检查用户是否参与该任务所属的项目
        return hasProjectAccess(task.getProjectId());
    }
}