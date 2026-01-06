package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目成本实体
 */
@Data
@TableName("project_costs")
public class ProjectCost {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private CostType costType;
    
    private String costCategory;
    
    private BigDecimal amount;
    
    private String currency;
    
    private String description;
    
    private LocalDate incurredDate;
    
    private Long approvedBy;
    
    private LocalDate approvalDate;
    
    private CostStatus status;
    
    private String invoiceNumber;
    
    private BigDecimal costVariance;
    
    private BudgetImpact budgetImpact;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    @TableField(exist = false)
    private String vendorName; // 供应商名称，用于前端显示
    
    @TableField(exist = false)
    private LocalDateTime approveTime; // 审批时间，用于兼容
    
    @TableField(exist = false)
    private Long approverId; // 审批人ID，用于兼容
    
    /**
     * 成本类型枚举
     */
    public enum CostType {
        LABOR("人工成本"),
        EQUIPMENT("设备费用"),
        TRAVEL("差旅费用"),
        OUTSOURCING("外包费用"),
        SOFTWARE("软件费用"),
        HARDWARE("硬件费用"),
        OTHER("其他费用");
        
        private final String description;
        
        CostType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 预算影响枚举
     */
    public enum BudgetImpact {
        WITHIN_BUDGET("预算内"),
        BUDGET_REALLOCATION("预算重新分配"),
        BUDGET_INCREASE("预算增加");
        
        private final String description;
        
        BudgetImpact(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 成本状态枚举
     */
    public enum CostStatus {
        PENDING("待审批"),
        APPROVED("已审批"),
        REJECTED("已拒绝");
        
        private final String description;
        
        CostStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}