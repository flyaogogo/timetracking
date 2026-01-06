package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("tasks")
public class Task {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String taskName;
    
    private String description;
    
    private Long projectId;
    
    private Long parentId;
    
    private TaskType taskType;
    
    private Integer priority;
    
    private Integer difficulty;
    
    private BigDecimal estimatedHours;
    
    private BigDecimal actualHours;
    
    private Integer progress;
    
    private TaskStatus status;
    
    private Long assigneeId;
    
    private Long reviewerId;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    // 新增统计分析字段
    private BigDecimal baselineHours;
    
    private LocalDate actualStartDate;
    
    private LocalDate actualEndDate;
    
    private BigDecimal completionPercentage;
    
    private BigDecimal effortVariance;
    
    private TaskComplexity taskComplexity;
    
    private Integer businessValue;
    
    private BigDecimal qualityScore;
    
    private Integer storyPoints;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 关联字段
    @TableField(exist = false)
    private String projectName;
    
    @TableField(exist = false)
    private String assigneeName;
    
    @TableField(exist = false)
    private String reviewerName;
    
    @TableField(exist = false)
    private Integer delayDays;
    
    public enum TaskType {
        DEVELOPMENT, TESTING, DESIGN, DOCUMENT
    }
    
    public enum TaskStatus {
        TODO, IN_PROGRESS, REVIEW, COMPLETED, PAUSED, CANCELLED
    }
    
    public enum TaskComplexity {
        LOW, MEDIUM, HIGH, VERY_HIGH
    }
}