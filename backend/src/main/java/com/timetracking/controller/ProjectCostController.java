package com.timetracking.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timetracking.entity.ProjectCost;
import com.timetracking.service.ProjectCostService;
import com.timetracking.service.ProjectService;
import com.timetracking.util.PermissionUtil;
import com.timetracking.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 项目成本管理控制器
 */
@RestController
@RequestMapping("/project-costs")
@CrossOrigin(origins = "*")
public class ProjectCostController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectCostController.class);
    
    @Autowired
    private ProjectCostService projectCostService;
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 获取项目成本列表（分页）
     */
    @GetMapping("/project/{projectId}")
    public Result<IPage<ProjectCost>> getProjectCosts(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String costType) {
        
        logger.info("获取项目成本列表，项目ID: {}, 页码: {}, 大小: {}, 类型: {}", projectId, current, size, costType);
        
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目成本信息");
            }
            
            Page<ProjectCost> page = new Page<>(current, size);
            QueryWrapper<ProjectCost> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("project_id", projectId);
            
            if (costType != null && !costType.isEmpty()) {
                queryWrapper.eq("cost_type", costType);
            }
            
            queryWrapper.orderByDesc("incurred_date", "create_time");
            
            IPage<ProjectCost> costs = projectCostService.getProjectCostsPage(page, queryWrapper);
            logger.info("成功获取项目成本，数量: {}", costs.getRecords().size());
            return Result.success("获取项目成本成功", costs);
        } catch (Exception e) {
            logger.error("获取项目成本失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            return Result.error("获取项目成本失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目成本统计
     */
    @GetMapping("/project/{projectId}/statistics")
    public Result<Map<String, Object>> getProjectCostStatistics(@PathVariable Long projectId) {
        logger.info("获取项目成本统计，项目ID: {}", projectId);
        
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectId)) {
                return Result.error("无权限访问该项目成本统计");
            }
            
            Map<String, Object> statistics = projectCostService.getProjectCostStatistics(projectId);
            logger.info("成功获取成本统计: {}", statistics);
            return Result.success("获取成本统计成功", statistics);
        } catch (Exception e) {
            logger.error("获取成本统计失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            return Result.error("获取成本统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建项目成本
     */
    @PostMapping
    public Result<String> createProjectCost(@RequestBody ProjectCost projectCost) {
        logger.info("创建项目成本: {}", projectCost);
        
        try {
            // 检查项目访问权限
            if (!hasProjectAccess(projectCost.getProjectId())) {
                return Result.error("无权限操作该项目成本");
            }
            
            // 设置默认值
            if (projectCost.getCurrency() == null) {
                projectCost.setCurrency("CNY");
            }
            if (projectCost.getStatus() == null) {
                projectCost.setStatus(ProjectCost.CostStatus.PENDING);
            }
            
            projectCostService.createProjectCost(projectCost);
            logger.info("成功创建项目成本");
            return Result.success("成本记录创建成功");
        } catch (Exception e) {
            logger.error("创建成本记录失败: {}", e.getMessage(), e);
            return Result.error("创建成本记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新项目成本
     */
    @PutMapping("/{id}")
    public Result<String> updateProjectCost(@PathVariable Long id, @RequestBody ProjectCost projectCost) {
        logger.info("更新项目成本，ID: {}, 数据: {}", id, projectCost);
        
        try {
            // 检查现有记录的项目访问权限
            ProjectCost existingCost = projectCostService.getById(id);
            if (existingCost == null || !hasProjectAccess(existingCost.getProjectId())) {
                return Result.error("无权限操作该项目成本");
            }
            
            projectCost.setId(id);
            projectCostService.updateProjectCost(projectCost);
            logger.info("成功更新项目成本");
            return Result.success("成本记录更新成功");
        } catch (Exception e) {
            logger.error("更新成本记录失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("更新成本记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除项目成本
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteProjectCost(@PathVariable Long id) {
        logger.info("删除项目成本，ID: {}", id);
        
        try {
            // 检查现有记录的项目访问权限
            ProjectCost existingCost = projectCostService.getById(id);
            if (existingCost == null || !hasProjectAccess(existingCost.getProjectId())) {
                return Result.error("无权限操作该项目成本");
            }
            
            projectCostService.deleteProjectCost(id);
            logger.info("成功删除项目成本");
            return Result.success("成本记录删除成功");
        } catch (Exception e) {
            logger.error("删除成本记录失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("删除成本记录失败: " + e.getMessage());
        }
    }
    
    /**
     * 审批项目成本
     */
    @PostMapping("/{id}/approve")
    public Result<String> approveProjectCost(@PathVariable Long id, @RequestParam String status) {
        logger.info("审批项目成本，ID: {}, 状态: {}", id, status);
        
        try {
            // 检查现有记录的项目访问权限
            ProjectCost existingCost = projectCostService.getById(id);
            if (existingCost == null || !hasProjectAccess(existingCost.getProjectId())) {
                return Result.error("无权限审批该项目成本");
            }
            
            projectCostService.approveProjectCost(id, status);
            logger.info("成功审批项目成本");
            return Result.success("成本审批完成");
        } catch (Exception e) {
            logger.error("成本审批失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("成本审批失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Result<String> healthCheck() {
        logger.info("项目成本控制器健康检查");
        return Result.success("项目成本控制器运行正常");
    }
    
    /**
     * 检查项目访问权限
     */
    private boolean hasProjectAccess(Long projectId) {
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (currentUserId == null || projectId == null) {
            return false;
        }
        
        return projectService.canUserAccessProject(currentUserId, projectId);
    }
}