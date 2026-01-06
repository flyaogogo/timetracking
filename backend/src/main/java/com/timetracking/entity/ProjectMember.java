package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("project_members")
public class ProjectMember {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long userId;
    
    private ProjectRole role;
    
    private LocalDate joinDate;
    
    // 项目内权限字段
    private Boolean isProjectManager;
    
    private Boolean isTechLeader;
    
    private Boolean canApproveTimesheet;
    
    private Boolean canManageTasks;
    
    private Boolean canViewReports;
    
    private LocalDate effectiveDate;
    
    private LocalDate expiryDate;
    
    // 统计分析字段
    private BigDecimal allocationPercentage;
    
    private BigDecimal hourlyRate;
    
    private SkillLevel skillLevel;
    
    private BigDecimal performanceRating;
    
    private BigDecimal productivityIndex;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 关联字段
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String projectName;
    
    @TableField(exist = false)
    private String userRealName;
    
    @TableField(exist = false)
    private String userRole;
    
    @TableField(exist = false)
    private String username;
    
    @TableField(exist = false)
    private String email;
    
    @TableField(exist = false)
    private String department;
    
    @TableField(exist = false)
    private String position;
    
    @TableField(exist = false)
    private java.util.List<String> permissions;
    
    // 显示角色字段（用于解决角色显示不一致问题）
    @TableField(exist = false)
    private String displayRole;
    
    @TableField(exist = false)
    private String displayRoleName;
    
    public enum ProjectRole {
        MANAGER, DEVELOPER, TESTER, DESIGNER
    }
    
    public enum SkillLevel {
        JUNIOR, INTERMEDIATE, SENIOR, EXPERT
    }
}