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
            "SELECT p.*, u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date &lt; CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days, " +
            "pm.is_project_manager, pm.is_tech_leader, pm.role " +
            "FROM projects p " +
            "INNER JOIN project_members pm ON p.id = pm.project_id " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "WHERE pm.user_id = #{userId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (p.project_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.project_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY pm.join_date DESC" +
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
            "SELECT p.*, u.real_name as manager_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN p.planned_end_date &lt; CURDATE() AND p.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), p.planned_end_date) ELSE 0 END as delay_days " +
            "FROM projects p " +
            "INNER JOIN project_members pm ON p.id = pm.project_id " +
            "LEFT JOIN users u ON p.manager_id = u.id " +
            "LEFT JOIN (" +
            "  SELECT te.project_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.project_id" +
            ") te_stats ON p.id = te_stats.project_id " +
            "WHERE pm.user_id = #{userId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (p.project_name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.project_code LIKE CONCAT('%', #{keyword}, '%') " +
            "OR p.description LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY pm.join_date DESC" +
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
            "AND (prp.expiry_date IS NULL OR prp.expiry_date >= CURDATE())")
    List<Long> selectUserAccessibleProjectIds(@Param("userId") Long userId);
}