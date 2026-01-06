package com.timetracking.service;

import com.timetracking.entity.ProjectMember;
import com.timetracking.mapper.ProjectMemberMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目成员服务
 */
@Service
public class ProjectMemberService {
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    /**
     * 获取项目成员列表
     */
    public List<ProjectMember> getProjectMembers(Long projectId) {
        return projectMemberMapper.selectByProjectId(projectId);
    }
    
    /**
     * 获取项目成员统计
     */
    public Map<String, Object> getProjectMemberStatistics(Long projectId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总成员数
        Integer totalMembers = projectMemberMapper.getTotalMembersByProject(projectId);
        statistics.put("totalMembers", totalMembers != null ? totalMembers : 0);
        
        // 按角色分组统计
        List<Map<String, Object>> membersByRole = projectMemberMapper.getMembersByRoleAndProject(projectId);
        statistics.put("membersByRole", membersByRole);
        
        // 按技能等级分组统计
        List<Map<String, Object>> membersBySkill = projectMemberMapper.getMembersBySkillAndProject(projectId);
        statistics.put("membersBySkill", membersBySkill);
        
        // 平均绩效评分
        Double avgPerformance = projectMemberMapper.getAvgPerformanceByProject(projectId);
        statistics.put("avgPerformance", avgPerformance != null ? avgPerformance : 0.0);
        
        // 平均生产力指数
        Double avgProductivity = projectMemberMapper.getAvgProductivityByProject(projectId);
        statistics.put("avgProductivity", avgProductivity != null ? avgProductivity : 1.0);
        
        return statistics;
    }
    
    /**
     * 添加项目成员
     */
    public void addProjectMember(ProjectMember projectMember) {
        // 检查是否已存在
        ProjectMember existing = projectMemberMapper.selectByProjectAndUser(
            projectMember.getProjectId(), projectMember.getUserId());
        if (existing != null) {
            throw new RuntimeException("该用户已是项目成员");
        }
        
        projectMember.setJoinDate(LocalDate.now());
        projectMember.setCreateTime(LocalDateTime.now());
        
        // 设置默认值
        if (projectMember.getAllocationPercentage() == null) {
            projectMember.setAllocationPercentage(BigDecimal.valueOf(100));
        }
        if (projectMember.getSkillLevel() == null) {
            projectMember.setSkillLevel(ProjectMember.SkillLevel.INTERMEDIATE);
        }
        if (projectMember.getPerformanceRating() == null) {
            projectMember.setPerformanceRating(BigDecimal.valueOf(3.0));
        }
        if (projectMember.getProductivityIndex() == null) {
            projectMember.setProductivityIndex(BigDecimal.valueOf(1.0));
        }
        
        projectMemberMapper.insert(projectMember);
    }
    
    /**
     * 更新项目成员
     */
    public void updateProjectMember(ProjectMember projectMember) {
        projectMemberMapper.updateById(projectMember);
    }
    
    /**
     * 移除项目成员
     */
    public void removeProjectMember(Long id) {
        projectMemberMapper.deleteById(id);
    }
    
    /**
     * 批量添加项目成员
     */
    public void batchAddProjectMembers(List<ProjectMember> members) {
        for (ProjectMember member : members) {
            try {
                addProjectMember(member);
            } catch (Exception e) {
                // 记录错误但继续处理其他成员
                System.err.println("添加成员失败: " + e.getMessage());
            }
        }
    }
}