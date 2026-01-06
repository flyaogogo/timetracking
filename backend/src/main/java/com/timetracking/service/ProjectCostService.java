package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timetracking.entity.ProjectCost;
import com.timetracking.mapper.ProjectCostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目成本服务
 */
@Service
public class ProjectCostService {
    
    @Autowired
    private ProjectCostMapper projectCostMapper;
    
    /**
     * 分页获取项目成本列表
     */
    public IPage<ProjectCost> getProjectCostsPage(Page<ProjectCost> page, QueryWrapper<ProjectCost> queryWrapper) {
        return projectCostMapper.selectPage(page, queryWrapper);
    }
    
    /**
     * 获取项目成本列表
     */
    public List<ProjectCost> getProjectCosts(Long projectId) {
        return projectCostMapper.selectByProjectId(projectId);
    }
    
    /**
     * 根据ID获取成本记录
     */
    public ProjectCost getById(Long id) {
        return projectCostMapper.selectById(id);
    }
    
    /**
     * 获取项目成本统计
     */
    public Map<String, Object> getProjectCostStatistics(Long projectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总成本
        BigDecimal totalCost = projectCostMapper.getTotalCostByProject(projectId);
        statistics.put("totalCost", totalCost != null ? totalCost : BigDecimal.ZERO);
        
        // 已审批成本
        BigDecimal approvedCost = projectCostMapper.getApprovedCostByProject(projectId);
        statistics.put("approvedCost", approvedCost != null ? approvedCost : BigDecimal.ZERO);
        
        // 待审批成本
        BigDecimal pendingCost = projectCostMapper.getPendingCostByProject(projectId);
        statistics.put("pendingCost", pendingCost != null ? pendingCost : BigDecimal.ZERO);
        
        // 成本条目数量
        Integer costCount = projectCostMapper.getCostItemCountByProject(projectId);
        statistics.put("costCount", costCount != null ? costCount : 0);
        
        // 按类型分组的成本
        List<Map<String, Object>> costByType = projectCostMapper.getCostByTypeAndProject(projectId);
        statistics.put("costByType", costByType);
        
        return statistics;
    }
    
    /**
     * 创建项目成本
     */
    public void createProjectCost(ProjectCost projectCost) {
        projectCost.setCreateTime(LocalDateTime.now());
        projectCost.setUpdateTime(LocalDateTime.now());
        
        // 设置默认值
        if (projectCost.getCostVariance() == null) {
            projectCost.setCostVariance(BigDecimal.ZERO);
        }
        if (projectCost.getBudgetImpact() == null) {
            projectCost.setBudgetImpact(ProjectCost.BudgetImpact.WITHIN_BUDGET);
        }
        
        projectCostMapper.insert(projectCost);
    }
    
    /**
     * 更新项目成本
     */
    public void updateProjectCost(ProjectCost projectCost) {
        projectCost.setUpdateTime(LocalDateTime.now());
        projectCostMapper.updateById(projectCost);
    }
    
    /**
     * 删除项目成本
     */
    public void deleteProjectCost(Long id) {
        projectCostMapper.deleteById(id);
    }
    
    /**
     * 审批项目成本
     */
    public void approveProjectCost(Long id, String status) {
        ProjectCost projectCost = new ProjectCost();
        projectCost.setId(id);
        projectCost.setStatus(ProjectCost.CostStatus.valueOf(status));
        projectCost.setApproveTime(LocalDateTime.now());
        projectCost.setUpdateTime(LocalDateTime.now());
        projectCostMapper.updateById(projectCost);
    }
}