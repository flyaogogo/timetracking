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
public class ProjectStatisticsEnhancedService {
    
    @Autowired
    private ProjectStatisticsMapper statisticsMapper;
    
    @Autowired
    private ProjectMapper projectMapper;
    
    /**
     * 获取增强的项目统计信息
     * 包含详细的成本分析、团队统计、里程碑进度等
     */
    public ProjectStatisticsVO getEnhancedProjectStatistics(Long projectId) {
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
        
        // 获取增强的进度统计
        statistics.setProgressStats(getEnhancedProgressStats(projectId, project));
        
        // 获取增强的财务统计
        statistics.setFinancialStats(getEnhancedFinancialStats(projectId, project));
        
        // 获取增强的工期统计
        statistics.setScheduleStats(getEnhancedScheduleStats(projectId, project));
        
        // 获取增强的团队统计
        statistics.setTeamStats(getEnhancedTeamStats(projectId));
        
        // 获取里程碑数据
        statistics.setMilestones(getMilestoneProgress(projectId));
        
        // 获取成本分解数据
        statistics.setCostBreakdown(getDetailedCostBreakdown(projectId));
        
        // 获取质量统计
        statistics.setQualityStats(getQualityStats(projectId, project));
        
        return statistics;
    }
    
    /**
     * 获取增强的进度统计
     */
    private ProjectStatisticsVO.ProgressStats getEnhancedProgressStats(Long projectId, Project project) {
        ProjectStatisticsVO.ProgressStats progressStats = statisticsMapper.getProgressStats(projectId);
        
        if (progressStats == null) {
            progressStats = new ProjectStatisticsVO.ProgressStats();
            progressStats.setCompletedTasks(0);
            progressStats.setTotalTasks(0);
            progressStats.setPendingTasks(0);
            progressStats.setActualHours(BigDecimal.ZERO);
            progressStats.setEstimatedHours(project.getEstimatedHours() != null ? project.getEstimatedHours() : BigDecimal.ZERO);
        }
        
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
            } else {
                progressStats.setHoursUtilization(BigDecimal.ZERO);
            }
        }
        
        // 计算进度百分比
        if (progressStats.getTotalTasks() > 0) {
            BigDecimal progress = BigDecimal.valueOf(progressStats.getCompletedTasks())
                    .divide(BigDecimal.valueOf(progressStats.getTotalTasks()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            progressStats.setProgressPercentage(progress);
        } else {
            progressStats.setProgressPercentage(BigDecimal.ZERO);
        }
        
        return progressStats;
    }
    
    /**
     * 获取增强的财务统计
     * 统计思路：
     * 1. 实际成本 = 人工成本 + 其他成本
     * 2. 人工成本 = Σ(员工工时 × 员工时薪)
     * 3. 其他成本从项目费用记录中统计
     * 4. 成本使用率 = 实际成本 / 预算成本 × 100%
     */
    private ProjectStatisticsVO.FinancialStats getEnhancedFinancialStats(Long projectId, Project project) {
        ProjectStatisticsVO.FinancialStats financialStats = statisticsMapper.getFinancialStats(projectId);
        
        if (financialStats == null) {
            financialStats = new ProjectStatisticsVO.FinancialStats();
            financialStats.setContractAmount(project.getContractAmount() != null ? project.getContractAmount() : BigDecimal.ZERO);
            financialStats.setBudgetAmount(project.getBudgetAmount() != null ? project.getBudgetAmount() : BigDecimal.ZERO);
        }
        
        // 计算详细的人工成本
        // 人工成本 = Σ(项目成员实际工时 × 成员时薪)
        BigDecimal laborCost = calculateLaborCost(projectId);
        financialStats.setLaborCost(laborCost);
        
        // 计算其他成本（设备、差旅、外包等）
        BigDecimal otherCost = calculateOtherCost(projectId);
        financialStats.setOtherCost(otherCost);
        
        // 计算总实际成本
        BigDecimal actualCost = laborCost.add(otherCost);
        financialStats.setActualCost(actualCost);
        
        // 计算成本使用率
        if (financialStats.getBudgetAmount() != null && financialStats.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costUtilization = actualCost
                    .divide(financialStats.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            financialStats.setCostUtilization(costUtilization);
        } else {
            financialStats.setCostUtilization(BigDecimal.ZERO);
        }
        
        // 计算利润率
        if (financialStats.getContractAmount() != null && financialStats.getContractAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal profit = financialStats.getContractAmount().subtract(actualCost);
            BigDecimal profitMargin = profit
                    .divide(financialStats.getContractAmount(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            financialStats.setProfitMargin(profitMargin);
        } else {
            financialStats.setProfitMargin(BigDecimal.ZERO);
        }
        
        // 确定成本状态
        if (financialStats.getBudgetAmount() != null && actualCost.compareTo(financialStats.getBudgetAmount()) > 0) {
            financialStats.setCostStatus("OVER_BUDGET");
        } else if (financialStats.getBudgetAmount() != null && 
                   actualCost.compareTo(financialStats.getBudgetAmount().multiply(BigDecimal.valueOf(0.9))) > 0) {
            financialStats.setCostStatus("ON_BUDGET");
        } else {
            financialStats.setCostStatus("UNDER_BUDGET");
        }
        
        return financialStats;
    }
    
    /**
     * 计算人工成本
     * 统计思路：从time_entries表和users表关联，计算 Σ(工时 × 时薪)
     */
    private BigDecimal calculateLaborCost(Long projectId) {
        // 这里应该执行SQL查询：
        // SELECT SUM(te.duration * u.hourly_rate) as labor_cost
        // FROM time_entries te 
        // JOIN users u ON te.user_id = u.id 
        // WHERE te.project_id = #{projectId}
        return statisticsMapper.calculateLaborCost(projectId);
    }
    
    /**
     * 计算其他成本
     * 统计思路：从项目费用记录表中统计非人工类成本
     */
    private BigDecimal calculateOtherCost(Long projectId) {
        // 这里应该从项目费用表中统计设备费、差旅费、外包费等
        return statisticsMapper.calculateOtherCost(projectId);
    }
    
    /**
     * 获取增强的工期统计
     */
    private ProjectStatisticsVO.ScheduleStats getEnhancedScheduleStats(Long projectId, Project project) {
        ProjectStatisticsVO.ScheduleStats scheduleStats = statisticsMapper.getScheduleStats(projectId);
        
        if (scheduleStats == null) {
            scheduleStats = new ProjectStatisticsVO.ScheduleStats();
            scheduleStats.setPlannedStartDate(project.getPlannedStartDate());
            scheduleStats.setPlannedEndDate(project.getPlannedEndDate());
            scheduleStats.setActualStartDate(project.getActualStartDate());
            scheduleStats.setActualEndDate(project.getActualEndDate());
        }
        
        // 计算计划工期
        if (scheduleStats.getPlannedStartDate() != null && scheduleStats.getPlannedEndDate() != null) {
            int plannedDuration = (int) ChronoUnit.DAYS.between(
                scheduleStats.getPlannedStartDate(), 
                scheduleStats.getPlannedEndDate()
            );
            scheduleStats.setPlannedDuration(plannedDuration);
        }
        
        // 计算实际工期和延期天数
        if (scheduleStats.getActualStartDate() != null) {
            LocalDate endDate = scheduleStats.getActualEndDate() != null ? 
                scheduleStats.getActualEndDate() : LocalDate.now();
            
            int actualDuration = (int) ChronoUnit.DAYS.between(
                scheduleStats.getActualStartDate(), 
                endDate
            );
            scheduleStats.setActualDuration(actualDuration);
            
            // 计算延期天数
            if (scheduleStats.getPlannedEndDate() != null) {
                int delayDays = (int) ChronoUnit.DAYS.between(
                    scheduleStats.getPlannedEndDate(), 
                    endDate
                );
                scheduleStats.setDelayDays(delayDays);
                
                // 确定工期状态
                if (delayDays > 7) {
                    scheduleStats.setScheduleStatus("DELAYED");
                } else if (delayDays < -7) {
                    scheduleStats.setScheduleStatus("AHEAD");
                } else {
                    scheduleStats.setScheduleStatus("ON_SCHEDULE");
                }
            }
        }
        
        return scheduleStats;
    }
    
    /**
     * 获取增强的团队统计
     * 统计思路：
     * 1. 团队规模：project_members表中该项目的所有成员数量
     * 2. 活跃成员：近7天内有工时记录的成员数量
     * 3. 日均工时：项目总工时 / 项目进行天数 / 团队规模
     * 4. 工作效率：实际完成工时 / 预估工时 × 100%
     */
    private ProjectStatisticsVO.TeamStats getEnhancedTeamStats(Long projectId) {
        ProjectStatisticsVO.TeamStats teamStats = new ProjectStatisticsVO.TeamStats();
        
        // 1. 统计团队规模
        Integer teamSize = statisticsMapper.getTeamSize(projectId);
        teamStats.setTeamSize(teamSize != null ? teamSize : 0);
        
        // 2. 统计活跃成员（近7天有工时记录）
        Integer activeMembers = statisticsMapper.getActiveMembersCount(projectId, 7);
        teamStats.setActiveMembers(activeMembers != null ? activeMembers : 0);
        
        // 3. 计算日均工时
        BigDecimal avgDailyHours = statisticsMapper.getAvgDailyHours(projectId);
        teamStats.setAvgDailyHours(avgDailyHours != null ? avgDailyHours : BigDecimal.ZERO);
        
        // 4. 计算工作效率
        BigDecimal efficiency = statisticsMapper.getTeamEfficiency(projectId);
        teamStats.setEfficiency(efficiency != null ? efficiency : BigDecimal.ZERO);
        
        return teamStats;
    }
    
    /**
     * 获取里程碑进度
     */
    private List<ProjectStatisticsVO.MilestoneInfo> getMilestoneProgress(Long projectId) {
        List<ProjectStatisticsVO.MilestoneInfo> milestones = statisticsMapper.getMilestones(projectId);
        
        // 计算每个里程碑的进度和状态
        if (milestones != null) {
            for (ProjectStatisticsVO.MilestoneInfo milestone : milestones) {
                // 计算里程碑进度
                Integer progress = statisticsMapper.getMilestoneProgress(milestone.getId());
                milestone.setProgress(progress != null ? progress : 0);
                
                // 判断是否延期
                if (milestone.getPlannedDate() != null && milestone.getActualDate() == null) {
                    boolean isDelayed = LocalDate.now().isAfter(milestone.getPlannedDate());
                    milestone.setDelayed(isDelayed);
                }
                
                // 设置状态
                if (milestone.getActualDate() != null) {
                    milestone.setStatus("COMPLETED");
                } else if (milestone.isDelayed()) {
                    milestone.setStatus("DELAYED");
                } else {
                    milestone.setStatus("IN_PROGRESS");
                }
            }
        }
        
        return milestones;
    }
    
    /**
     * 获取详细的成本分解
     */
    private List<ProjectStatisticsVO.CostBreakdown> getDetailedCostBreakdown(Long projectId) {
        return statisticsMapper.getCostBreakdown(projectId);
    }
    
    /**
     * 获取质量统计
     */
    private ProjectStatisticsVO.QualityStats getQualityStats(Long projectId, Project project) {
        ProjectStatisticsVO.QualityStats qualityStats = statisticsMapper.getQualityStats(projectId);
        
        if (qualityStats == null) {
            qualityStats = new ProjectStatisticsVO.QualityStats();
            qualityStats.setQualityScore(project.getQualityScore() != null ? project.getQualityScore() : BigDecimal.ZERO);
            qualityStats.setClientSatisfaction(project.getClientSatisfaction() != null ? project.getClientSatisfaction() : BigDecimal.ZERO);
            qualityStats.setDefectCount(0);
            qualityStats.setReworkHours(BigDecimal.ZERO);
        }
        
        return qualityStats;
    }
}