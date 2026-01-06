package com.timetracking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.timetracking.entity.TimeEntry;
import com.timetracking.service.TimeEntryService;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/time-entries")
@CrossOrigin
public class TimeEntryController {
    
    @Autowired
    private TimeEntryService timeEntryService;
    
    @GetMapping
    public Result getTimeEntryList(@RequestParam(defaultValue = "1") int current,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(required = false) Long userId,
                                  @RequestParam(required = false) String status) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 根据用户权限获取工时记录列表
            IPage<TimeEntry> page = timeEntryService.getTimeEntryListByPermission(current, size, userId, status, currentUserId);
            return Result.success("获取工时记录成功", page);
        } catch (Exception e) {
            return Result.error("获取工时记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    public Result getUserTimeEntries(@PathVariable Long userId,
                                    @RequestParam(defaultValue = "1") int current,
                                    @RequestParam(defaultValue = "10") int size,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) Long projectId,
                                    @RequestParam(required = false) Long taskId) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 只能查看自己的工时记录，除非是管理员
            if (!currentUserId.equals(userId) && !PermissionUtil.isAdmin()) {
                return Result.error("无权限查看其他用户的工时记录");
            }
            
            // 获取用户工时记录列表
            IPage<TimeEntry> page = timeEntryService.getTimeEntryListByPermission(current, size, userId, status, userId);
            return Result.success("获取用户工时记录成功", page);
        } catch (Exception e) {
            return Result.error("获取用户工时记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result getTimeEntryById(@PathVariable Long id) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            TimeEntry timeEntry = timeEntryService.getTimeEntryWithDetails(id);
            
            if (timeEntry == null) {
                return Result.error("工时记录不存在");
            }
            
            // 检查工时记录访问权限
            if (!canAccessTimeEntry(currentUserId, timeEntry)) {
                return Result.error("无权限访问该工时记录");
            }
            
            return Result.success("获取工时记录成功", timeEntry);
        } catch (Exception e) {
            return Result.error("获取工时记录失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/pending")
    public Result getPendingApprovals(@RequestParam(defaultValue = "1") int current,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) String startDate,
                                     @RequestParam(required = false) String endDate,
                                     @RequestParam(required = false) String keyword) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 获取当前用户可以审批的待审核工时
            IPage<TimeEntry> page;
            
            if (PermissionUtil.isAdmin()) {
                // 管理员可以看到所有待审核工时
                page = timeEntryService.getPendingApprovals(current, size);
            } else {
                // 其他用户只能看到自己作为项目经理的项目的待审核工时
                page = timeEntryService.getPendingApprovalsByManager(current, size, currentUserId, keyword);
            }
            
            return Result.success("获取待审核工时成功", page);
        } catch (Exception e) {
            return Result.error("获取待审核工时失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/pending/manager/{managerId}")
    public Result getPendingApprovalsByManager(@PathVariable Long managerId,
                                              @RequestParam(defaultValue = "1") int current,
                                              @RequestParam(defaultValue = "10") int size) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 只能查看自己作为项目经理的待审核工时，除非是管理员
            if (!currentUserId.equals(managerId) && !PermissionUtil.isAdmin()) {
                return Result.error("无权限查看其他用户的待审核工时");
            }
            
            // 获取指定项目经理可以审批的待审核工时
            IPage<TimeEntry> page = timeEntryService.getPendingApprovalsByPermission(current, size, managerId);
            return Result.success("获取项目经理待审核工时成功", page);
        } catch (Exception e) {
            return Result.error("获取项目经理待审核工时失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    public Result createTimeEntry(@RequestBody TimeEntry timeEntry) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 只能为自己创建工时记录，除非是管理员
            if (!timeEntry.getUserId().equals(currentUserId) && !PermissionUtil.isAdmin()) {
                return Result.error("只能为自己创建工时记录");
            }
            
            TimeEntry savedTimeEntry = timeEntryService.createTimeEntry(timeEntry);
            return Result.success("创建工时记录成功", savedTimeEntry);
        } catch (Exception e) {
            return Result.error("创建工时记录失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result updateTimeEntry(@PathVariable Long id, @RequestBody TimeEntry timeEntry) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            TimeEntry existingEntry = timeEntryService.getById(id);
            
            if (existingEntry == null) {
                return Result.error("工时记录不存在");
            }
            
            // 检查修改权限
            if (!canModifyTimeEntry(currentUserId, existingEntry)) {
                return Result.error("无权限修改该工时记录");
            }
            
            timeEntry.setId(id);
            TimeEntry updatedTimeEntry = timeEntryService.updateTimeEntry(timeEntry);
            return Result.success("更新工时记录成功", updatedTimeEntry);
        } catch (Exception e) {
            return Result.error("更新工时记录失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result deleteTimeEntry(@PathVariable Long id) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            TimeEntry timeEntry = timeEntryService.getById(id);
            
            if (timeEntry == null) {
                return Result.error("工时记录不存在");
            }
            
            // 检查删除权限
            if (!canModifyTimeEntry(currentUserId, timeEntry)) {
                return Result.error("无权限删除该工时记录");
            }
            
            timeEntryService.deleteTimeEntry(id);
            return Result.success("删除工时记录成功");
        } catch (Exception e) {
            return Result.error("删除工时记录失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/approve")
    public Result approveTimeEntry(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            TimeEntry timeEntry = timeEntryService.getById(id);
            
            if (timeEntry == null) {
                return Result.error("工时记录不存在");
            }
            
            // 检查审批权限
            if (!canApproveTimeEntry(currentUserId, timeEntry)) {
                return Result.error("无权限审批该工时记录");
            }
            
            Boolean approved = (Boolean) request.get("approved");
            String comment = (String) request.get("comment");
            
            boolean success = timeEntryService.approveTimeEntry(id, currentUserId, approved != null ? approved : true, comment);
            if (success) {
                return Result.success(approved != null && approved ? "审批通过" : "审批拒绝");
            } else {
                return Result.error("审批失败");
            }
        } catch (Exception e) {
            return Result.error("审批工时记录失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/batch-approve")
    public Result batchApproveTimeEntries(@RequestBody Map<String, Object> request) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            String comment = (String) request.get("comment");
            
            // 检查每个工时记录的审批权限
            for (Long id : ids) {
                TimeEntry timeEntry = timeEntryService.getById(id);
                if (timeEntry == null || !canApproveTimeEntry(currentUserId, timeEntry)) {
                    return Result.error("部分工时记录无权限审批");
                }
            }
            
            timeEntryService.batchApproveTimeEntries(ids, currentUserId, comment);
            return Result.success("批量审批工时记录成功");
        } catch (Exception e) {
            return Result.error("批量审批工时记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否可以访问工时记录
     */
    private boolean canAccessTimeEntry(Long userId, TimeEntry timeEntry) {
        if (userId == null || timeEntry == null) {
            return false;
        }
        
        // 管理员可以访问所有工时记录
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 工时记录所有者可以访问
        if (timeEntry.getUserId().equals(userId)) {
            return true;
        }
        
        // 项目经理可以访问项目内的工时记录
        if (timeEntry.getProjectId() != null && 
            EnhancedPermissionUtil.isProjectManager(userId, timeEntry.getProjectId())) {
            return true;
        }
        
        // 有审批权限的用户可以访问
        if (timeEntry.getProjectId() != null && 
            EnhancedPermissionUtil.canApproveTimesheet(userId, timeEntry.getProjectId())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查用户是否可以修改工时记录
     */
    private boolean canModifyTimeEntry(Long userId, TimeEntry timeEntry) {
        if (userId == null || timeEntry == null) {
            return false;
        }
        
        // 管理员可以修改所有工时记录
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 工时记录所有者可以修改（如果状态允许）
        if (timeEntry.getUserId().equals(userId)) {
            // 已审批的工时记录不能修改
            return !"APPROVED".equals(timeEntry.getStatus());
        }
        
        return false;
    }
    
    /**
     * 检查用户是否可以审批工时记录
     */
    private boolean canApproveTimeEntry(Long userId, TimeEntry timeEntry) {
        if (userId == null || timeEntry == null) {
            return false;
        }
        
        // 不能审批自己的工时记录
        if (timeEntry.getUserId().equals(userId)) {
            return false;
        }
        
        // 管理员可以审批所有工时记录
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 项目经理可以审批项目内的工时记录
        if (timeEntry.getProjectId() != null && 
            EnhancedPermissionUtil.canApproveTimesheet(userId, timeEntry.getProjectId())) {
            return true;
        }
        
        return false;
    }
}