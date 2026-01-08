package com.timetracking.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 项目状态变更历史实体类
 */
@Data
@TableName("project_status_history")
public class ProjectStatusHistory {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 变更前状态
     */
    private String oldStatus;

    /**
     * 变更后状态
     */
    private String newStatus;

    /**
     * 变更原因
     */
    private String changeReason;

    /**
     * 变更人
     */
    private String changedBy;

    /**
     * 变更时间
     */
    private Date changedTime;
}
