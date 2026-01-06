package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timetracking.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
    @Select("SELECT t.*, p.project_name, " +
            "u1.real_name as assignee_name, " +
            "u2.real_name as reviewer_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN t.end_date < CURDATE() AND t.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), t.end_date) ELSE 0 END as delay_days " +
            "FROM tasks t " +
            "LEFT JOIN projects p ON t.project_id = p.id " +
            "LEFT JOIN users u1 ON t.assignee_id = u1.id " +
            "LEFT JOIN users u2 ON t.reviewer_id = u2.id " +
            "LEFT JOIN (" +
            "  SELECT te.task_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.task_id" +
            ") te_stats ON t.id = te_stats.task_id " +
            "WHERE t.id = #{id}")
    Task selectTaskWithDetails(@Param("id") Long id);
    
    @Select("SELECT t.*, p.project_name, " +
            "u1.real_name as assignee_name, " +
            "u2.real_name as reviewer_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN t.end_date < CURDATE() AND t.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), t.end_date) ELSE 0 END as delay_days " +
            "FROM tasks t " +
            "LEFT JOIN projects p ON t.project_id = p.id " +
            "LEFT JOIN users u1 ON t.assignee_id = u1.id " +
            "LEFT JOIN users u2 ON t.reviewer_id = u2.id " +
            "LEFT JOIN (" +
            "  SELECT te.task_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.task_id" +
            ") te_stats ON t.id = te_stats.task_id " +
            "ORDER BY t.create_time DESC")
    IPage<Task> selectTasksWithDetails(Page<Task> page);
    
    @Select("<script>" +
            "SELECT t.*, p.project_name, " +
            "u1.real_name as assignee_name, " +
            "u2.real_name as reviewer_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN t.end_date &lt; CURDATE() AND t.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), t.end_date) ELSE 0 END as delay_days " +
            "FROM tasks t " +
            "LEFT JOIN projects p ON t.project_id = p.id " +
            "LEFT JOIN users u1 ON t.assignee_id = u1.id " +
            "LEFT JOIN users u2 ON t.reviewer_id = u2.id " +
            "LEFT JOIN (" +
            "  SELECT te.task_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.task_id" +
            ") te_stats ON t.id = te_stats.task_id " +
            "WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (t.task_name LIKE CONCAT('%', #{keyword}, '%') OR t.description LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "ORDER BY t.create_time DESC" +
            "</script>")
    IPage<Task> selectTasksWithDetailsAndKeyword(Page<Task> page, @Param("keyword") String keyword);
    
    @Select("<script>" +
            "SELECT t.*, p.project_name, " +
            "u1.real_name as assignee_name, " +
            "u2.real_name as reviewer_name, " +
            "COALESCE(te_stats.actual_hours, 0) as actual_hours, " +
            "CASE WHEN t.end_date &lt; CURDATE() AND t.status != 'COMPLETED' " +
            "THEN DATEDIFF(CURDATE(), t.end_date) ELSE 0 END as delay_days " +
            "FROM tasks t " +
            "LEFT JOIN projects p ON t.project_id = p.id " +
            "LEFT JOIN users u1 ON t.assignee_id = u1.id " +
            "LEFT JOIN users u2 ON t.reviewer_id = u2.id " +
            "LEFT JOIN (" +
            "  SELECT te.task_id, SUM(te.duration) as actual_hours " +
            "  FROM time_entries te " +
            "  WHERE te.status = 'APPROVED' " +
            "  GROUP BY te.task_id" +
            ") te_stats ON t.id = te_stats.task_id " +
            "WHERE t.project_id = #{projectId} " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "AND (t.task_name LIKE CONCAT('%', #{keyword}, '%') OR t.description LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if> " +
            "ORDER BY t.create_time DESC" +
            "</script>")
    IPage<Task> selectTasksByProjectAndKeyword(Page<Task> page, @Param("projectId") Long projectId, @Param("keyword") String keyword);
    
    IPage<Task> selectTasksByProject(Page<Task> page, @Param("projectId") Long projectId);
    
    IPage<Task> selectUserRelatedTasks(Page<Task> page, @Param("userId") Long userId, @Param("keyword") String keyword);
    
    IPage<Task> selectUserRelatedTasksByProject(Page<Task> page, @Param("userId") Long userId, @Param("projectId") Long projectId, @Param("keyword") String keyword);
    
    /**
     * 获取项目经理管理的项目的任务统计
     */
    @Select("SELECT " +
            "COUNT(*) as totalTasks, " +
            "SUM(CASE WHEN t.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedTasks, " +
            "SUM(CASE WHEN t.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgressTasks " +
            "FROM tasks t " +
            "WHERE t.project_id IN (" +
            "  SELECT DISTINCT pm.project_id FROM project_members pm " +
            "  WHERE pm.user_id = #{managerId} AND pm.is_project_manager = 1" +
            ")")
    java.util.Map<String, Object> selectProjectManagerTaskStats(@Param("managerId") Long managerId);
}