package com.timetracking.mapper;

import com.timetracking.vo.ProjectStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProjectStatisticsMapper {
    
    /**
     * 获取项目进度统计 - 使用增强字段
     */
    @Select("SELECT " +
            "COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as completedTasks, " +
            "COUNT(*) as totalTasks, " +
            "COUNT(CASE WHEN t.status != 'COMPLETED' THEN 1 END) as pendingTasks, " +
            "COALESCE(SUM(t.actual_hours), 0) as actualHours, " +
            "COALESCE(SUM(t.estimated_hours), 0) as estimatedHours, " +
            "COALESCE(AVG(t.completion_percentage), 0) as progressPercentage, " +
            "COALESCE(SUM(t.baseline_hours), 0) as baselineHours, " +
            "COALESCE(AVG(t.quality_score), 0) as avgQualityScore " +
            "FROM tasks t WHERE t.project_id = #{projectId}")
    ProjectStatisticsVO.ProgressStats getProgressStats(@Param("projectId") Long projectId);
    
    /**
     * 获取项目财务统计 - 使用增强字段
     */
    @Select("SELECT " +
            "p.contract_amount as contractAmount, " +
            "p.budget_amount as budgetAmount, " +
            "p.baseline_budget as baselineBudget, " +
            "p.actual_labor_cost + p.actual_other_cost as actualCost, " +
            "p.actual_labor_cost as laborCost, " +
            "p.actual_other_cost as otherCost, " +
            "p.earned_value as earnedValue, " +
            "p.planned_value as plannedValue, " +
            "p.cost_performance_index as costPerformanceIndex, " +
            "CASE WHEN p.budget_amount > 0 " +
            "THEN ((p.actual_labor_cost + p.actual_other_cost) / p.budget_amount) * 100 " +
            "ELSE 0 END as costUtilization, " +
            "CASE WHEN p.contract_amount > 0 " +
            "THEN ((p.contract_amount - (p.actual_labor_cost + p.actual_other_cost)) / p.contract_amount) * 100 " +
            "ELSE 0 END as profitMargin, " +
            "p.roi_percentage as roiPercentage " +
            "FROM projects p WHERE p.id = #{projectId}")
    ProjectStatisticsVO.FinancialStats getFinancialStats(@Param("projectId") Long projectId);
    
    /**
     * 获取项目工期统计 - 使用增强字段
     */
    @Select("SELECT " +
            "p.planned_start_date as plannedStartDate, " +
            "p.planned_end_date as plannedEndDate, " +
            "p.actual_start_date as actualStartDate, " +
            "p.actual_end_date as actualEndDate, " +
            "p.schedule_performance_index as schedulePerformanceIndex, " +
            "DATEDIFF(p.planned_end_date, p.planned_start_date) as plannedDuration, " +
            "CASE WHEN p.actual_end_date IS NOT NULL " +
            "THEN DATEDIFF(p.actual_end_date, p.actual_start_date) " +
            "ELSE DATEDIFF(CURDATE(), p.actual_start_date) END as actualDuration, " +
            "CASE WHEN p.actual_end_date IS NOT NULL " +
            "THEN DATEDIFF(p.actual_end_date, p.planned_end_date) " +
            "ELSE DATEDIFF(CURDATE(), p.planned_end_date) END as delayDays, " +
            "p.project_health_status as healthStatus " +
            "FROM projects p WHERE p.id = #{projectId}")
    ProjectStatisticsVO.ScheduleStats getScheduleStats(@Param("projectId") Long projectId);
    
    /**
     * 获取项目团队统计 - 使用增强字段
     */
    @Select("SELECT " +
            "COUNT(DISTINCT pm.user_id) as teamSize, " +
            "COUNT(DISTINCT te.user_id) as activeMembers, " +
            "COALESCE(AVG(te.duration), 0) as avgDailyHours, " +
            "COALESCE(AVG(pm.productivity_index), 1.0) as avgProductivityIndex, " +
            "COALESCE(AVG(pm.performance_rating), 0) as avgPerformanceRating, " +
            "COALESCE(AVG(pm.allocation_percentage), 100) as avgAllocation " +
            "FROM project_members pm " +
            "LEFT JOIN time_entries te ON pm.user_id = te.user_id AND te.project_id = pm.project_id " +
            "WHERE pm.project_id = #{projectId}")
    ProjectStatisticsVO.TeamStats getTeamStats(@Param("projectId") Long projectId);
    
    /**
     * 获取团队成员统计 - 使用增强字段
     */
    @Select("SELECT " +
            "u.real_name as memberName, " +
            "pm.role as role, " +
            "pm.skill_level as skillLevel, " +
            "pm.hourly_rate as hourlyRate, " +
            "pm.performance_rating as performanceRating, " +
            "pm.productivity_index as productivityIndex, " +
            "COALESCE(SUM(te.duration), 0) as hoursWorked, " +
            "COUNT(DISTINCT t.id) as tasksCompleted, " +
            "COALESCE(AVG(te.productivity_score), 0) as avgProductivityScore " +
            "FROM project_members pm " +
            "JOIN users u ON pm.user_id = u.id " +
            "LEFT JOIN time_entries te ON pm.user_id = te.user_id AND te.project_id = pm.project_id AND te.status = 'APPROVED' " +
            "LEFT JOIN tasks t ON pm.user_id = t.assignee_id AND t.project_id = pm.project_id AND t.status = 'COMPLETED' " +
            "WHERE pm.project_id = #{projectId} " +
            "GROUP BY pm.user_id, u.real_name, pm.role, pm.skill_level, pm.hourly_rate, pm.performance_rating, pm.productivity_index")
    List<ProjectStatisticsVO.TeamMemberStats> getTeamMemberStats(@Param("projectId") Long projectId);
    
    /**
     * 获取项目里程碑信息 - 使用增强字段
     */
    @Select("SELECT " +
            "pm.milestone_name as milestoneName, " +
            "pm.milestone_type as milestoneType, " +
            "pm.planned_date as plannedDate, " +
            "pm.actual_date as actualDate, " +
            "pm.status as status, " +
            "pm.completion_percentage as completionPercentage, " +
            "pm.health_status as healthStatus, " +
            "CASE WHEN pm.actual_date IS NOT NULL " +
            "THEN DATEDIFF(pm.actual_date, pm.planned_date) " +
            "ELSE DATEDIFF(CURDATE(), pm.planned_date) END as delayDays, " +
            "u.real_name as responsiblePerson " +
            "FROM project_milestones pm " +
            "LEFT JOIN users u ON pm.responsible_person_id = u.id " +
            "WHERE pm.project_id = #{projectId} " +
            "ORDER BY pm.planned_date")
    List<ProjectStatisticsVO.MilestoneInfo> getMilestones(@Param("projectId") Long projectId);
    
    /**
     * 获取成本分解 - 使用增强字段
     */
    @Select("SELECT " +
            "pc.cost_type as costType, " +
            "pc.cost_category as costCategory, " +
            "SUM(pc.amount) as amount, " +
            "SUM(pc.cost_variance) as variance, " +
            "pc.budget_impact as budgetImpact, " +
            "pc.description as description " +
            "FROM project_costs pc " +
            "WHERE pc.project_id = #{projectId} AND pc.status = 'APPROVED' " +
            "GROUP BY pc.cost_type, pc.cost_category, pc.budget_impact, pc.description")
    List<ProjectStatisticsVO.CostBreakdown> getCostBreakdown(@Param("projectId") Long projectId);
    
    /**
     * 获取进度趋势数据
     */
    @Select("SELECT " +
            "te.work_date as date, " +
            "SUM(te.duration) as value, " +
            "AVG(te.productivity_score) as productivityScore " +
            "FROM time_entries te " +
            "WHERE te.project_id = #{projectId} AND te.status = 'APPROVED' " +
            "AND te.work_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY te.work_date " +
            "ORDER BY te.work_date")
    List<ProjectStatisticsVO.TrendData> getProgressTrend(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 获取成本趋势数据
     */
    @Select("SELECT " +
            "pc.incurred_date as date, " +
            "SUM(pc.amount) as value " +
            "FROM project_costs pc " +
            "WHERE pc.project_id = #{projectId} AND pc.status = 'APPROVED' " +
            "AND pc.incurred_date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY pc.incurred_date " +
            "ORDER BY pc.incurred_date")
    List<ProjectStatisticsVO.TrendData> getCostTrend(
            @Param("projectId") Long projectId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    /**
     * 获取项目质量统计 - 使用增强字段
     */
    @Select("SELECT " +
            "p.quality_score as qualityScore, " +
            "p.client_satisfaction as clientSatisfaction, " +
            "COALESCE(defect_stats.defect_count, 0) as defectCount, " +
            "COALESCE(rework_stats.rework_hours, 0) as reworkHours, " +
            "COALESCE(AVG(t.quality_score), 0) as avgTaskQuality, " +
            "COALESCE(AVG(te.productivity_score), 0) as avgProductivity " +
            "FROM projects p " +
            "LEFT JOIN tasks t ON p.id = t.project_id " +
            "LEFT JOIN time_entries te ON p.id = te.project_id AND te.status = 'APPROVED' " +
            "LEFT JOIN (" +
            "  SELECT project_id, COUNT(*) as defect_count " +
            "  FROM tasks " +
            "  WHERE project_id = #{projectId} AND (description LIKE '%缺陷%' OR description LIKE '%bug%') " +
            "  GROUP BY project_id" +
            ") defect_stats ON p.id = defect_stats.project_id " +
            "LEFT JOIN (" +
            "  SELECT project_id, SUM(duration) as rework_hours " +
            "  FROM time_entries " +
            "  WHERE project_id = #{projectId} AND (description LIKE '%返工%' OR description LIKE '%修复%') " +
            "  GROUP BY project_id" +
            ") rework_stats ON p.id = rework_stats.project_id " +
            "WHERE p.id = #{projectId} " +
            "GROUP BY p.id, p.quality_score, p.client_satisfaction, defect_stats.defect_count, rework_stats.rework_hours")
    ProjectStatisticsVO.QualityStats getQualityStats(@Param("projectId") Long projectId);
    
    // ========== 增强统计方法 ==========
    
    /**
     * 计算人工成本
     * 统计思路：从time_entries表和users表关联，计算 Σ(工时 × 月薪/176)
     * 月薪转时薪公式：月薪 ÷ 176小时（22天 × 8小时）
     */
    @Select("SELECT COALESCE(SUM(te.duration * (u.monthly_salary / 176)), 0) " +
            "FROM time_entries te " +
            "JOIN users u ON te.user_id = u.id " +
            "WHERE te.project_id = #{projectId} AND te.status = 'APPROVED'")
    BigDecimal calculateLaborCost(@Param("projectId") Long projectId);
    
    /**
     * 计算其他成本
     * 统计思路：从项目费用记录表中统计非人工类成本
     */
    @Select("SELECT COALESCE(SUM(amount), 0) " +
            "FROM project_costs " +
            "WHERE project_id = #{projectId} AND cost_type != 'LABOR' AND status = 'APPROVED'")
    BigDecimal calculateOtherCost(@Param("projectId") Long projectId);
    
    /**
     * 获取团队规模
     * 统计思路：project_members表中该项目的所有成员数量
     */
    @Select("SELECT COUNT(DISTINCT user_id) " +
            "FROM project_members " +
            "WHERE project_id = #{projectId}")
    Integer getTeamSize(@Param("projectId") Long projectId);
    
    /**
     * 获取活跃成员数量
     * 统计思路：近N天内在该项目有工时记录的成员数量
     */
    @Select("SELECT COUNT(DISTINCT te.user_id) " +
            "FROM time_entries te " +
            "WHERE te.project_id = #{projectId} " +
            "AND te.work_date >= DATE_SUB(CURDATE(), INTERVAL #{days} DAY) " +
            "AND te.status = 'APPROVED'")
    Integer getActiveMembersCount(@Param("projectId") Long projectId, @Param("days") Integer days);
    
    /**
     * 获取日均工时
     * 统计思路：项目总工时 / 项目进行天数 / 团队规模
     */
    @Select("SELECT COALESCE(AVG(daily_total.total_hours), 0) " +
            "FROM (SELECT work_date, SUM(duration) as total_hours " +
            "      FROM time_entries " +
            "      WHERE project_id = #{projectId} AND status = 'APPROVED' " +
            "      GROUP BY work_date) daily_total")
    BigDecimal getAvgDailyHours(@Param("projectId") Long projectId);
    
    /**
     * 获取团队效率
     * 统计思路：工作效率 = (实际完成工时 / 预估工时) × 100%
     */
    @Select("SELECT CASE " +
            "WHEN SUM(t.estimated_hours) > 0 " +
            "THEN (SUM(t.actual_hours) / SUM(t.estimated_hours)) * 100 " +
            "ELSE 0 END " +
            "FROM tasks t " +
            "WHERE t.project_id = #{projectId}")
    BigDecimal getTeamEfficiency(@Param("projectId") Long projectId);
    
    /**
     * 获取里程碑进度
     * 统计思路：里程碑关联任务的完成度平均值
     */
    @Select("SELECT COALESCE(AVG(t.completion_percentage), 0) " +
            "FROM milestone_tasks mt " +
            "JOIN tasks t ON mt.task_id = t.id " +
            "WHERE mt.milestone_id = #{milestoneId}")
    Integer getMilestoneProgress(@Param("milestoneId") Long milestoneId);
    
    /**
     * 获取部门相关项目统计（部门负责人权限）
     * 统计思路：根据用户所属部门，统计该部门参与的所有项目
     */
    @Select("SELECT DISTINCT p.id, p.project_name, p.status " +
            "FROM projects p " +
            "JOIN project_members pm ON p.id = pm.project_id " +
            "JOIN users u ON pm.user_id = u.id " +
            "JOIN organizations o ON u.organization_id = o.id " +
            "WHERE o.id = #{departmentId} OR o.parent_id = #{departmentId}")
    List<Map<String, Object>> getDepartmentProjects(@Param("departmentId") Long departmentId);
    
    /**
     * 获取部门成员工时统计
     * 统计思路：统计部门所有成员在各项目的工时分布
     */
    @Select("SELECT " +
            "u.real_name as userName, " +
            "p.project_name as projectName, " +
            "SUM(te.duration) as totalHours, " +
            "COUNT(DISTINCT te.work_date) as workDays " +
            "FROM time_entries te " +
            "JOIN users u ON te.user_id = u.id " +
            "JOIN projects p ON te.project_id = p.id " +
            "JOIN organizations o ON u.organization_id = o.id " +
            "WHERE (o.id = #{departmentId} OR o.parent_id = #{departmentId}) " +
            "AND te.status = 'APPROVED' " +
            "AND te.work_date >= #{startDate} AND te.work_date <= #{endDate} " +
            "GROUP BY u.id, p.id " +
            "ORDER BY u.real_name, totalHours DESC")
    List<Map<String, Object>> getDepartmentMemberHours(
            @Param("departmentId") Long departmentId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}