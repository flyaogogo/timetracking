package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("project_role_permissions")
public class ProjectRolePermission {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private Long userId;
    
    private PermissionType permissionType;
    
    private Long grantedBy;
    
    private LocalDateTime grantedDate;
    
    private LocalDate effectiveDate;
    
    private LocalDate expiryDate;
    
    private Boolean isActive;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 关联字段
    @TableField(exist = false)
    private String projectName;
    
    @TableField(exist = false)
    private String userName;
    
    @TableField(exist = false)
    private String grantedByName;
    
    public enum PermissionType {
        PROJECT_MANAGEMENT("项目管理"),
        TASK_MANAGEMENT("任务管理"),
        TIMESHEET_APPROVAL("工时审核"),
        REPORT_VIEW("报表查看"),
        COST_MANAGEMENT("成本管理"),
        MILESTONE_MANAGEMENT("里程碑管理"),
        TEAM_MANAGEMENT("团队管理");
        
        private final String description;
        
        PermissionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}