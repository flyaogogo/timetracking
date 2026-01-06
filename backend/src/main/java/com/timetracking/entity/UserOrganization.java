package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_organizations")
public class UserOrganization {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long organizationId;
    
    private Boolean isPrimary;
    
    private String position;
    
    private LocalDate joinDate;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 关联字段
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String orgName;
}