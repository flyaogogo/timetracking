package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timetracking.entity.Project;
import com.timetracking.entity.Task;
import com.timetracking.mapper.ProjectMapper;
import com.timetracking.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 项目状态管理服务
 * 负责自动调整项目状态
 */
@Service
public class ProjectStatusManagementService {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private TaskMapper taskMapper;
    
    /**
     * 自动调整项目状态
     * @param projectId 项目ID
     */
    public void autoAdjustProjectStatus(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) return;
        
        // 基于项目属性调整
        adjustStatusBasedOnProjectProperties(project);
        
        // 基于任务完成情况调整
        adjustStatusBasedOnTasks(project);
        
        // 基于时间调整
        adjustStatusBasedOnTime(project);
        
        // 基于进度调整
        adjustStatusBasedOnProgress(project);
    }
    
    /**
     * 基于项目属性调整状态
     */
    private void adjustStatusBasedOnProjectProperties(Project project) {
        // 如果项目已经是完成或取消状态，不进行调整
        if (project.getStatus() == Project.ProjectStatus.COMPLETED || 
            project.getStatus() == Project.ProjectStatus.CANCELLED) {
            return;
        }
        
        // 检查是否应该从PLANNING改为IN_PROGRESS
        if (project.getStatus() == Project.ProjectStatus.PLANNING) {
            // 1. 有实际开始日期
            if (project.getActualStartDate() != null) {
                updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
                return;
            }
            
            // 2. 有实际工时
            if (project.getActualHours() != null && project.getActualHours().compareTo(BigDecimal.ZERO) > 0) {
                updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
                return;
            }
            
            // 3. 计划开始日期已过
            if (project.getPlannedStartDate() != null) {
                LocalDate now = LocalDate.now();
                if (project.getPlannedStartDate().isBefore(now) || project.getPlannedStartDate().isEqual(now)) {
                    updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
                    return;
                }
            }
            
            // 4. 开始日期（兼容旧字段）已过
            if (project.getStartDate() != null) {
                LocalDate now = LocalDate.now();
                if (project.getStartDate().isBefore(now) || project.getStartDate().isEqual(now)) {
                    updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
                    return;
                }
            }
        }
        
        // 检查是否应该从IN_PROGRESS改为COMPLETED
        if (project.getStatus() == Project.ProjectStatus.IN_PROGRESS) {
            // 有实际结束日期
            if (project.getActualEndDate() != null) {
                updateProjectStatus(project, Project.ProjectStatus.COMPLETED);
                return;
            }
        }
    }
    
    /**
     * 基于任务完成情况调整状态
     */
    private void adjustStatusBasedOnTasks(Project project) {
        // 如果项目已经是完成或取消状态，不进行调整
        if (project.getStatus() == Project.ProjectStatus.COMPLETED || 
            project.getStatus() == Project.ProjectStatus.CANCELLED) {
            return;
        }
        
        // 获取项目的所有任务
        List<Task> tasks = taskMapper.selectList(new QueryWrapper<Task>()
            .eq("project_id", project.getId()));
        
        if (tasks.isEmpty()) {
            return; // 没有任务，不调整
        }
        
        // 检查是否有任务正在进行中
        boolean hasInProgressTask = false;
        boolean hasCompletedTask = false;
        boolean hasPausedTask = false;
        boolean allTasksCompleted = true;
        boolean allTasksPaused = true;
        
        for (Task task : tasks) {
            if (task.getStatus() == Task.TaskStatus.IN_PROGRESS || 
                task.getStatus() == Task.TaskStatus.REVIEW) {
                hasInProgressTask = true;
                allTasksCompleted = false;
                allTasksPaused = false;
            } else if (task.getStatus() == Task.TaskStatus.COMPLETED) {
                hasCompletedTask = true;
                allTasksPaused = false;
            } else if (task.getStatus() == Task.TaskStatus.PAUSED) {
                hasPausedTask = true;
                allTasksCompleted = false;
            } else {
                allTasksCompleted = false;
                allTasksPaused = false;
            }
        }
        
        // 基于任务状态调整项目状态
        if (project.getStatus() == Project.ProjectStatus.PLANNING) {
            // 如果有任务正在进行或已完成，将项目状态改为IN_PROGRESS
            if (hasInProgressTask || hasCompletedTask) {
                updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
            }
        } else if (project.getStatus() == Project.ProjectStatus.IN_PROGRESS) {
            // 如果所有任务都已完成，将项目状态改为COMPLETED
            if (allTasksCompleted) {
                updateProjectStatus(project, Project.ProjectStatus.COMPLETED);
            } 
            // 如果所有任务都已暂停，将项目状态改为PAUSED
            else if (allTasksPaused) {
                updateProjectStatus(project, Project.ProjectStatus.PAUSED);
            }
        } else if (project.getStatus() == Project.ProjectStatus.PAUSED) {
            // 如果有任务正在进行，将项目状态改为IN_PROGRESS
            if (hasInProgressTask) {
                updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
            }
        }
    }
    
    /**
     * 基于时间调整状态
     */
    private void adjustStatusBasedOnTime(Project project) {
        // 如果项目已经是完成或取消状态，不进行调整
        if (project.getStatus() == Project.ProjectStatus.COMPLETED || 
            project.getStatus() == Project.ProjectStatus.CANCELLED) {
            return;
        }
        
        LocalDate now = LocalDate.now();
        
        // 计划开始日期已过但项目仍为PLANNING状态
        if (project.getStatus() == Project.ProjectStatus.PLANNING) {
            if (project.getPlannedStartDate() != null && 
                (project.getPlannedStartDate().isBefore(now) || project.getPlannedStartDate().isEqual(now))) {
                updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
            } else if (project.getStartDate() != null && 
                       (project.getStartDate().isBefore(now) || project.getStartDate().isEqual(now))) {
                updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
            }
        }
        
        // 计划结束日期已过但项目仍为IN_PROGRESS状态
        if (project.getStatus() == Project.ProjectStatus.IN_PROGRESS) {
            if (project.getPlannedEndDate() != null && project.getPlannedEndDate().isBefore(now)) {
                // 检查项目进度
                if (project.getCompletionPercentage() != null && 
                    project.getCompletionPercentage().compareTo(new BigDecimal(90)) >= 0) {
                    // 进度≥90%，自动改为COMPLETED
                    updateProjectStatus(project, Project.ProjectStatus.COMPLETED);
                }
            }
        }
    }
    
    /**
     * 基于进度调整状态
     */
    private void adjustStatusBasedOnProgress(Project project) {
        // 如果项目已经是完成或取消状态，不进行调整
        if (project.getStatus() == Project.ProjectStatus.COMPLETED || 
            project.getStatus() == Project.ProjectStatus.CANCELLED) {
            return;
        }
        
        BigDecimal completionPercentage = project.getCompletionPercentage();
        if (completionPercentage == null) {
            return;
        }
        
        // 进度≥100%，自动改为COMPLETED
        if (completionPercentage.compareTo(new BigDecimal(100)) >= 0) {
            updateProjectStatus(project, Project.ProjectStatus.COMPLETED);
        } 
        // 进度>0且<100%，如果当前为PLANNING，自动改为IN_PROGRESS
        else if (completionPercentage.compareTo(BigDecimal.ZERO) > 0 && 
                 project.getStatus() == Project.ProjectStatus.PLANNING) {
            updateProjectStatus(project, Project.ProjectStatus.IN_PROGRESS);
        }
    }
    
    /**
     * 更新项目状态
     */
    private void updateProjectStatus(Project project, Project.ProjectStatus newStatus) {
        if (project.getStatus() != newStatus) {
            Project updateProject = new Project();
            updateProject.setId(project.getId());
            updateProject.setStatus(newStatus);
            projectMapper.updateById(updateProject);
            
            // 这里可以添加状态变更日志记录
            System.out.println("Project " + project.getProjectName() + " status changed to " + newStatus.name());
        }
    }
}
