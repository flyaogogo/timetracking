package com.timetracking.controller;

import com.timetracking.entity.ProjectMember;
import com.timetracking.service.ProjectMemberService;
import com.timetracking.service.ProjectRoleDisplayService;
import com.timetracking.vo.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目成员管理控制器 - 修复版本
 */
@RestController
@RequestMapping("/project-members")
@CrossOrigin(origins = "*")
public class ProjectMemberController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectMemberController.class);
    
    @Autowired
    private ProjectMemberService projectMemberService;
    
    @Autowired
    private ProjectRoleDisplayService projectRoleDisplayService;
    
    /**
     * 获取项目成员列表
     */
    @GetMapping("/project/{projectId}")
    public Result<List<ProjectMember>> getProjectMembers(@PathVariable Long projectId) {
        logger.info("获取项目成员列表，项目ID: {}", projectId);
        
        try {
            // 使用新的角色显示服务获取成员列表
            List<ProjectMember> members = projectRoleDisplayService.getProjectMembersWithDisplayRole(projectId);
            logger.info("成功获取项目成员，数量: {}", members.size());
            return Result.success("获取项目成员成功", members);
        } catch (Exception e) {
            logger.error("获取项目成员失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            return Result.error("获取项目成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取项目成员统计
     */
    @GetMapping("/project/{projectId}/statistics")
    public Result<Map<String, Object>> getProjectMemberStatistics(@PathVariable Long projectId) {
        logger.info("获取项目成员统计，项目ID: {}", projectId);
        
        try {
            Map<String, Object> statistics = projectMemberService.getProjectMemberStatistics(projectId);
            logger.info("成功获取成员统计: {}", statistics);
            return Result.success("获取成员统计成功", statistics);
        } catch (Exception e) {
            logger.error("获取成员统计失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            
            // 返回默认统计数据，避免前端报错
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalMembers", 0);
            defaultStats.put("avgPerformance", 0.0);
            defaultStats.put("avgProductivity", 1.0);
            return Result.success("获取成员统计成功（默认值）", defaultStats);
        }
    }
    
    /**
     * 添加项目成员
     */
    @PostMapping
    public Result<String> addProjectMember(@RequestBody ProjectMember projectMember) {
        logger.info("添加项目成员: {}", projectMember);
        
        try {
            // 验证必需字段
            if (projectMember.getProjectId() == null) {
                return Result.error("项目ID不能为空");
            }
            if (projectMember.getUserId() == null) {
                return Result.error("用户ID不能为空");
            }
            
            // 设置默认值
            if (projectMember.getRole() == null) {
                projectMember.setRole(ProjectMember.ProjectRole.DEVELOPER);
            }
            if (projectMember.getSkillLevel() == null) {
                projectMember.setSkillLevel(ProjectMember.SkillLevel.INTERMEDIATE);
            }
            if (projectMember.getAllocationPercentage() == null) {
                projectMember.setAllocationPercentage(BigDecimal.valueOf(100));
            }
            if (projectMember.getPerformanceRating() == null) {
                projectMember.setPerformanceRating(BigDecimal.valueOf(3.5));
            }
            if (projectMember.getProductivityIndex() == null) {
                projectMember.setProductivityIndex(BigDecimal.valueOf(1.1));
            }
            if (projectMember.getHourlyRate() == null) {
                projectMember.setHourlyRate(BigDecimal.valueOf(600));
            }
            if (projectMember.getJoinDate() == null) {
                projectMember.setJoinDate(LocalDate.now());
            }
            
            projectMemberService.addProjectMember(projectMember);
            logger.info("成功添加项目成员");
            return Result.success("添加成员成功");
        } catch (Exception e) {
            logger.error("添加项目成员失败: {}", e.getMessage(), e);
            return Result.error("添加成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新项目成员
     */
    @PutMapping("/{id}")
    public Result<String> updateProjectMember(@PathVariable Long id, @RequestBody ProjectMember projectMember) {
        logger.info("更新项目成员，ID: {}, 数据: {}", id, projectMember);
        
        try {
            projectMember.setId(id);
            projectMemberService.updateProjectMember(projectMember);
            logger.info("成功更新项目成员");
            return Result.success("更新成员成功");
        } catch (Exception e) {
            logger.error("更新项目成员失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("更新成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 移除项目成员
     */
    @DeleteMapping("/{id}")
    public Result<String> removeProjectMember(@PathVariable Long id) {
        logger.info("移除项目成员，ID: {}", id);
        
        try {
            projectMemberService.removeProjectMember(id);
            logger.info("成功移除项目成员");
            return Result.success("移除成员成功");
        } catch (Exception e) {
            logger.error("移除项目成员失败，ID: {}, 错误: {}", id, e.getMessage(), e);
            return Result.error("移除成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量添加项目成员
     */
    @PostMapping("/batch")
    public Result<String> batchAddProjectMembers(@RequestBody List<ProjectMember> members) {
        logger.info("批量添加项目成员，数量: {}", members.size());
        
        try {
            projectMemberService.batchAddProjectMembers(members);
            logger.info("成功批量添加项目成员");
            return Result.success("批量添加成员成功");
        } catch (Exception e) {
            logger.error("批量添加项目成员失败: {}", e.getMessage(), e);
            return Result.error("批量添加成员失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Result<String> healthCheck() {
        logger.info("项目成员控制器健康检查");
        return Result.success("项目成员控制器运行正常");
    }
    
    /**
     * 修复项目成员角色数据
     */
    @PostMapping("/project/{projectId}/fix-roles")
    public Result<String> fixProjectMemberRoles(@PathVariable Long projectId) {
        logger.info("修复项目成员角色数据，项目ID: {}", projectId);
        
        try {
            projectRoleDisplayService.fixProjectMemberRoles(projectId);
            logger.info("成功修复项目成员角色数据");
            return Result.success("修复项目成员角色成功");
        } catch (Exception e) {
            logger.error("修复项目成员角色失败，项目ID: {}, 错误: {}", projectId, e.getMessage(), e);
            return Result.error("修复项目成员角色失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户在所有项目中的角色信息
     */
    @GetMapping("/user/{userId}/roles")
    public Result<List<Map<String, Object>>> getUserProjectRoles(@PathVariable Long userId) {
        logger.info("获取用户项目角色信息，用户ID: {}", userId);
        
        try {
            List<Map<String, Object>> roles = projectRoleDisplayService.getUserProjectRoles(userId);
            logger.info("成功获取用户项目角色，数量: {}", roles.size());
            return Result.success("获取用户项目角色成功", roles);
        } catch (Exception e) {
            logger.error("获取用户项目角色失败，用户ID: {}, 错误: {}", userId, e.getMessage(), e);
            return Result.error("获取用户项目角色失败: " + e.getMessage());
        }
    }
}