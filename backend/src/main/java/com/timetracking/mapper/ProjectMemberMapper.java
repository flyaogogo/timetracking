package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timetracking.entity.ProjectMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 项目成员Mapper
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
    
    /**
     * 根据项目ID查询成员列表 - 增强版本，确保字段映射正确
     */
    @Select("SELECT " +
            "pm.id, " +
            "pm.project_id as projectId, " +
            "pm.user_id as userId, " +
            "pm.role, " +
            "pm.join_date as joinDate, " +
            "pm.is_project_manager as isProjectManager, " +
            "pm.is_tech_leader as isTechLeader, " +
            "pm.can_approve_timesheet as canApproveTimesheet, " +
            "pm.can_manage_tasks as canManageTasks, " +
            "pm.can_view_reports as canViewReports, " +
            "pm.effective_date as effectiveDate, " +
            "pm.expiry_date as expiryDate, " +
            "pm.allocation_percentage as allocationPercentage, " +
            "pm.hourly_rate as hourlyRate, " +
            "pm.skill_level as skillLevel, " +
            "pm.performance_rating as performanceRating, " +
            "pm.productivity_index as productivityIndex, " +
            "u.real_name as userRealName, " +
            "u.role as userRole, " +
            "u.username, " +
            "u.email, " +
            "u.department, " +
            "u.position " +
            "FROM project_members pm " +
            "LEFT JOIN users u ON pm.user_id = u.id " +
            "WHERE pm.project_id = #{projectId} " +
            "ORDER BY pm.join_date DESC")
    List<ProjectMember> selectByProjectId(@Param("projectId") Long projectId);
    
    /**
     * 根据项目ID和用户ID查询成员
     */
    @Select("SELECT pm.*, u.real_name as userRealName, u.role as userRole, " +
            "p.project_name as projectName " +
            "FROM project_members pm " +
            "LEFT JOIN users u ON pm.user_id = u.id " +
            "LEFT JOIN projects p ON pm.project_id = p.id " +
            "WHERE pm.project_id = #{projectId} AND pm.user_id = #{userId}")
    ProjectMember selectByProjectAndUser(@Param("projectId") Long projectId, @Param("userId") Long userId);
    
    /**
     * 获取项目总成员数
     */
    @Select("SELECT COUNT(*) FROM project_members WHERE project_id = #{projectId}")
    Integer getTotalMembersByProject(@Param("projectId") Long projectId);
    
    /**
     * 按角色分组获取成员统计
     */
    @Select("SELECT role, COUNT(*) as count " +
            "FROM project_members " +
            "WHERE project_id = #{projectId} " +
            "GROUP BY role")
    List<Map<String, Object>> getMembersByRoleAndProject(@Param("projectId") Long projectId);
    
    /**
     * 按技能等级分组获取成员统计
     */
    @Select("SELECT skill_level as skillLevel, COUNT(*) as count " +
            "FROM project_members " +
            "WHERE project_id = #{projectId} " +
            "GROUP BY skill_level")
    List<Map<String, Object>> getMembersBySkillAndProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目平均绩效评分
     */
    @Select("SELECT AVG(performance_rating) FROM project_members WHERE project_id = #{projectId}")
    Double getAvgPerformanceByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目平均生产力指数
     */
    @Select("SELECT AVG(productivity_index) FROM project_members WHERE project_id = #{projectId}")
    Double getAvgProductivityByProject(@Param("projectId") Long projectId);
    
    /**
     * 获取项目中的所有项目经理
     */
    @Select("SELECT pm.*, u.real_name as userRealName, u.role as userRole " +
            "FROM project_members pm " +
            "LEFT JOIN users u ON pm.user_id = u.id " +
            "WHERE pm.project_id = #{projectId} AND pm.is_project_manager = TRUE " +
            "AND (pm.effective_date IS NULL OR pm.effective_date <= CURDATE()) " +
            "AND (pm.expiry_date IS NULL OR pm.expiry_date >= CURDATE())")
    List<ProjectMember> selectProjectManagers(@Param("projectId") Long projectId);
    
    /**
     * 获取项目中的所有技术负责人
     */
    @Select("SELECT pm.*, u.real_name as userRealName, u.role as userRole " +
            "FROM project_members pm " +
            "LEFT JOIN users u ON pm.user_id = u.id " +
            "WHERE pm.project_id = #{projectId} AND pm.is_tech_leader = TRUE " +
            "AND (pm.effective_date IS NULL OR pm.effective_date <= CURDATE()) " +
            "AND (pm.expiry_date IS NULL OR pm.expiry_date >= CURDATE())")
    List<ProjectMember> selectTechLeaders(@Param("projectId") Long projectId);
    
    /**
     * 获取有工时审核权限的成员
     */
    @Select("SELECT pm.*, u.real_name as userRealName, u.role as userRole " +
            "FROM project_members pm " +
            "LEFT JOIN users u ON pm.user_id = u.id " +
            "WHERE pm.project_id = #{projectId} AND pm.can_approve_timesheet = TRUE " +
            "AND (pm.effective_date IS NULL OR pm.effective_date <= CURDATE()) " +
            "AND (pm.expiry_date IS NULL OR pm.expiry_date >= CURDATE())")
    List<ProjectMember> selectTimesheetApprovers(@Param("projectId") Long projectId);
    
    /**
     * 检查用户是否为项目成员
     */
    @Select("SELECT COUNT(*) > 0 FROM project_members " +
            "WHERE project_id = #{projectId} AND user_id = #{userId}")
    boolean isProjectMember(@Param("projectId") Long projectId, @Param("userId") Long userId);
    
    /**
     * 获取用户在所有项目中的角色信息（包含正确的角色显示）
     */
    @Select("SELECT p.id as projectId, p.project_name as projectName, p.status, " +
            "CASE " +
            "  WHEN p.manager_id = pm.user_id THEN 'MANAGER' " +
            "  WHEN pm.is_project_manager = 1 THEN 'MANAGER' " +
            "  WHEN pm.is_tech_leader = 1 THEN 'TECH_LEADER' " +
            "  ELSE pm.role " +
            "END as displayRole, " +
            "pm.role as originalRole, " +
            "pm.is_project_manager as isProjectManager, " +
            "pm.is_tech_leader as isTechLeader, " +
            "pm.join_date as joinDate " +
            "FROM project_members pm " +
            "INNER JOIN projects p ON pm.project_id = p.id " +
            "WHERE pm.user_id = #{userId} " +
            "ORDER BY pm.join_date DESC")
    List<Map<String, Object>> getUserProjectRolesWithDisplay(@Param("userId") Long userId);
    
    /**
     * 检查用户是否在任何项目中担任项目经理
     */
    @Select("SELECT COUNT(*) FROM project_members " +
            "WHERE user_id = #{userId} AND is_project_manager = TRUE " +
            "AND (effective_date IS NULL OR effective_date <= CURDATE()) " +
            "AND (expiry_date IS NULL OR expiry_date >= CURDATE())")
    int isProjectManagerInAnyProject(@Param("userId") Long userId);
}