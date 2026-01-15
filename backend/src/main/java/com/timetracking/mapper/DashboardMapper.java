package com.timetracking.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 工作台数据Mapper
 */
@Mapper
public interface DashboardMapper {
    
    /**
     * 获取用户参与的项目数量
     */
    @Select("SELECT COUNT(DISTINCT p.id) FROM projects p LEFT JOIN project_members pm ON p.id = pm.project_id WHERE pm.user_id = #{userId} OR p.manager_id = #{userId}")
    Integer getUserProjectCount(@Param("userId") Long userId);
    
    /**
     * 获取用户参与项目中的任务数量（分配给用户的任务）
     */
    @Select("SELECT COUNT(*) FROM tasks t INNER JOIN projects p ON t.project_id = p.id LEFT JOIN project_members pm ON t.project_id = pm.project_id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND t.assignee_id = #{userId}")
    Integer getUserTaskCount(@Param("userId") Long userId);
    
    /**
     * 获取用户在参与项目上花费的总工时
     */
    @Select("SELECT COALESCE(SUM(te.duration), 0) FROM time_entries te INNER JOIN projects p ON te.project_id = p.id LEFT JOIN project_members pm ON te.project_id = pm.project_id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND te.user_id = #{userId}")
    Double getUserTotalHours(@Param("userId") Long userId);
    
    /**
     * 获取用户作为项目经理需要审核的工时数量
     */
    @Select("SELECT COUNT(*) FROM time_entries te " +
            "WHERE te.status = 'PENDING' " +
            "AND te.project_id IN (" +
            "    SELECT DISTINCT pm.project_id FROM project_members pm " +
            "    WHERE pm.user_id = #{userId} AND pm.is_project_manager = 1" +
            ")")
    Integer getUserPendingTimeEntries(@Param("userId") Long userId);
    
    /**
     * 获取用户最近的任务（Top10）
     */
    @Select("SELECT t.id, t.task_name as taskName, t.status, p.project_name as projectName, t.priority, t.end_date as endDate, t.update_time as updateTime FROM tasks t INNER JOIN projects p ON t.project_id = p.id LEFT JOIN project_members pm ON t.project_id = pm.project_id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND t.assignee_id = #{userId} ORDER BY t.update_time DESC LIMIT #{limit}")
    List<Map<String, Object>> getUserRecentTasks(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 获取用户最近的工时记录（Top10）
     */
    @Select("SELECT te.id, te.work_date as workDate, te.duration, p.project_name as projectName, t.task_name as taskName, te.create_time as createTime FROM time_entries te INNER JOIN projects p ON te.project_id = p.id LEFT JOIN project_members pm ON te.project_id = pm.project_id LEFT JOIN tasks t ON te.task_id = t.id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND te.user_id = #{userId} ORDER BY te.work_date DESC, te.create_time DESC LIMIT #{limit}")
    List<Map<String, Object>> getUserRecentTimeEntries(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 获取用户参与的项目列表（Top10最新）- 包含正确的角色显示
     */
    @Select("SELECT p.id, p.project_name as projectName, p.status, CASE WHEN p.manager_id = #{userId} THEN 'MANAGER' WHEN pm.is_project_manager = 1 THEN 'MANAGER' WHEN pm.is_tech_leader = 1 THEN 'TECH_LEADER' ELSE COALESCE(pm.role, 'MEMBER') END as role, COALESCE(pm.join_date, p.create_time) as joinDate FROM projects p LEFT JOIN project_members pm ON p.id = pm.project_id AND pm.user_id = #{userId} WHERE pm.user_id = #{userId} OR p.manager_id = #{userId} ORDER BY COALESCE(pm.join_date, p.create_time) DESC LIMIT #{limit}")
    List<Map<String, Object>> getUserRecentProjects(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 获取用户参与的项目列表（全部）- 包含正确的角色显示
     */
    @Select("SELECT p.id, p.project_name as projectName, p.status, CASE WHEN p.manager_id = #{userId} THEN 'MANAGER' WHEN pm.is_project_manager = 1 THEN 'MANAGER' WHEN pm.is_tech_leader = 1 THEN 'TECH_LEADER' ELSE COALESCE(pm.role, 'MEMBER') END as role, COALESCE(pm.join_date, p.create_time) as joinDate FROM projects p LEFT JOIN project_members pm ON p.id = pm.project_id AND pm.user_id = #{userId} WHERE pm.user_id = #{userId} OR p.manager_id = #{userId} ORDER BY COALESCE(pm.join_date, p.create_time) DESC")
    List<Map<String, Object>> getUserProjects(@Param("userId") Long userId);
    
    /**
     * 获取用户作为项目经理需要审核的工时记录（Top10最新）
     */
    @Select("SELECT te.id, te.work_date as workDate, te.duration, p.project_name as projectName, t.task_name as taskName, u.real_name as userName, te.create_time as createTime FROM time_entries te INNER JOIN projects p ON te.project_id = p.id INNER JOIN users u ON te.user_id = u.id LEFT JOIN tasks t ON te.task_id = t.id WHERE te.status = 'PENDING' AND te.project_id IN (SELECT DISTINCT pm.project_id FROM project_members pm WHERE pm.user_id = #{userId} AND pm.is_project_manager = 1) ORDER BY te.work_date ASC, te.create_time ASC LIMIT #{limit}")
    List<Map<String, Object>> getUserPendingApprovals(@Param("userId") Long userId, @Param("limit") Integer limit);
    
    /**
     * 按状态获取用户任务数量
     */
    @Select("SELECT COUNT(*) FROM tasks t INNER JOIN projects p ON t.project_id = p.id LEFT JOIN project_members pm ON t.project_id = pm.project_id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND t.assignee_id = #{userId} AND t.status = #{status}")
    Integer getUserTaskCountByStatus(@Param("userId") Long userId, @Param("status") String status);
    
    /**
     * 获取用户本周工时
     */
    @Select("SELECT COALESCE(SUM(te.duration), 0) FROM time_entries te INNER JOIN projects p ON te.project_id = p.id LEFT JOIN project_members pm ON te.project_id = pm.project_id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND te.user_id = #{userId} AND YEARWEEK(te.work_date, 1) = YEARWEEK(CURDATE(), 1)")
    Double getUserThisWeekHours(@Param("userId") Long userId);
    
    /**
     * 获取用户本月工时
     */
    @Select("SELECT COALESCE(SUM(te.duration), 0) FROM time_entries te INNER JOIN projects p ON te.project_id = p.id LEFT JOIN project_members pm ON te.project_id = pm.project_id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND te.user_id = #{userId} AND YEAR(te.work_date) = YEAR(CURDATE()) AND MONTH(te.work_date) = MONTH(CURDATE())")
    Double getUserThisMonthHours(@Param("userId") Long userId);
    
    /**
     * 获取用户平均日工时
     */
    @Select("SELECT COALESCE(AVG(daily_hours), 0) FROM (SELECT SUM(te.duration) as daily_hours FROM time_entries te INNER JOIN projects p ON te.project_id = p.id LEFT JOIN project_members pm ON te.project_id = pm.project_id WHERE (pm.user_id = #{userId} OR p.manager_id = #{userId}) AND te.user_id = #{userId} AND te.work_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY te.work_date) as daily_stats")
    Double getUserAvgDailyHours(@Param("userId") Long userId);
}