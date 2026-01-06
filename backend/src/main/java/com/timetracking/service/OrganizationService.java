package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.timetracking.entity.Organization;
import com.timetracking.entity.UserOrganization;
import com.timetracking.mapper.OrganizationMapper;
import com.timetracking.mapper.UserOrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrganizationService {
    
    @Autowired
    private OrganizationMapper organizationMapper;
    
    @Autowired
    private UserOrganizationMapper userOrganizationMapper;
    
    /**
     * 获取组织树结构
     */
    public List<Organization> getOrganizationTree() {
        List<Organization> allOrgs = organizationMapper.selectList(
            new QueryWrapper<Organization>().eq("status", "ACTIVE").orderByAsc("org_level", "org_name")
        );
        
        // 为每个组织设置负责人姓名和成员数量
        for (Organization org : allOrgs) {
            if (org.getManagerId() != null) {
                Organization orgWithManager = organizationMapper.selectOrganizationWithManager(org.getId());
                if (orgWithManager != null) {
                    org.setManagerName(orgWithManager.getManagerName());
                }
            }
            
            // 设置成员数量
            Integer memberCount = organizationMapper.getMemberCount(org.getId());
            org.setMemberCount(memberCount);
        }
        
        // 构建树结构
        Map<Long, Organization> orgMap = new HashMap<>();
        for (Organization org : allOrgs) {
            orgMap.put(org.getId(), org);
        }
        
        List<Organization> rootOrgs = allOrgs.stream()
            .filter(org -> org.getParentId() == null)
            .collect(Collectors.toList());
        
        for (Organization org : allOrgs) {
            if (org.getParentId() != null) {
                Organization parent = orgMap.get(org.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new java.util.ArrayList<>());
                    }
                    parent.getChildren().add(org);
                }
            }
        }
        
        return rootOrgs;
    }
    
    /**
     * 获取所有组织的平铺列表（用于下拉选择）
     */
    public List<Organization> getAllOrganizationsFlat() {
        List<Organization> allOrgs = organizationMapper.selectList(
            new QueryWrapper<Organization>().eq("status", "ACTIVE").orderByAsc("org_level", "org_name")
        );
        
        // 为每个组织设置显示名称（包含层级结构）
        Map<Long, Organization> orgMap = new HashMap<>();
        for (Organization org : allOrgs) {
            orgMap.put(org.getId(), org);
        }
        
        for (Organization org : allOrgs) {
            String displayName = buildDisplayName(org, orgMap);
            org.setDisplayName(displayName);
        }
        
        return allOrgs;
    }
    
    /**
     * 构建组织的显示名称（包含层级结构）
     */
    private String buildDisplayName(Organization org, Map<Long, Organization> orgMap) {
        if (org.getParentId() == null) {
            return org.getOrgName();
        }
        
        Organization parent = orgMap.get(org.getParentId());
        if (parent == null) {
            return org.getOrgName();
        }
        
        // 如果是团队，显示为 "团队名 (部门名)"
        if ("TEAM".equals(org.getOrgType())) {
            return org.getOrgName() + " (" + parent.getOrgName() + ")";
        }
        
        // 其他情况显示完整路径
        String parentPath = buildDisplayName(parent, orgMap);
        return parentPath + " / " + org.getOrgName();
    }
    
    /**
     * 获取用户所属组织
     */
    public List<Organization> getUserOrganizations(Long userId) {
        return organizationMapper.selectUserOrganizations(userId);
    }
    
    /**
     * 获取组织统计信息
     */
    public Map<String, Object> getOrganizationStatistics(Long orgId) {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取组织基本信息
        Organization org = organizationMapper.selectById(orgId);
        statistics.put("organization", org);
        
        // 获取成员数量
        Integer memberCount = organizationMapper.getMemberCount(orgId);
        statistics.put("memberCount", memberCount != null ? memberCount : 0);
        
        // 获取项目数量
        Integer projectCount = organizationMapper.getProjectCount(orgId);
        statistics.put("projectCount", projectCount != null ? projectCount : 0);
        
        // 获取本月工时
        Double monthlyHours = organizationMapper.getMonthlyHours(orgId);
        statistics.put("monthlyHours", monthlyHours != null ? monthlyHours : 0.0);
        
        // 获取预算使用情况
        Map<String, Object> budgetInfo = organizationMapper.getBudgetInfo(orgId);
        if (budgetInfo == null) {
            budgetInfo = new HashMap<>();
            budgetInfo.put("budgetAmount", org != null ? org.getBudgetAmount() : 0);
            budgetInfo.put("actualCost", 0);
            budgetInfo.put("utilizationRate", 0);
        }
        statistics.put("budgetInfo", budgetInfo);
        
        return statistics;
    }
    
    /**
     * 创建组织
     */
    public void createOrganization(Organization organization) {
        // 设置组织层级
        if (organization.getParentId() != null) {
            Organization parent = organizationMapper.selectById(organization.getParentId());
            if (parent != null) {
                organization.setOrgLevel(parent.getOrgLevel() + 1);
            }
        } else {
            organization.setOrgLevel(1);
        }
        
        organizationMapper.insert(organization);
    }
    
    /**
     * 更新组织
     */
    public void updateOrganization(Organization organization) {
        organizationMapper.updateById(organization);
    }
    
    /**
     * 删除组织
     */
    public void deleteOrganization(Long id) {
        // 检查是否有子组织
        QueryWrapper<Organization> childWrapper = new QueryWrapper<>();
        childWrapper.eq("parent_id", id);
        long childCount = organizationMapper.selectCount(childWrapper);
        
        if (childCount > 0) {
            throw new RuntimeException("该组织下还有子组织，无法删除");
        }
        
        // 检查是否有成员
        List<UserOrganization> members = userOrganizationMapper.selectList(
            new QueryWrapper<UserOrganization>().eq("organization_id", id)
        );
        
        if (!members.isEmpty()) {
            throw new RuntimeException("该组织下还有成员，无法删除");
        }
        
        // 删除组织
        organizationMapper.deleteById(id);
    }
}