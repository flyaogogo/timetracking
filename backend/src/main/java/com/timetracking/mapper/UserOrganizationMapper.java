package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timetracking.entity.UserOrganization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserOrganizationMapper extends BaseMapper<UserOrganization> {
    
    /**
     * 获取组织成员列表
     */
    @Select("SELECT uo.*, u.real_name as userName, o.org_name as orgName " +
            "FROM user_organizations uo " +
            "LEFT JOIN users u ON uo.user_id = u.id " +
            "LEFT JOIN organizations o ON uo.organization_id = o.id " +
            "WHERE uo.organization_id = #{orgId}")
    List<UserOrganization> selectByOrganizationId(@Param("orgId") Long orgId);
    
    /**
     * 获取用户组织关系
     */
    @Select("SELECT uo.*, u.real_name as userName, o.org_name as orgName " +
            "FROM user_organizations uo " +
            "LEFT JOIN users u ON uo.user_id = u.id " +
            "LEFT JOIN organizations o ON uo.organization_id = o.id " +
            "WHERE uo.user_id = #{userId}")
    List<UserOrganization> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 检查用户是否属于组织
     */
    @Select("SELECT COUNT(*) FROM user_organizations " +
            "WHERE user_id = #{userId} AND organization_id = #{orgId}")
    Integer checkUserInOrganization(@Param("userId") Long userId, @Param("orgId") Long orgId);
}