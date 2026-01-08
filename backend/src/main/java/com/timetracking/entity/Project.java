package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("projects")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String projectName;
    
    private String projectCode;
    
    private String description;
    
    private ProjectType projectType;
    
    private Priority priority;
    
    // 工时相关
    private BigDecimal estimatedHours;
    
    private BigDecimal actualHours;
    
    // 财务相关
    private BigDecimal contractAmount;
    
    private BigDecimal budgetAmount;
    
    private BigDecimal laborBudget;
    
    private BigDecimal otherBudget;
    
    private BigDecimal actualLaborCost;
    
    private BigDecimal actualOtherCost;
    
    // 时间相关
    private LocalDate plannedStartDate;
    
    private LocalDate plannedEndDate;
    
    private LocalDate actualStartDate;
    
    private LocalDate actualEndDate;
    
    private LocalDate startDate; // 保持兼容性
    
    private LocalDate endDate; // 保持兼容性
    
    // 客户和合同信息
    private String clientName;
    
    private String clientContact;
    
    private String contractNumber;
    
    // 项目状态和管理
    private ProjectStatus status = ProjectStatus.PLANNING;
    
    private Long managerId;
    
    private Long techLeaderId;
    
    // 风险和质量
    private RiskLevel riskLevel;
    
    private BigDecimal qualityScore;
    
    private BigDecimal clientSatisfaction;
    
    // 新增统计分析字段
    private BigDecimal baselineBudget;
    
    private BigDecimal baselineHours;
    
    private BigDecimal earnedValue;
    
    private BigDecimal plannedValue;
    
    private BigDecimal costPerformanceIndex;
    
    private BigDecimal schedulePerformanceIndex;
    
    private ProjectHealthStatus projectHealthStatus;
    
    private BigDecimal completionPercentage;
    
    private BigDecimal resourceUtilization;
    
    private BigDecimal teamVelocity;
    
    private BigDecimal burnRate;
    
    private BigDecimal roiPercentage;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 关联字段
    @TableField(exist = false)
    private String managerName;
    
    @TableField(exist = false)
    private String techLeaderName;
    
    @TableField(exist = false)
    private Integer delayDays;
    
    public enum ProjectType {
        DEVELOPMENT, MAINTENANCE, RESEARCH
    }
    
    public enum Priority {
        HIGH, MEDIUM, LOW
    }
    
    public enum ProjectStatus {
        PLANNING, IN_PROGRESS, COMPLETED, PAUSED, CANCELLED
    }
    
    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum ProjectHealthStatus {
        GREEN, YELLOW, RED
    }
}