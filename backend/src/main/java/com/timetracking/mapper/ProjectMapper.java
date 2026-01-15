package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timetracking.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
    
    @Select("SELECT p.*, u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date < CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days " +
            "FROM projects p " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "WHERE p.id = #{id}")
    Project selectProjectWithManager(@Param("id") Long id);
    
    @Select("SELECT p.*, u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date < CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days " +
            "FROM projects p " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "ORDER BY p.create_time DESC")
    IPage<Project> selectProjectsWithManager(Page<Project> page);
    
    /**
     * 查询所有项目（带详细信息）
     */
    @Select("<script>" +
            "SELECT p.*, u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date &lt; CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days " +
            "FROM projects p " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "WHERE (p.project_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.project_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    IPage<Project> selectAllProjectsWithDetails(Page<Project> page, @Param("keyword") String keyword);
    
    /**
     * 查询用户参与的项目（带权限信息）
     */
    @Select("<script>" +
            "SELECT p.id, p.project_name, p.project_code, p.description, p.project_type, p.priority, " +
            "p.estimated_hours, p.contract_amount, p.budget_amount, p.labor_budget, p.other_budget, " +
            "p.planned_start_date, p.planned_end_date, p.actual_start_date, p.actual_end_date, " +
            "p.start_date, p.end_date, p.client_name, p.client_contact, p.contract_number, p.status, " +
            "p.manager_id, p.tech_leader_id, p.risk_level, p.quality_score, p.client_satisfaction, " +
            "p.create_time, p.update_time, p.baseline_budget, p.baseline_hours, p.earned_value, " +
            "p.planned_value, p.cost_performance_index, p.schedule_performance_index, p.project_health_status, " +
            "p.completion_percentage, p.resource_utilization, p.team_velocity, p.burn_rate, p.roi_percentage, " +
            "u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date &lt; CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days, " +
            "CASE WHEN p.manager_id = #{userId} THEN 1 ELSE COALESCE(pm.is_project_manager, 0) END as is_project_manager, " +
            "COALESCE(pm.is_tech_leader, 0) as is_tech_leader, " +
            "COALESCE(pm.role, 'MANAGER') as role " +
            "FROM projects p " +
            "LEFT JOIN project_members pm ON p.id = pm.project_id AND pm.user_id = #{userId} " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "WHERE (pm.user_id IS NOT NULL OR p.manager_id = #{userId}) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (p.project_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.project_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    IPage<Project> selectUserProjectsWithPermission(Page<Project> page, @Param("userId") Long userId, @Param("keyword") String keyword);
    
    /**
     * 查询用户管理的项目
     */
    @Select("<script>" +
            "SELECT p.*, u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date &lt; CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days " +
            "FROM projects p " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "WHERE p.manager_id = #{userId} " +
            "ORDER BY p.create_time DESC" +
            "</script>")
    IPage<Project> selectManagedProjects(Page<Project> page, @Param("userId") Long userId);
    
    /**
     * 查询用户参与的项目
     */
    @Select("<script>" +
            "SELECT p.id, p.project_name, p.project_code, p.description, p.project_type, p.priority, " +
            "p.estimated_hours, p.contract_amount, p.budget_amount, p.labor_budget, p.other_budget, " +
            "p.planned_start_date, p.planned_end_date, p.actual_start_date, p.actual_end_date, " +
            "p.start_date, p.end_date, p.client_name, p.client_contact, p.contract_number, p.status, " +
            "p.manager_id, p.tech_leader_id, p.risk_level, p.quality_score, p.client_satisfaction, " +
            "p.create_time, p.update_time, p.baseline_budget, p.baseline_hours, p.earned_value, " +
            "p.planned_value, p.cost_performance_index, p.schedule_performance_index, p.project_health_status, " +
            "p.completion_percentage, p.resource_utilization, p.team_velocity, p.burn_rate, p.roi_percentage, " +
            "u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date &lt; CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days " +
            "FROM projects p " +
            "LEFT JOIN project_members pm ON p.id = pm.project_id AND pm.user_id = #{userId} " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "WHERE (pm.user_id IS NOT NULL OR p.manager_id = #{userId}) " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (p.project_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.project_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY p.create_time DESC" +
            "</script>")
    IPage<Project> selectUserProjects(Page<Project> page, @Param("userId") Long userId, @Param("keyword") String keyword);
    
    /**
     * 检查用户是否参与指定项目
     */
    @Select("SELECT COUNT(*) FROM project_members WHERE user_id = #{userId} AND project_id = #{projectId}")
    int isUserInProject(@Param("userId") Long userId, @Param("projectId") Long projectId);
    
    /**
     * 获取项目经理ID
     */
    @Select("SELECT manager_id FROM projects WHERE id = #{projectId}")
    Long getProjectManagerId(@Param("projectId") Long projectId);
    
    /**
     * 获取所有项目ID列表
     */
    @Select("SELECT id FROM projects ORDER BY create_time DESC")
    List<Long> selectAllProjectIds();
    
    /**
     * 获取用户可访问的项目ID列表
     */
    @Select("SELECT DISTINCT pm.project_id FROM project_members pm " +
            "WHERE pm.user_id = #{userId} " +
            "UNION " +
            "SELECT DISTINCT prp.project_id FROM project_role_permissions prp " +
            "WHERE prp.user_id = #{userId} " +
            "AND (prp.effective_date IS NULL OR prp.effective_date <= CURDATE()) " +
            "AND (prp.expiry_date IS NULL OR prp.expiry_date >= CURDATE()) " +
            "UNION " +
            "SELECT DISTINCT id FROM projects p " +
            "WHERE p.manager_id = #{userId}")
    List<Long> selectUserAccessibleProjectIds(@Param("userId") Long userId);
}