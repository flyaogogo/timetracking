package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 项目里程碑实体
 */
@Data
@TableName("project_milestones")
public class ProjectMilestone {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long projectId;
    
    private String milestoneName;
    
    private MilestoneType milestoneType;
    
    private LocalDate plannedDate;
    
    private LocalDate actualDate;
    
    private MilestoneStatus status;
    
    private Integer completionPercentage;
    
    private HealthStatus healthStatus;
    
    private Long responsiblePersonId;
    
    private String description;
    
    private String deliverables;
    
    private String acceptanceCriteria;
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
    
    /**
     * 里程碑类型枚举
     */
    public enum MilestoneType {
        PLANNING("规划阶段"),
        DEVELOPMENT("开发阶段"),
        TESTING("测试阶段"),
        DEPLOYMENT("部署阶段"),
        DELIVERY("交付阶段");
        
        private final String description;
        
        MilestoneType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 里程碑状态枚举
     */
    public enum MilestoneStatus {
        PENDING("待开始"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        DELAYED("延期");
        
        private final String description;
        
        MilestoneStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        GREEN("健康"),
        YELLOW("警告"),
        RED("风险");
        
        private final String description;
        
        HealthStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}