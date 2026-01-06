package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timetracking.entity.Organization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrganizationMapper extends BaseMapper<Organization> {
    
    /**
     * 获取用户所属组织
     */
    @Select("SELECT o.*, u.real_name as manager_name " +
            "FROM organizations o " +
            "LEFT JOIN users u ON o.manager_id = u.id " +
            "INNER JOIN user_organizations uo ON o.id = uo.organization_id " +
            "WHERE uo.user_id = #{userId} AND o.status = 'ACTIVE'")
    List<Organization> selectUserOrganizations(@Param("userId") Long userId);
    
    /**
     * 获取组织成员数量
     */
    @Select("SELECT COUNT(DISTINCT uo.user_id) " +
            "FROM user_organizations uo " +
            "WHERE uo.organization_id = #{orgId}")
    Integer getMemberCount(@Param("orgId") Long orgId);
    
    /**
     * 获取组织项目数量
     */
    @Select("SELECT COUNT(DISTINCT p.id) " +
            "FROM projects p " +
            "INNER JOIN project_members pm ON p.id = pm.project_id " +
            "INNER JOIN user_organizations uo ON pm.user_id = uo.user_id " +
            "WHERE uo.organization_id = #{orgId}")
    Integer getProjectCount(@Param("orgId") Long orgId);
    
    /**
     * 获取组织本月工时
     */
    @Select("SELECT COALESCE(SUM(te.duration), 0) " +
            "FROM time_entries te " +
            "INNER JOIN user_organizations uo ON te.user_id = uo.user_id " +
            "WHERE uo.organization_id = #{orgId} " +
            "AND te.work_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01') " +
            "AND te.status = 'APPROVED'")
    Double getMonthlyHours(@Param("orgId") Long orgId);
    
    /**
     * 获取组织预算信息（简化版本）
     */
    @Select("SELECT " +
            "o.budget_amount as budgetAmount, " +
            "0 as actualCost, " +
            "0 as utilizationRate " +
            "FROM organizations o " +
            "WHERE o.id = #{orgId}")
    Map<String, Object> getBudgetInfo(@Param("orgId") Long orgId);
    
    /**
     * 获取组织详细信息（包含负责人姓名）
     */
    @Select("SELECT o.*, u.real_name as manager_name " +
            "FROM organizations o " +
            "LEFT JOIN users u ON o.manager_id = u.id " +
            "WHERE o.id = #{id}")
    Organization selectOrganizationWithManager(@Param("id") Long id);
}