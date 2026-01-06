package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timetracking.entity.TimeEntry;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface TimeEntryMapper extends BaseMapper<TimeEntry> {
    
    TimeEntry selectTimeEntryWithDetails(@Param("id") Long id);
    
    IPage<TimeEntry> selectTimeEntriesByUser(Page<TimeEntry> page, @Param("userId") Long userId);
    
    @Select("SELECT DATE_FORMAT(work_date, '%Y-%m') as month, " +
            "SUM(duration) as total_hours, " +
            "COUNT(*) as entry_count " +
            "FROM time_entries " +
            "WHERE user_id = #{userId} AND status = 'APPROVED' " +
            "AND work_date >= #{startDate} AND work_date <= #{endDate} " +
            "GROUP BY DATE_FORMAT(work_date, '%Y-%m') " +
            "ORDER BY month")
    List<Map<String, Object>> selectMonthlyStats(@Param("userId") Long userId, 
                                                 @Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    IPage<TimeEntry> selectUserTimeEntries(Page<TimeEntry> page, 
                                          @Param("userId") Long userId,
                                          @Param("status") String status,
                                          @Param("startDate") String startDate,
                                          @Param("endDate") String endDate);
    
    IPage<TimeEntry> selectTimeEntriesWithDetails(Page<TimeEntry> page);
    
    IPage<TimeEntry> selectPendingApprovals(Page<TimeEntry> page);
    
    /**
     * 查询项目经理管理的项目的待审批工时记录
     */
    IPage<TimeEntry> selectPendingApprovalsByManager(Page<TimeEntry> page, 
                                                    @Param("managerId") Long managerId,
                                                    @Param("keyword") String keyword);
    
    /**
     * 查询指定项目的待审批工时记录
     */
    @Select("SELECT te.*, u.real_name as userName, " +
            "p.project_name as projectName, t.task_name as taskName, " +
            "u2.real_name as approverName " +
            "FROM time_entries te " +
            "LEFT JOIN users u ON te.user_id = u.id " +
            "LEFT JOIN projects p ON te.project_id = p.id " +
            "LEFT JOIN tasks t ON te.task_id = t.id " +
            "LEFT JOIN users u2 ON te.approver_id = u2.id " +
            "WHERE te.status = 'PENDING' AND te.project_id = #{projectId} " +
            "ORDER BY te.work_date ASC, te.create_time ASC")
    IPage<TimeEntry> selectProjectPendingApprovals(Page<TimeEntry> page, @Param("projectId") Long projectId);
}