package com.timetracking.controller;

import com.timetracking.entity.Organization;
import com.timetracking.service.OrganizationService;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/organizations")
@CrossOrigin
public class OrganizationController {
    
    @Autowired
    private OrganizationService organizationService;
    
    /**
     * 获取组织树结构
     */
    @GetMapping("/tree")
    public Result getOrganizationTree() {
        try {
            List<Organization> tree = organizationService.getOrganizationTree();
            return Result.success("获取组织架构成功", tree);
        } catch (Exception e) {
            return Result.error("获取组织架构失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有组织的平铺列表（用于下拉选择）
     */
    @GetMapping("/flat")
    public Result getAllOrganizationsFlat() {
        try {
            List<Organization> organizations = organizationService.getAllOrganizationsFlat();
            return Result.success("获取组织列表成功", organizations);
        } catch (Exception e) {
            return Result.error("获取组织列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取用户所属组织
     */
    @GetMapping("/user/{userId}")
    public Result getUserOrganizations(@PathVariable Long userId) {
        try {
            List<Organization> organizations = organizationService.getUserOrganizations(userId);
            return Result.success("获取用户组织成功", organizations);
        } catch (Exception e) {
            return Result.error("获取用户组织失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取组织统计信息
     */
    @GetMapping("/{orgId}/statistics")
    public Result getOrganizationStatistics(@PathVariable Long orgId) {
        try {
            Map<String, Object> statistics = organizationService.getOrganizationStatistics(orgId);
            return Result.success("获取组织统计成功", statistics);
        } catch (Exception e) {
            return Result.error("获取组织统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建组织
     */
    @PostMapping
    public Result createOrganization(@RequestBody Organization organization) {
        try {
            organizationService.createOrganization(organization);
            return Result.success("创建组织成功");
        } catch (Exception e) {
            return Result.error("创建组织失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新组织
     */
    @PutMapping("/{id}")
    public Result updateOrganization(@PathVariable Long id, @RequestBody Organization organization) {
        try {
            organization.setId(id);
            organizationService.updateOrganization(organization);
            return Result.success("更新组织成功");
        } catch (Exception e) {
            return Result.error("更新组织失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除组织
     */
    @DeleteMapping("/{id}")
    public Result deleteOrganization(@PathVariable Long id) {
        try {
            organizationService.deleteOrganization(id);
            return Result.success("删除组织成功");
        } catch (Exception e) {
            return Result.error("删除组织失败：" + e.getMessage());
        }
    }
}