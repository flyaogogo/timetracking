package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timetracking.entity.TimeEntry;
import com.timetracking.mapper.TimeEntryMapper;
import com.timetracking.mapper.ProjectMapper;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class TimeEntryService extends ServiceImpl<TimeEntryMapper, TimeEntry> {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private TimeTrackingStatisticsService statisticsService;
    
    public IPage<TimeEntry> getTimeEntryList(int current, int size, Long userId, String status) {
        Page<TimeEntry> page = new Page<>(current, size);
        
        // 获取当前用户信息
        Long currentUserId = PermissionUtil.getCurrentUserId();
        boolean canViewAll = PermissionUtil.canViewAllProjects();
        
        if (canViewAll) {
            // 管理员可以查看所有工时记录
            return baseMapper.selectTimeEntriesWithDetails(page);
        } else {
            // 其他角色只能查看参与项目中的工时记录
            Long targetUserId = userId != null ? userId : currentUserId;
            return baseMapper.selectUserTimeEntries(page, targetUserId, status, null, null);
        }
    }
    
    /**
     * 根据用户权限获取工时记录列表
     */
    public IPage<TimeEntry> getTimeEntryListByPermission(int current, int size, Long userId, String status, Long currentUserId) {
        Page<TimeEntry> page = new Page<>(current, size);
        
        if (currentUserId == null) {
            return page; // 返回空页面
        }
        
        // 管理员可以查看所有工时记录
        if (PermissionUtil.isAdmin()) {
            if (userId != null) {
                // 查看指定用户的工时记录
                return baseMapper.selectUserTimeEntries(page, userId, status, null, null);
            } else {
                // 查看所有工时记录
                return baseMapper.selectTimeEntriesWithDetails(page);
            }
        }
        
        // 其他用户只能查看自己的工时记录
        Long targetUserId = userId != null ? userId : currentUserId;
        // 确保非管理员用户只能查看自己的工时记录
        if (!targetUserId.equals(currentUserId)) {
            return page; // 返回空页面
        }
        
        return baseMapper.selectUserTimeEntries(page, targetUserId, status, null, null);
    }
    
    /**
     * 获取待审核工时记录（根据权限）
     */
    public IPage<TimeEntry> getPendingApprovalsByPermission(int current, int size, Long currentUserId) {
        Page<TimeEntry> page = new Page<>(current, size);
        
        if (currentUserId == null) {
            return page;
        }
        
        // 管理员可以审批所有工时记录
        if (PermissionUtil.isAdmin()) {
            return baseMapper.selectPendingApprovals(page);
        }
        
        // 其他用户需要基于项目权限审批工时记录
        return baseMapper.selectPendingApprovalsByManager(page, currentUserId, null);
    }
    
    public IPage<TimeEntry> getPendingApprovals(int current, int size) {
        Page<TimeEntry> page = new Page<>(current, size);
        return baseMapper.selectPendingApprovals(page);
    }
    
    public TimeEntry getTimeEntryWithDetails(Long id) {
        return baseMapper.selectTimeEntryWithDetails(id);
    }
    
    /**
     * 创建工时记录
     */
    public TimeEntry createTimeEntry(TimeEntry timeEntry) {
        timeEntry.setStatus(TimeEntry.ApprovalStatus.PENDING);
        timeEntry.setCreateTime(LocalDateTime.now());
        save(timeEntry);
        
        // 创建工时记录后，更新相关任务的实际工时（虽然还未审核，但可以更新统计）
        if (timeEntry.getTaskId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(timeEntry.getTaskId());
        }
        
        return timeEntry;
    }
    
    /**
     * 更新工时记录
     */
    public TimeEntry updateTimeEntry(TimeEntry timeEntry) {
        TimeEntry oldEntry = getById(timeEntry.getId());
        timeEntry.setUpdateTime(LocalDateTime.now());
        updateById(timeEntry);
        
        // 更新工时记录后，更新相关任务的实际工时
        if (timeEntry.getTaskId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(timeEntry.getTaskId());
        }
        
        // 如果任务ID发生了变化，也要更新原来的任务
        if (oldEntry != null && oldEntry.getTaskId() != null && 
            !oldEntry.getTaskId().equals(timeEntry.getTaskId())) {
            statisticsService.updateTaskActualHoursAndProgress(oldEntry.getTaskId());
        }
        
        return getById(timeEntry.getId());
    }
    
    /**
     * 删除工时记录
     */
    public void deleteTimeEntry(Long id) {
        TimeEntry timeEntry = getById(id);
        removeById(id);
        
        // 删除工时记录后，更新相关任务的实际工时
        if (timeEntry != null && timeEntry.getTaskId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(timeEntry.getTaskId());
        }
    }
    
    /**
     * 审批工时记录
     */
    public void approveTimeEntry(Long id, Long approverId, String comment) {
        TimeEntry timeEntry = getById(id);
        if (timeEntry == null) {
            return;
        }
        
        TimeEntry updateEntry = new TimeEntry();
        updateEntry.setId(id);
        updateEntry.setStatus(TimeEntry.ApprovalStatus.APPROVED);
        updateEntry.setApproverId(approverId);
        updateEntry.setApproveTime(LocalDateTime.now());
        updateEntry.setApproveComment(comment);
        updateById(updateEntry);
        
        // 审批通过后，更新相关任务的实际工时和进度
        if (timeEntry.getTaskId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(timeEntry.getTaskId());
        }
    }
    
    /**
     * 拒绝工时记录
     */
    public void rejectTimeEntry(Long id, Long approverId, String comment) {
        TimeEntry timeEntry = getById(id);
        if (timeEntry == null) {
            return;
        }
        
        TimeEntry updateEntry = new TimeEntry();
        updateEntry.setId(id);
        updateEntry.setStatus(TimeEntry.ApprovalStatus.REJECTED);
        updateEntry.setApproverId(approverId);
        updateEntry.setApproveTime(LocalDateTime.now());
        updateEntry.setApproveComment(comment);
        updateById(updateEntry);
        
        // 拒绝后，也要更新相关任务的实际工时（因为审核状态变化了）
        if (timeEntry.getTaskId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(timeEntry.getTaskId());
        }
    }
    
    /**
     * 批量审批工时记录
     */
    public void batchApproveTimeEntries(List<Long> ids, Long approverId, String comment) {
        for (Long id : ids) {
            approveTimeEntry(id, approverId, comment);
        }
    }
    
    /**
     * 获取我的待审批工时记录 - 只返回用户担任项目经理的项目的待审批工时
     */
    public IPage<TimeEntry> getMyPendingApprovals(int current, int size, Long managerId) {
        Page<TimeEntry> page = new Page<>(current, size);
        
        if (managerId == null) {
            return page;
        }
        
        // 查询用户担任项目经理的项目的待审批工时记录
        return baseMapper.selectPendingApprovalsByManager(page, managerId, null);
    }
    
    /**
     * 获取项目待审批工时记录
     */
    public IPage<TimeEntry> getProjectPendingApprovals(int current, int size, Long projectId) {
        Page<TimeEntry> page = new Page<>(current, size);
        
        if (projectId == null) {
            return page;
        }
        
        // 查询指定项目的待审批工时记录
        return baseMapper.selectProjectPendingApprovals(page, projectId);
    }
    
    /**
     * 获取团队绩效统计
     */
    public Map<String, Object> getTeamPerformance(Long projectId, String period) {
        Map<String, Object> performance = new HashMap<>();
        
        try {
            QueryWrapper<TimeEntry> wrapper = new QueryWrapper<>();
            wrapper.eq("project_id", projectId);
            
            // 根据时间段过滤
            if (period != null) {
                LocalDateTime startDate = getStartDateByPeriod(period);
                if (startDate != null) {
                    wrapper.ge("work_date", startDate.toLocalDate());
                }
            }
            
            // 获取项目总工时
            List<TimeEntry> entries = list(wrapper);
            double totalHours = entries.stream()
                .mapToDouble(entry -> entry.getDuration() != null ? entry.getDuration().doubleValue() : 0.0)
                .sum();
            performance.put("totalHours", Math.round(totalHours * 100.0) / 100.0);
            
            // 获取团队成员数量
            long memberCount = entries.stream()
                .map(TimeEntry::getUserId)
                .distinct()
                .count();
            performance.put("teamSize", (int) memberCount);
            
            // 计算平均效率（简单的工时/天数比率）
            double efficiency = memberCount > 0 ? totalHours / memberCount : 0.0;
            performance.put("efficiency", Math.round(efficiency * 100.0) / 100.0);
            
            // 获取已审批工时
            long approvedCount = entries.stream()
                .filter(entry -> TimeEntry.ApprovalStatus.APPROVED.equals(entry.getStatus()))
                .count();
            performance.put("approvedEntries", (int) approvedCount);
            
            // 获取待审批工时
            long pendingCount = entries.stream()
                .filter(entry -> TimeEntry.ApprovalStatus.PENDING.equals(entry.getStatus()))
                .count();
            performance.put("pendingEntries", (int) pendingCount);
            
            // 计算审批率
            double approvalRate = entries.size() > 0 ? (double) approvedCount / entries.size() * 100 : 0;
            performance.put("approvalRate", Math.round(approvalRate * 100.0) / 100.0);
            
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            performance.put("totalHours", 0.0);
            performance.put("teamSize", 0);
            performance.put("efficiency", 0.0);
            performance.put("approvedEntries", 0);
            performance.put("pendingEntries", 0);
            performance.put("approvalRate", 0.0);
        }
        
        return performance;
    }
    
    /**
     * 获取项目工时统计
     */
    public Map<String, Object> getProjectTimeStats(Long projectId, String startDate, String endDate) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            QueryWrapper<TimeEntry> wrapper = new QueryWrapper<>();
            wrapper.eq("project_id", projectId);
            
            // 添加日期范围过滤
            if (startDate != null && !startDate.isEmpty()) {
                wrapper.ge("work_date", startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                wrapper.le("work_date", endDate);
            }
            
            List<TimeEntry> entries = list(wrapper);
            
            // 总工时
            double totalHours = entries.stream()
                .mapToDouble(entry -> entry.getDuration() != null ? entry.getDuration().doubleValue() : 0.0)
                .sum();
            stats.put("totalHours", Math.round(totalHours * 100.0) / 100.0);
            
            // 工时记录数
            stats.put("totalEntries", entries.size());
            
            // 平均每日工时
            long dayCount = entries.stream()
                .map(TimeEntry::getWorkDate)
                .distinct()
                .count();
            double avgDailyHours = dayCount > 0 ? totalHours / dayCount : 0.0;
            stats.put("avgDailyHours", Math.round(avgDailyHours * 100.0) / 100.0);
            
            // 按状态统计
            Map<String, Long> statusStats = entries.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    entry -> entry.getStatus() != null ? entry.getStatus().toString() : "UNKNOWN",
                    java.util.stream.Collectors.counting()
                ));
            stats.put("statusStats", statusStats);
            
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            stats.put("totalHours", 0.0);
            stats.put("totalEntries", 0);
            stats.put("avgDailyHours", 0.0);
            stats.put("statusStats", new HashMap<>());
        }
        
        return stats;
    }
    
    /**
     * 根据时间段获取开始日期
     */
    private LocalDateTime getStartDateByPeriod(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period) {
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            case "quarter":
                return now.minusMonths(3);
            case "year":
                return now.minusYears(1);
            default:
                return null;
        }
    }
    
    public boolean approveTimeEntry(Long id, Long approverId, boolean approved, String comment) {
        TimeEntry timeEntry = getById(id);
        if (timeEntry == null) {
            return false;
        }
        
        timeEntry.setStatus(approved ? TimeEntry.ApprovalStatus.APPROVED : TimeEntry.ApprovalStatus.REJECTED);
        timeEntry.setApproverId(approverId);
        timeEntry.setApproveTime(LocalDateTime.now());
        timeEntry.setApproveComment(comment);
        
        boolean result = updateById(timeEntry);
        
        // 审批后，更新相关任务的实际工时和进度
        if (result && timeEntry.getTaskId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(timeEntry.getTaskId());
        }
        
        return result;
    }
    
    public List<Map<String, Object>> getMonthlyStats(Long userId, LocalDate startDate, LocalDate endDate) {
        return baseMapper.selectMonthlyStats(userId, startDate, endDate);
    }
    
    public IPage<TimeEntry> getUserTimeEntries(int current, int size, Long userId, String status, String startDate, String endDate) {
        Page<TimeEntry> page = new Page<>(current, size);
        return baseMapper.selectUserTimeEntries(page, userId, status, startDate, endDate);
    }
    
    public IPage<TimeEntry> getPendingApprovalsByManager(int current, int size, Long managerId, String keyword) {
        Page<TimeEntry> page = new Page<>(current, size);
        return baseMapper.selectPendingApprovalsByManager(page, managerId, keyword);
    }
}