package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timetracking.entity.Task;
import com.timetracking.entity.TimeEntry;
import com.timetracking.entity.Project;
import com.timetracking.mapper.TaskMapper;
import com.timetracking.mapper.TimeEntryMapper;
import com.timetracking.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 工时统计服务
 * 负责计算和更新任务实际工时、进度以及项目工时汇总
 */
@Service
public class TimeTrackingStatisticsService {
    
    @Autowired
    private TaskMapper taskMapper;
    
    @Autowired
    private TimeEntryMapper timeEntryMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    /**
     * 更新任务的实际工时和进度
     * @param taskId 任务ID
     * @return 更新后的任务信息
     */
    @Transactional
    public Task updateTaskActualHoursAndProgress(Long taskId) {
        if (taskId == null) {
            return null;
        }
        
        // 1. 计算任务的实际工时（只统计已审核的工时记录）
        BigDecimal actualHours = calculateTaskActualHours(taskId);
        
        // 2. 获取任务信息
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return null;
        }
        
        // 3. 计算进度
        Integer newProgress = calculateTaskProgress(task, actualHours);
        
        // 4. 更新任务的实际工时和进度
        Task updateTask = new Task();
        updateTask.setId(taskId);
        updateTask.setActualHours(actualHours);
        updateTask.setProgress(newProgress);
        
        // 5. 根据进度自动更新任务状态
        Task.TaskStatus newStatus = determineTaskStatus(task, newProgress);
        if (newStatus != null && !newStatus.equals(task.getStatus())) {
            updateTask.setStatus(newStatus);
        }
        
        taskMapper.updateById(updateTask);
        
        // 6. 更新项目的实际工时汇总
        updateProjectActualHours(task.getProjectId());
        
        return taskMapper.selectById(taskId);
    }
    
    /**
     * 计算任务的实际工时
     * @param taskId 任务ID
     * @return 实际工时
     */
    public BigDecimal calculateTaskActualHours(Long taskId) {
        QueryWrapper<TimeEntry> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId)
               .eq("status", "APPROVED"); // 只统计已审核的工时
        
        List<TimeEntry> timeEntries = timeEntryMapper.selectList(wrapper);
        
        BigDecimal totalHours = BigDecimal.ZERO;
        for (TimeEntry entry : timeEntries) {
            if (entry.getDuration() != null) {
                totalHours = totalHours.add(entry.getDuration());
            }
        }
        
        return totalHours.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算任务进度
     * @param task 任务信息
     * @param actualHours 实际工时
     * @return 进度百分比
     */
    private Integer calculateTaskProgress(Task task, BigDecimal actualHours) {
        if (task.getEstimatedHours() == null || task.getEstimatedHours().compareTo(BigDecimal.ZERO) <= 0) {
            // 如果没有预估工时，根据任务状态返回默认进度
            return getDefaultProgressByStatus(task.getStatus());
        }
        
        // 根据实际工时与预估工时的比例计算进度
        BigDecimal progressRatio = actualHours.divide(task.getEstimatedHours(), 4, RoundingMode.HALF_UP);
        int calculatedProgress = progressRatio.multiply(new BigDecimal(100)).intValue();
        
        // 进度不能超过100%
        calculatedProgress = Math.min(calculatedProgress, 100);
        
        // 如果任务已完成，进度应该是100%
        if (task.getStatus() == Task.TaskStatus.COMPLETED) {
            calculatedProgress = 100;
        }
        
        return calculatedProgress;
    }
    
    /**
     * 根据任务状态获取默认进度
     */
    private Integer getDefaultProgressByStatus(Task.TaskStatus status) {
        if (status == null) {
            return 0;
        }
        
        switch (status) {
            case TODO:
                return 0;
            case IN_PROGRESS:
                return 50;
            case REVIEW:
                return 90;
            case COMPLETED:
                return 100;
            case PAUSED:
                return 30;
            case CANCELLED:
                return 0;
            default:
                return 0;
        }
    }
    
    /**
     * 根据进度确定任务状态
     */
    private Task.TaskStatus determineTaskStatus(Task task, Integer progress) {
        if (progress == null) {
            return null;
        }
        
        // 如果任务已经是完成或取消状态，不自动更改
        if (task.getStatus() == Task.TaskStatus.COMPLETED || 
            task.getStatus() == Task.TaskStatus.CANCELLED) {
            return null;
        }
        
        // 根据进度自动调整状态
        if (progress >= 100) {
            return Task.TaskStatus.COMPLETED;
        } else if (progress >= 90) {
            return Task.TaskStatus.REVIEW;
        } else if (progress > 0) {
            return Task.TaskStatus.IN_PROGRESS;
        } else {
            return Task.TaskStatus.TODO;
        }
    }
    
    /**
     * 更新项目的实际工时汇总
     * @param projectId 项目ID
     */
    @Transactional
    public void updateProjectActualHours(Long projectId) {
        if (projectId == null) {
            return;
        }
        
        // 1. 计算项目所有任务的实际工时总和
        BigDecimal totalActualHours = calculateProjectActualHours(projectId);
        
        // 2. 计算项目所有任务的预估工时总和
        BigDecimal totalEstimatedHours = calculateProjectEstimatedHours(projectId);
        
        // 3. 计算项目整体进度
        Integer projectProgress = calculateProjectProgress(projectId, totalActualHours, totalEstimatedHours);
        
        // 4. 更新项目信息
        Project updateProject = new Project();
        updateProject.setId(projectId);
        updateProject.setActualHours(totalActualHours);
        updateProject.setCompletionPercentage(new BigDecimal(projectProgress));
        
        projectMapper.updateById(updateProject);
    }
    
    /**
     * 计算项目的实际工时
     */
    public BigDecimal calculateProjectActualHours(Long projectId) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId);
        
        List<Task> tasks = taskMapper.selectList(wrapper);
        
        BigDecimal totalHours = BigDecimal.ZERO;
        for (Task task : tasks) {
            BigDecimal taskActualHours = calculateTaskActualHours(task.getId());
            totalHours = totalHours.add(taskActualHours);
        }
        
        return totalHours.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算项目的预估工时
     */
    public BigDecimal calculateProjectEstimatedHours(Long projectId) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId);
        
        List<Task> tasks = taskMapper.selectList(wrapper);
        
        BigDecimal totalHours = BigDecimal.ZERO;
        for (Task task : tasks) {
            if (task.getEstimatedHours() != null) {
                totalHours = totalHours.add(task.getEstimatedHours());
            }
        }
        
        return totalHours.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算项目进度
     */
    private Integer calculateProjectProgress(Long projectId, BigDecimal actualHours, BigDecimal estimatedHours) {
        if (estimatedHours == null || estimatedHours.compareTo(BigDecimal.ZERO) <= 0) {
            // 如果没有预估工时，根据任务完成情况计算进度
            return calculateProjectProgressByTaskCompletion(projectId);
        }
        
        // 根据实际工时与预估工时的比例计算进度
        BigDecimal progressRatio = actualHours.divide(estimatedHours, 4, RoundingMode.HALF_UP);
        int calculatedProgress = progressRatio.multiply(new BigDecimal(100)).intValue();
        
        // 进度不能超过100%
        return Math.min(calculatedProgress, 100);
    }
    
    /**
     * 根据任务完成情况计算项目进度
     */
    private Integer calculateProjectProgressByTaskCompletion(Long projectId) {
        QueryWrapper<Task> wrapper = new QueryWrapper<>();
        wrapper.eq("project_id", projectId);
        
        List<Task> tasks = taskMapper.selectList(wrapper);
        
        if (tasks.isEmpty()) {
            return 0;
        }
        
        int totalTasks = tasks.size();
        int completedTasks = 0;
        int totalProgress = 0;
        
        for (Task task : tasks) {
            if (task.getStatus() == Task.TaskStatus.COMPLETED) {
                completedTasks++;
            }
            if (task.getProgress() != null) {
                totalProgress += task.getProgress();
            }
        }
        
        // 使用任务平均进度作为项目进度
        return totalProgress / totalTasks;
    }
    
    /**
     * 批量更新所有任务的实际工时和进度
     */
    @Transactional
    public void updateAllTasksActualHours() {
        List<Task> allTasks = taskMapper.selectList(null);
        
        for (Task task : allTasks) {
            updateTaskActualHoursAndProgress(task.getId());
        }
    }
    
    /**
     * 批量更新所有项目的实际工时汇总
     */
    @Transactional
    public void updateAllProjectsActualHours() {
        List<Project> allProjects = projectMapper.selectList(null);
        
        for (Project project : allProjects) {
            updateProjectActualHours(project.getId());
        }
    }
    
    /**
     * 获取任务工时统计详情
     */
    public Map<String, Object> getTaskTimeStatistics(Long taskId) {
        Map<String, Object> statistics = new HashMap<>();
        
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            return statistics;
        }
        
        // 基本信息
        statistics.put("taskId", taskId);
        statistics.put("taskName", task.getTaskName());
        statistics.put("estimatedHours", task.getEstimatedHours());
        statistics.put("actualHours", calculateTaskActualHours(taskId));
        statistics.put("progress", task.getProgress());
        statistics.put("status", task.getStatus());
        
        // 工时记录统计
        QueryWrapper<TimeEntry> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId);
        
        List<TimeEntry> allEntries = timeEntryMapper.selectList(wrapper);
        List<TimeEntry> approvedEntries = timeEntryMapper.selectList(
            wrapper.clone().eq("status", "APPROVED")
        );
        List<TimeEntry> pendingEntries = timeEntryMapper.selectList(
            wrapper.clone().eq("status", "PENDING")
        );
        
        statistics.put("totalTimeEntries", allEntries.size());
        statistics.put("approvedTimeEntries", approvedEntries.size());
        statistics.put("pendingTimeEntries", pendingEntries.size());
        
        // 工时偏差分析
        BigDecimal actualHours = calculateTaskActualHours(taskId);
        if (task.getEstimatedHours() != null && task.getEstimatedHours().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal variance = actualHours.subtract(task.getEstimatedHours());
            BigDecimal variancePercentage = variance.divide(task.getEstimatedHours(), 4, RoundingMode.HALF_UP)
                                                  .multiply(new BigDecimal(100));
            
            statistics.put("hoursVariance", variance);
            statistics.put("variancePercentage", variancePercentage);
        }
        
        return statistics;
    }
    
    /**
     * 获取项目工时统计详情
     */
    public Map<String, Object> getProjectTimeStatistics(Long projectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return statistics;
        }
        
        // 基本信息
        statistics.put("projectId", projectId);
        statistics.put("projectName", project.getProjectName());
        statistics.put("estimatedHours", calculateProjectEstimatedHours(projectId));
        statistics.put("actualHours", calculateProjectActualHours(projectId));
        
        // 任务统计
        QueryWrapper<Task> taskWrapper = new QueryWrapper<>();
        taskWrapper.eq("project_id", projectId);
        
        List<Task> allTasks = taskMapper.selectList(taskWrapper);
        long completedTasks = allTasks.stream()
            .filter(task -> task.getStatus() == Task.TaskStatus.COMPLETED)
            .count();
        
        statistics.put("totalTasks", allTasks.size());
        statistics.put("completedTasks", completedTasks);
        statistics.put("taskCompletionRate", allTasks.isEmpty() ? 0 : 
            (double) completedTasks / allTasks.size() * 100);
        
        // 工时记录统计
        QueryWrapper<TimeEntry> entryWrapper = new QueryWrapper<>();
        entryWrapper.eq("project_id", projectId);
        
        List<TimeEntry> allEntries = timeEntryMapper.selectList(entryWrapper);
        List<TimeEntry> approvedEntries = timeEntryMapper.selectList(
            entryWrapper.clone().eq("status", "APPROVED")
        );
        
        statistics.put("totalTimeEntries", allEntries.size());
        statistics.put("approvedTimeEntries", approvedEntries.size());
        
        return statistics;
    }
}