package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timetracking.entity.ProjectMilestone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目里程碑Mapper
 */
@Mapper
public interface ProjectMilestoneMapper extends BaseMapper<ProjectMilestone> {
    
    /**
     * 根据项目ID查询里程碑列表
     */
    @Select("SELECT * FROM project_milestones WHERE project_id = #{projectId} ORDER BY planned_date ASC")
    List<ProjectMilestone> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 获取项目总里程碑数
     */
    @Select("SELECT COUNT(*) FROM project_milestones WHERE project_id = #{projectId}")
    Integer getTotalMilestonesByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目已完成里程碑数
     */
    @Select("SELECT COUNT(*) FROM project_milestones WHERE project_id = #{projectId} AND status = 'COMPLETED'")
    Integer getCompletedMilestonesByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目进行中里程碑数
     */
    @Select("SELECT COUNT(*) FROM project_milestones WHERE project_id = #{projectId} AND status = 'IN_PROGRESS'")
    Integer getInProgressMilestonesByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目延期里程碑数
     */
    @Select("SELECT COUNT(*) FROM project_milestones WHERE project_id = #{projectId} AND status = 'DELAYED'")
    Integer getDelayedMilestonesByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目平均完成度
     */
    @Select("SELECT AVG(completion_percentage) FROM project_milestones WHERE project_id = #{projectId}")
    Double getAvgProgressByProject(@Param("projectId") Long projectId);
}