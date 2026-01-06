package com.timetracking.service;

import com.timetracking.entity.Project;
import com.timetracking.mapper.ProjectMapper;
import com.timetracking.mapper.ProjectStatisticsMapper;
import com.timetracking.vo.ProjectStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ProjectStatisticsService {
    
    @Autowired
    private ProjectStatisticsMapper statisticsMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    /**
     * 获取项目完整统计信息
     */
    public ProjectStatisticsVO getProjectStatistics(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return null;
        }
        
        ProjectStatisticsVO statistics = new ProjectStatisticsVO();
        
        // 基本信息
        statistics.setProjectId(projectId);
        statistics.setProjectName(project.getProjectName());
        statistics.setProjectCode(project.getProjectCode());
        statistics.setStatus(project.getStatus().name());
        
        // 获取真实的进度统计数据
        ProjectStatisticsVO.ProgressStats progressStats = statisticsMapper.getProgressStats(projectId);
        if (progressStats != null) {
            // 计算剩余工时
            if (progressStats.getEstimatedHours() != null && progressStats.getActualHours() != null) {
                BigDecimal remainingHours = progressStats.getEstimatedHours().subtract(progressStats.getActualHours());
                progressStats.setRemainingHours(remainingHours.max(BigDecimal.ZERO));
                
                // 计算工时利用率
                if (progressStats.getEstimatedHours().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal utilization = progressStats.getActualHours()
                            .divide(progressStats.getEstimatedHours(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    progressStats.setHoursUtilization(utilization);
                }
            }
        } else {
            // 如果没有数据，创建默认值
            progressStats = new ProjectStatisticsVO.ProgressStats();
            progressStats.setCompletedTasks(0);
            progressStats.setTotalTasks(0);
            progressStats.setPendingTasks(0);
            progressStats.setActualHours(BigDecimal.ZERO);
            progressStats.setEstimatedHours(project.getEstimatedHours() != null ? project.getEstimatedHours() : BigDecimal.ZERO);
            progressStats.setProgressPercentage(BigDecimal.ZERO);
            progressStats.setRemainingHours(progressStats.getEstimatedHours());
            progressStats.setHoursUtilization(BigDecimal.ZERO);
        }
        statistics.setProgressStats(progressStats);
        
        // 获取真实的财务统计数据
        ProjectStatisticsVO.FinancialStats financialStats = statisticsMapper.getFinancialStats(projectId);
        if (financialStats != null) {
            // 计算成本状态
            if (financialStats.getBudgetAmount() != null && financialStats.getActualCost() != null) {
                if (financialStats.getActualCost().compareTo(financialStats.getBudgetAmount()) > 0) {
                    financialStats.setCostStatus("OVER_BUDGET");
                } else if (financialStats.getActualCost().compareTo(financialStats.getBudgetAmount().multiply(BigDecimal.valueOf(0.9))) > 0) {
                    financialStats.setCostStatus("ON_BUDGET");
                } else {
                    financialStats.setCostStatus("UNDER_BUDGET");
                }
            }
        } else {
            // 如果没有数据，从项目基本信息获取
            financialStats = new ProjectStatisticsVO.FinancialStats();
            financialStats.setContractAmount(project.getContractAmount() != null ? project.getContractAmount() : BigDecimal.ZERO);
            financialStats.setBudgetAmount(project.getBudgetAmount() != null ? project.getBudgetAmount() : BigDecimal.ZERO);
            financialStats.setActualCost(BigDecimal.ZERO);
            financialStats.setLaborCost(BigDecimal.ZERO);
            financialStats.setOtherCost(BigDecimal.ZERO);
            financialStats.setCostUtilization(BigDecimal.ZERO);
            financialStats.setProfitMargin(BigDecimal.valueOf(100));
            financialStats.setCostStatus("UNDER_BUDGET");
        }
        statistics.setFinancialStats(financialStats);
        
        // 获取真实的工期统计数据
        ProjectStatisticsVO.ScheduleStats scheduleStats = statisticsMapper.getScheduleStats(projectId);
        if (scheduleStats != null) {
            // 计算工期状态
            if (scheduleStats.getDelayDays() != null) {
                if (scheduleStats.getDelayDays() > 7) {
                    scheduleStats.setScheduleStatus("DELAYED");
                } else if (scheduleStats.getDelayDays() < -7) {
                    scheduleStats.setScheduleStatus("AHEAD");
                } else {
                    scheduleStats.setScheduleStatus("ON_SCHEDULE");
                }
            }
        } else {
            // 如果没有数据，从项目基本信息获取
            scheduleStats = new ProjectStatisticsVO.ScheduleStats();
            scheduleStats.setPlannedStartDate(project.getPlannedStartDate());
            scheduleStats.setPlannedEndDate(project.getPlannedEndDate());
            scheduleStats.setActualStartDate(project.getActualStartDate());
            scheduleStats.setActualEndDate(project.getActualEndDate());
            
            if (project.getPlannedStartDate() != null && project.getPlannedEndDate() != null) {
                scheduleStats.setPlannedDuration((int) ChronoUnit.DAYS.between(project.getPlannedStartDate(), project.getPlannedEndDate()));
            }
            
            if (project.getActualStartDate() != null) {
                LocalDate endDate = project.getActualEndDate() != null ? project.getActualEndDate() : LocalDate.now();
                scheduleStats.setActualDuration((int) ChronoUnit.DAYS.between(project.getActualStartDate(), endDate));
                
                if (project.getPlannedEndDate() != null) {
                    scheduleStats.setDelayDays((int) ChronoUnit.DAYS.between(project.getPlannedEndDate(), endDate));
                }
            }
            
            scheduleStats.setScheduleStatus("ON_SCHEDULE");
        }
        statistics.setScheduleStats(scheduleStats);
        
        // 获取真实的团队统计数据
        ProjectStatisticsVO.TeamStats teamStats = statisticsMapper.getTeamStats(projectId);
        if (teamStats == null) {
            teamStats = new ProjectStatisticsVO.TeamStats();
            teamStats.setTeamSize(0);
            teamStats.setActiveMembers(0);
            teamStats.setAvgDailyHours(BigDecimal.ZERO);
        }
        statistics.setTeamStats(teamStats);
        
        // 获取里程碑数据
        List<ProjectStatisticsVO.MilestoneInfo> milestones = statisticsMapper.getMilestones(projectId);
        statistics.setMilestones(milestones);
        
        // 获取成本分解数据
        List<ProjectStatisticsVO.CostBreakdown> costBreakdown = statisticsMapper.getCostBreakdown(projectId);
        statistics.setCostBreakdown(costBreakdown);
        
        // 获取质量统计数据
        ProjectStatisticsVO.QualityStats qualityStats = statisticsMapper.getQualityStats(projectId);
        if (qualityStats == null) {
            qualityStats = new ProjectStatisticsVO.QualityStats();
            qualityStats.setQualityScore(project.getQualityScore() != null ? project.getQualityScore() : BigDecimal.ZERO);
            qualityStats.setClientSatisfaction(project.getClientSatisfaction() != null ? project.getClientSatisfaction() : BigDecimal.ZERO);
            qualityStats.setDefectCount(0);
            qualityStats.setReworkHours(BigDecimal.ZERO);
        }
        statistics.setQualityStats(qualityStats);
        
        return statistics;
    }
    
    /**
     * 获取项目列表的简要统计信息
     */
    public List<ProjectStatisticsVO> getProjectsOverview() {
        // 这里可以实现获取所有项目的概览统计
        // 为了性能考虑，只返回关键指标
        return null; // 待实现
    }
}