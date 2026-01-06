package com.timetracking.service;

import com.timetracking.entity.ProjectMilestone;
import com.timetracking.mapper.ProjectMilestoneMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目里程碑服务
 */
@Service
public class ProjectMilestoneService {
    
    @Autowired
    private ProjectMilestoneMapper projectMilestoneMapper;
    
    /**
     * 获取项目里程碑列表
     */
    public List<ProjectMilestone> getProjectMilestones(Long projectId) {
        return projectMilestoneMapper.selectByProjectId(projectId);
    }
    
    /**
     * 根据ID获取里程碑
     */
    public ProjectMilestone getById(Long id) {
        return projectMilestoneMapper.selectById(id);
    }
    
    /**
     * 获取项目里程碑统计
     */
    public Map<String, Object> getProjectMilestoneStatistics(Long projectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总里程碑数
        Integer totalMilestones = projectMilestoneMapper.getTotalMilestonesByProject(projectId);
        statistics.put("totalMilestones", totalMilestones != null ? totalMilestones : 0);
        
        // 已完成里程碑数
        Integer completedMilestones = projectMilestoneMapper.getCompletedMilestonesByProject(projectId);
        statistics.put("completedMilestones", completedMilestones != null ? completedMilestones : 0);
        
        // 进行中里程碑数
        Integer inProgressMilestones = projectMilestoneMapper.getInProgressMilestonesByProject(projectId);
        statistics.put("inProgressMilestones", inProgressMilestones != null ? inProgressMilestones : 0);
        
        // 延期里程碑数
        Integer delayedMilestones = projectMilestoneMapper.getDelayedMilestonesByProject(projectId);
        statistics.put("delayedMilestones", delayedMilestones != null ? delayedMilestones : 0);
        
        // 平均完成度
        Double avgProgress = projectMilestoneMapper.getAvgProgressByProject(projectId);
        statistics.put("avgProgress", avgProgress != null ? avgProgress : 0.0);
        
        return statistics;
    }
    
    /**
     * 创建项目里程碑
     */
    public void createProjectMilestone(ProjectMilestone projectMilestone) {
        projectMilestone.setCreateTime(LocalDateTime.now());
        projectMilestone.setUpdateTime(LocalDateTime.now());
        
        // 自动判断状态
        updateMilestoneStatus(projectMilestone);
        
        projectMilestoneMapper.insert(projectMilestone);
    }
    
    /**
     * 更新项目里程碑
     */
    public void updateProjectMilestone(ProjectMilestone projectMilestone) {
        projectMilestone.setUpdateTime(LocalDateTime.now());
        
        // 自动判断状态
        updateMilestoneStatus(projectMilestone);
        
        projectMilestoneMapper.updateById(projectMilestone);
    }
    
    /**
     * 删除项目里程碑
     */
    public void deleteProjectMilestone(Long id) {
        projectMilestoneMapper.deleteById(id);
    }
    
    /**
     * 批量保存项目里程碑
     */
    public void batchSaveProjectMilestones(Long projectId, List<ProjectMilestone> milestones) {
        for (ProjectMilestone milestone : milestones) {
            milestone.setProjectId(projectId);
            
            if (milestone.getId() != null) {
                // 更新现有里程碑
                updateProjectMilestone(milestone);
            } else {
                // 创建新里程碑
                createProjectMilestone(milestone);
            }
        }
    }
    
    /**
     * 更新里程碑状态
     */
    private void updateMilestoneStatus(ProjectMilestone milestone) {
        if (milestone.getCompletionPercentage() != null && milestone.getCompletionPercentage() >= 100) {
            milestone.setStatus(ProjectMilestone.MilestoneStatus.COMPLETED);
            if (milestone.getActualDate() == null) {
                milestone.setActualDate(LocalDate.now());
            }
        } else if (milestone.getPlannedDate() != null && 
                   LocalDate.now().isAfter(milestone.getPlannedDate()) && 
                   milestone.getActualDate() == null) {
            milestone.setStatus(ProjectMilestone.MilestoneStatus.DELAYED);
        } else if (milestone.getCompletionPercentage() != null && milestone.getCompletionPercentage() > 0) {
            milestone.setStatus(ProjectMilestone.MilestoneStatus.IN_PROGRESS);
        } else {
            milestone.setStatus(ProjectMilestone.MilestoneStatus.PENDING);
        }
    }
}