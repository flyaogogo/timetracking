package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("organizations")
public class Organization {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String orgName;
    
    private String orgCode;
    
    private Long parentId;
    
    private OrgType orgType;
    
    private Integer orgLevel;
    
    private Long managerId;
    
    private String costCenter;
    
    private BigDecimal budgetAmount;
    
    private OrgStatus status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 关联字段
    @TableField(exist = false)
    private String managerName;
    
    @TableField(exist = false)
    private List<Organization> children;
    
    @TableField(exist = false)
    private Integer memberCount;
    
    @TableField(exist = false)
    private BigDecimal actualCost;
    
    @TableField(exist = false)
    private String displayName;
    
    public enum OrgType {
        COMPANY, DEPARTMENT, TEAM, PROJECT_GROUP
    }
    
    public enum OrgStatus {
        ACTIVE, INACTIVE
    }
}