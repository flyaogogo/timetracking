package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String realName;
    
    private String email;
    
    private String phone;
    
    private String department;
    
    private String position;
    
    private UserRole role;
    
    private java.math.BigDecimal monthlySalary;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 关联字段
    @TableField(exist = false)
    private String organizationName;
    
    @TableField(exist = false)
    private Long organizationId;
    
    public enum UserRole {
        ADMIN, PROJECT_MANAGER, PRODUCT_MANAGER, DEPARTMENT_MANAGER, DEVELOPER, TESTER
    }
}