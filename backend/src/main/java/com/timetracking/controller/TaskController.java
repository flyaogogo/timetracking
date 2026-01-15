package com.timetracking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.timetracking.entity.Task;
import com.timetracking.entity.Project;
import com.timetracking.service.TaskService;
import com.timetracking.service.ProjectService;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@CrossOrigin
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private ProjectService projectService;
    
    @GetMapping
    public Result getTaskList(@RequestParam(defaultValue = "1") int current,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) Long projectId,
                             @RequestParam(required = false) String keyword) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 根据用户权限获取任务列表
            IPage<Task> page = taskService.getTaskListByPermission(current, size, projectId, keyword, currentUserId);
            return Result.success("获取任务列表成功", page);
        } catch (Exception e) {
            return Result.error("获取任务列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result getTaskById(@PathVariable Long id) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            Task task = taskService.getTaskWithDetails(id);
            
            if (task == null) {
                return Result.error("任务不存在");
            }
            
            // 检查任务访问权限
            if (!canAccessTask(currentUserId, task)) {
                return Result.error("无权限访问该任务");
            }
            
            return Result.success("获取任务信息成功", task);
        } catch (Exception e) {
            return Result.error("获取任务信息失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/my")
    public Result getMyTasks(@RequestParam(defaultValue = "1") int current,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) Long userId) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 如果没有提供userId，使用当前登录用户的ID
            Long targetUserId = userId != null ? userId : currentUserId;
            
            // 只能查看自己的任务，除非是管理员
            if (!currentUserId.equals(targetUserId) && !PermissionUtil.isAdmin()) {
                return Result.error("无权限查看其他用户的任务");
            }
            
            IPage<Task> page = taskService.getMyTasks(current, size, targetUserId);
            return Result.success("获取我的任务成功", page);
        } catch (Exception e) {
            return Result.error("获取我的任务失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    public Result getUserTasks(@PathVariable Long userId,
                              @RequestParam(defaultValue = "1") int current,
                              @RequestParam(defaultValue = "10") int size,
                              @RequestParam(required = false) Long projectId,
                              @RequestParam(required = false) String keyword) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 只能查看自己的任务，除非是管理员
            if (!currentUserId.equals(userId) && !PermissionUtil.isAdmin()) {
                return Result.error("无权限查看其他用户的任务");
            }
            
            // 获取用户任务列表
            IPage<Task> page = taskService.getTaskListByPermission(current, size, projectId, keyword, userId);
            return Result.success("获取用户任务列表成功", page);
        } catch (Exception e) {
            return Result.error("获取用户任务列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping
    public Result createTask(@RequestBody Task task) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            
            // 检查任务创建权限
            if (!canManageTasks(currentUserId, task.getProjectId())) {
                return Result.error("无权限在该项目中创建任务");
            }
            
            Task savedTask = taskService.createTask(task);
            return Result.success("创建任务成功", savedTask);
        } catch (Exception e) {
            return Result.error("创建任务失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public Result updateTask(@PathVariable Long id, @RequestBody Task task) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            Task existingTask = taskService.getById(id);
            
            if (existingTask == null) {
                return Result.error("任务不存在");
            }
            
            // 检查任务修改权限
            if (!canManageTasks(currentUserId, existingTask.getProjectId())) {
                return Result.error("无权限修改该任务");
            }
            
            task.setId(id);
            Task updatedTask = taskService.updateTask(task);
            return Result.success("更新任务成功", updatedTask);
        } catch (Exception e) {
            return Result.error("更新任务失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result deleteTask(@PathVariable Long id) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            Task task = taskService.getById(id);
            
            if (task == null) {
                return Result.error("任务不存在");
            }
            
            // 检查任务删除权限
            if (!canManageTasks(currentUserId, task.getProjectId())) {
                return Result.error("无权限删除该任务");
            }
            
            taskService.deleteTask(id);
            return Result.success("删除任务成功");
        } catch (Exception e) {
            return Result.error("删除任务失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}/status")
    public Result updateTaskStatus(@PathVariable Long id, @RequestBody String status) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            Task task = taskService.getById(id);
            
            if (task == null) {
                return Result.error("任务不存在");
            }
            
            // 任务负责人可以更新状态，项目经理也可以
            if (!task.getAssigneeId().equals(currentUserId) && 
                !canManageTasks(currentUserId, task.getProjectId())) {
                return Result.error("无权限修改任务状态");
            }
            
            taskService.updateTaskStatus(id, status);
            return Result.success("更新任务状态成功");
        } catch (Exception e) {
            return Result.error("更新任务状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户可访问的项目列表（用于下拉选择）
     */
    @GetMapping("/accessible-projects")
    public Result getAccessibleProjects() {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            // 获取用户可访问的项目列表
            List<Long> projectIds = projectService.getUserAccessibleProjectIds(currentUserId);
            List<Project> projects = projectService.getProjectsByIds(projectIds);
            
            return Result.success("获取可访问项目成功", projects);
        } catch (Exception e) {
            return Result.error("获取可访问项目失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查用户是否可以访问任务
     */
    private boolean canAccessTask(Long userId, Task task) {
        if (userId == null || task == null) {
            return false;
        }
        
        // 管理员可以访问所有任务
        if (PermissionUtil.isAdmin()) {
            return true;
        }
        
        // 任务负责人可以访问
        if (task.getAssigneeId() != null && task.getAssigneeId().equals(userId)) {
            return true;
        }
        
        // 项目经理可以访问项目内的任务
        if (EnhancedPermissionUtil.isProjectManager(userId, task.getProjectId())) {
            return true;
        }
        
        // 技术负责人可以访问项目内的任务
        if (EnhancedPermissionUtil.isTechLeader(userId, task.getProjectId())) {
            return true;
        }
        
        // 项目成员可以查看项目内的任务
        return taskService.isProjectMember(task.getProjectId(), userId);
    }
    
    /**
     * 检查用户是否可以管理任务
     */
    private boolean canManageTasks(Long userId, Long projectId) {
        if (userId == null || projectId == null) {
            return false;
        }
        
        return EnhancedPermissionUtil.canManageTasks(userId, projectId);
    }
    
    /**
     * Excel导入任务
     */
    @PostMapping("/import")
    public Result importTasks(@RequestParam("file") MultipartFile file) {
        try {
            Long currentUserId = PermissionUtil.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("用户未登录");
            }
            
            // 调用服务层进行导入
            return taskService.importTasks(file, currentUserId);
        } catch (Exception e) {
            return Result.error("导入任务失败: " + e.getMessage());
        }
    }
    
    /**
     * 下载导入模板
     */
    @GetMapping("/import/template")
    public void downloadImportTemplate(HttpServletResponse response) {
        try {
            taskService.downloadImportTemplate(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}