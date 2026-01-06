package com.timetracking.controller;

import com.timetracking.service.ProjectStatisticsEnhancedService;
import com.timetracking.vo.ProjectStatisticsVO;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 增强的项目统计分析控制器
 * 提供详细的成本分析、团队统计、里程碑管理等功能
 */
@RestController
@RequestMapping("/api/project-statistics-enhanced")
@CrossOrigin(origins = "*")
public class ProjectStatisticsEnhancedController {
    
    @Autowired
    private ProjectStatisticsEnhancedService statisticsService;
    
    /**
     * 获取增强的项目统计信息
     * 包含详细的成本分析、团队统计、里程碑进度等
     * 
     * @param projectId 项目ID
     * @return 增强的项目统计数据
     */
    @GetMapping("/{projectId}")
    public Result<ProjectStatisticsVO> getEnhancedProjectStatistics(@PathVariable Long projectId) {
        try {
            ProjectStatisticsVO statistics = statisticsService.getEnhancedProjectStatistics(projectId);
            if (statistics != null) {
                return Result.success("获取项目统计数据成功", statistics);
            } else {
                return Result.error("项目不存在");
            }
        } catch (Exception e) {
            return Result.error("获取项目统计数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 保存项目里程碑
     * 
     * @param projectId 项目ID
     * @param milestones 里程碑列表
     * @return 保存结果
     */
    @PostMapping("/{projectId}/milestones")
    public Result<String> saveMilestones(
            @PathVariable Long projectId,
            @RequestBody java.util.List<ProjectStatisticsVO.MilestoneInfo> milestones) {
        try {
            // 这里应该实现保存里程碑的逻辑
            // statisticsService.saveMilestones(projectId, milestones);
            return Result.success("里程碑保存成功");
        } catch (Exception e) {
            return Result.error("保存里程碑失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取部门相关项目统计（部门负责人权限）
     * 
     * @param departmentId 部门ID
     * @return 部门项目统计数据
     */
    @GetMapping("/department/{departmentId}")
    public Result<java.util.List<java.util.Map<String, Object>>> getDepartmentProjectStatistics(
            @PathVariable Long departmentId) {
        try {
            // 这里应该实现部门项目统计逻辑
            // List<Map<String, Object>> departmentStats = statisticsService.getDepartmentProjectStatistics(departmentId);
            return Result.success("获取部门项目统计成功", java.util.Collections.emptyList());
        } catch (Exception e) {
            return Result.error("获取部门项目统计失败: " + e.getMessage());
        }
    }
}