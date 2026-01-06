package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timetracking.entity.ProjectRolePermission;
import com.timetracking.util.EnhancedPermissionUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProjectRolePermissionMapper extends BaseMapper<ProjectRolePermission> {
    
    /**
     * 检查用户在项目中是否有指定权限
     */
    @Select("SELECT COUNT(*) > 0 FROM project_role_permissions " +
            "WHERE user_id = #{userId} AND project_id = #{projectId} " +
            "AND permission_type = #{permissionType} AND is_active = TRUE " +
            "AND (effective_date IS NULL OR effective_date <= CURDATE()) " +
            "AND (expiry_date IS NULL OR expiry_date >= CURDATE())")
    boolean hasActivePermission(@Param("userId") Long userId, 
                               @Param("projectId") Long projectId, 
                               @Param("permissionType") String permissionType);
    
    /**
     * 获取用户在项目中的所有有效权限
     */
    @Select("SELECT permission_type FROM project_role_permissions " +
            "WHERE user_id = #{userId} AND project_id = #{projectId} " +
            "AND is_active = TRUE " +
            "AND (effective_date IS NULL OR effective_date <= CURDATE()) " +
            "AND (expiry_date IS NULL OR expiry_date >= CURDATE())")
    List<EnhancedPermissionUtil.ProjectPermissionType> selectUserProjectPermissions(
            @Param("userId") Long userId, 
            @Param("projectId") Long projectId);
    
    /**
     * 获取用户项目权限详情
     */
    @Select("SELECT prp.*, p.project_name, u.real_name as user_name, " +
            "ub.real_name as granted_by_name " +
            "FROM project_role_permissions prp " +
            "LEFT JOIN projects p ON prp.project_id = p.id " +
            "LEFT JOIN users u ON prp.user_id = u.id " +
            "LEFT JOIN users ub ON prp.granted_by = ub.id " +
            "WHERE prp.user_id = #{userId} AND prp.project_id = #{projectId} " +
            "AND prp.permission_type = #{permissionType}")
    ProjectRolePermission selectByUserProjectPermission(@Param("userId") Long userId,
                                                       @Param("projectId") Long projectId,
                                                       @Param("permissionType") String permissionType);
    
    /**
     * 获取项目的所有权限配置
     */
    @Select("SELECT prp.*, p.project_name, u.real_name as user_name, " +
            "ub.real_name as granted_by_name " +
            "FROM project_role_permissions prp " +
            "LEFT JOIN projects p ON prp.project_id = p.id " +
            "LEFT JOIN users u ON prp.user_id = u.id " +
            "LEFT JOIN users ub ON prp.granted_by = ub.id " +
            "WHERE prp.project_id = #{projectId} " +
            "ORDER BY u.real_name, prp.permission_type")
    IPage<ProjectRolePermission> selectProjectPermissions(Page<ProjectRolePermission> page, 
                                                         @Param("projectId") Long projectId);
    
    /**
     * 获取用户的所有项目权限
     */
    @Select("SELECT prp.*, p.project_name, u.real_name as user_name, " +
            "ub.real_name as granted_by_name " +
            "FROM project_role_permissions prp " +
            "LEFT JOIN projects p ON prp.project_id = p.id " +
            "LEFT JOIN users u ON prp.user_id = u.id " +
            "LEFT JOIN users ub ON prp.granted_by = ub.id " +
            "WHERE prp.user_id = #{userId} " +
            "ORDER BY p.project_name, prp.permission_type")
    IPage<ProjectRolePermission> selectUserPermissions(Page<ProjectRolePermission> page, 
                                                      @Param("userId") Long userId);
    
    /**
     * 撤销用户在项目中的所有权限
     */
    @Update("UPDATE project_role_permissions SET is_active = FALSE, update_time = NOW() " +
            "WHERE user_id = #{userId} AND project_id = #{projectId}")
    int revokeUserProjectPermissions(@Param("projectId") Long projectId, 
                                   @Param("userId") Long userId);
    
    /**
     * 撤销用户在项目中的特定权限
     */
    @Update("UPDATE project_role_permissions SET is_active = FALSE, update_time = NOW() " +
            "WHERE user_id = #{userId} AND project_id = #{projectId} " +
            "AND permission_type = #{permissionType}")
    int revokeUserProjectPermission(@Param("projectId") Long projectId, 
                                  @Param("userId") Long userId,
                                  @Param("permissionType") String permissionType);
    
    /**
     * 激活用户在项目中的特定权限
     */
    @Update("UPDATE project_role_permissions SET is_active = TRUE, update_time = NOW() " +
            "WHERE user_id = #{userId} AND project_id = #{projectId} " +
            "AND permission_type = #{permissionType}")
    int activateUserProjectPermission(@Param("projectId") Long projectId, 
                                    @Param("userId") Long userId,
                                    @Param("permissionType") String permissionType);
    
    /**
     * 获取即将过期的权限
     */
    @Select("SELECT prp.*, p.project_name, u.real_name as user_name " +
            "FROM project_role_permissions prp " +
            "LEFT JOIN projects p ON prp.project_id = p.id " +
            "LEFT JOIN users u ON prp.user_id = u.id " +
            "WHERE prp.is_active = TRUE " +
            "AND prp.expiry_date IS NOT NULL " +
            "AND prp.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL #{days} DAY) " +
            "ORDER BY prp.expiry_date, p.project_name, u.real_name")
    List<ProjectRolePermission> selectExpiringPermissions(@Param("days") int days);
    
    /**
     * 清理过期权限
     */
    @Update("UPDATE project_role_permissions SET is_active = FALSE, update_time = NOW() " +
            "WHERE is_active = TRUE AND expiry_date IS NOT NULL AND expiry_date < CURDATE()")
    int cleanupExpiredPermissions();
}