package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("time_entries")
public class TimeEntry {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long projectId;
    
    private Long taskId;
    
    private LocalDate workDate;
    
    private LocalTime startTime;
    
    private LocalTime endTime;
    
    private BigDecimal duration;
    
    private WorkType workType;
    
    private String description;
    
    private ApprovalStatus status;
    
    private Long approverId;
    
    private LocalDateTime approveTime;
    
    private String approveComment;
    
    // 新增统计分析字段
    private ActivityType activityType;
    
    private BigDecimal productivityScore;
    
    private Boolean billableFlag;
    
    private BigDecimal billingRate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 关联字段
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String projectName;
    
    @TableField(exist = false)
    private String taskName;
    
    @TableField(exist = false)
    private String approverName;
    
    public enum WorkType {
        NORMAL, OVERTIME, HOLIDAY
    }
    
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
    
    public enum ActivityType {
        TESTING, REVIEW, DOCUMENTATION, RESEARCH, DEBUGGING, CODING, MEETING, ANALYSIS, LEARNING
    }
}