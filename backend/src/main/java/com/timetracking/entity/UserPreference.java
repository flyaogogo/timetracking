package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_preferences")
public class UserPreference {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private Long defaultProjectId;
    
    private BigDecimal defaultBillingRate;
    
    private WorkLocation defaultWorkLocation;
    
    private Boolean autoFillYesterdayProject;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    // 关联字段
    @TableField(exist = false)
    private String defaultProjectName;
    
    public enum WorkLocation {
        OFFICE, HOME, CLIENT_SITE
    }
}