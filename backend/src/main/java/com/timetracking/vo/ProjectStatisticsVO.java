package com.timetracking.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class ProjectStatisticsVO {
    
    // 项目基本信息
    private Long projectId;
    private String projectName;
    private String projectCode;
    private String status;
    private String managerName;
    
    // 进度统计
    private ProgressStats progressStats;
    
    // 财务统计
    private FinancialStats financialStats;
    
    // 工期统计
    private ScheduleStats scheduleStats;
    
    // 人员统计
    private TeamStats teamStats;
    
    // 质量统计
    private QualityStats qualityStats;
    
    // 里程碑统计
    private List<MilestoneInfo> milestones;
    
    // 成本明细
    private List<CostBreakdown> costBreakdown;
    
    // 趋势数据
    private List<TrendData> progressTrend;
    private List<TrendData> costTrend;
    
    @Data
    public static class ProgressStats {
        private BigDecimal progressPercentage; // 项目进度百分比
        private Integer completedTasks; // 已完成任务数
        private Integer totalTasks; // 总任务数
        private Integer pendingTasks; // 待完成任务数
        private BigDecimal actualHours; // 实际工时
        private BigDecimal estimatedHours; // 预估工时
        private BigDecimal remainingHours; // 剩余工时
        private BigDecimal hoursUtilization; // 工时利用率
    }
    
    @Data
    public static class FinancialStats {
        private BigDecimal contractAmount; // 合同金额
        private BigDecimal budgetAmount; // 预算金额
        private BigDecimal actualCost; // 实际成本
        private BigDecimal laborCost; // 人工成本
        private BigDecimal otherCost; // 其他成本
        private BigDecimal costUtilization; // 成本使用率
        private BigDecimal profitMargin; // 利润率
        private BigDecimal estimatedFinalCost; // 预估最终成本
        private String costStatus; // 成本状态：UNDER_BUDGET, ON_BUDGET, OVER_BUDGET
    }
    
    @Data
    public static class ScheduleStats {
        private LocalDate plannedStartDate; // 计划开始日期
        private LocalDate plannedEndDate; // 计划结束日期
        private LocalDate actualStartDate; // 实际开始日期
        private LocalDate actualEndDate; // 实际结束日期
        private LocalDate estimatedEndDate; // 预估结束日期
        private Integer plannedDuration; // 计划工期（天）
        private Integer actualDuration; // 实际工期（天）
        private Integer delayDays; // 延期天数
        private BigDecimal schedulePerformance; // 进度绩效指数
        private String scheduleStatus; // 进度状态：ON_SCHEDULE, AHEAD, DELAYED
    }
    
    @Data
    public static class TeamStats {
        private Integer teamSize; // 团队规模
        private Integer activeMembers; // 活跃成员数
        private BigDecimal avgDailyHours; // 平均日工时
        private BigDecimal teamEfficiency; // 团队效率
        private BigDecimal efficiency; // 工作效率（实际/预估工时比）
        private BigDecimal resourceUtilization; // 资源利用率
        private Map<String, Integer> roleDistribution; // 角色分布
        private Map<String, BigDecimal> memberContribution; // 成员贡献度
        private List<TeamMemberStats> memberStats; // 成员统计
    }
    
    @Data
    public static class QualityStats {
        private BigDecimal qualityScore; // 质量评分
        private Integer defectCount; // 缺陷数量
        private BigDecimal reworkHours; // 返工工时
        private BigDecimal reworkRate; // 返工率
        private BigDecimal clientSatisfaction; // 客户满意度
        private Integer codeReviewCount; // 代码评审次数
        private BigDecimal testCoverage; // 测试覆盖率
    }
    
    @Data
    public static class MilestoneInfo {
        private Long id; // 里程碑ID
        private String name; // 里程碑名称
        private String milestoneName;
        private LocalDate plannedDate;
        private LocalDate actualDate;
        private String status;
        private Integer delayDays;
        private String responsiblePerson;
        private Integer progress; // 进度百分比
        private boolean isDelayed; // 是否延期
        private String description; // 描述
    }
    
    @Data
    public static class CostBreakdown {
        private String costType;
        private String costCategory;
        private BigDecimal amount;
        private BigDecimal percentage;
        private String description;
    }
    
    @Data
    public static class TrendData {
        private LocalDate date;
        private BigDecimal value;
        private String label;
    }
    
    @Data
    public static class TeamMemberStats {
        private String memberName;
        private String role;
        private BigDecimal hoursWorked;
        private BigDecimal contribution;
        private BigDecimal efficiency;
        private Integer tasksCompleted;
    }
}