package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timetracking.entity.ProjectCost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 项目成本Mapper
 */
@Mapper
public interface ProjectCostMapper extends BaseMapper<ProjectCost> {
    
    /**
     * 根据项目ID查询成本列表
     */
    @Select("SELECT * FROM project_costs WHERE project_id = #{projectId} ORDER BY incurred_date DESC")
    List<ProjectCost> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 获取项目总成本
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM project_costs WHERE project_id = #{projectId}")
    BigDecimal getTotalCostByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目已审批成本
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM project_costs WHERE project_id = #{projectId} AND status = 'APPROVED'")
    BigDecimal getApprovedCostByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目待审批成本
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM project_costs WHERE project_id = #{projectId} AND status = 'PENDING'")
    BigDecimal getPendingCostByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目成本条目数量
     */
    @Select("SELECT COUNT(*) FROM project_costs WHERE project_id = #{projectId}")
    Integer getCostItemCountByProject(@Param("projectId") Long projectId);
    
    /**
     * 按类型分组获取成本
     */
    @Select("SELECT cost_type as costType, COALESCE(SUM(amount), 0) as totalAmount, COUNT(*) as itemCount " +
            "FROM project_costs WHERE project_id = #{projectId} AND status = 'APPROVED' " +
            "GROUP BY cost_type ORDER BY totalAmount DESC")
    List<Map<String, Object>> getCostByTypeAndProject(@Param("projectId") Long projectId);
}