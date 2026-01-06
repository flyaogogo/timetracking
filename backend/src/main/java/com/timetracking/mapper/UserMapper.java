package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.timetracking.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT u.*, " +
            "GROUP_CONCAT(DISTINCT CONCAT(o.org_name, " +
            "CASE WHEN o.org_type = 'TEAM' THEN CONCAT(' (', parent_org.org_name, ')') ELSE '' END) " +
            "ORDER BY uo.is_primary DESC SEPARATOR ', ') as organization_name, " +
            "MAX(CASE WHEN uo.is_primary = 1 THEN uo.organization_id END) as organization_id " +
            "FROM users u " +
            "LEFT JOIN user_organizations uo ON u.id = uo.user_id " +
            "LEFT JOIN organizations o ON uo.organization_id = o.id " +
            "LEFT JOIN organizations parent_org ON o.parent_id = parent_org.id " +
            "WHERE u.id = #{id} " +
            "GROUP BY u.id")
    User selectUserWithOrganization(@Param("id") Long id);
    
    @Select("SELECT u.*, " +
            "GROUP_CONCAT(DISTINCT CONCAT(o.org_name, " +
            "CASE WHEN o.org_type = 'TEAM' THEN CONCAT(' (', parent_org.org_name, ')') ELSE '' END) " +
            "ORDER BY uo.is_primary DESC SEPARATOR ', ') as organization_name, " +
            "MAX(CASE WHEN uo.is_primary = 1 THEN uo.organization_id END) as organization_id " +
            "FROM users u " +
            "LEFT JOIN user_organizations uo ON u.id = uo.user_id " +
            "LEFT JOIN organizations o ON uo.organization_id = o.id " +
            "LEFT JOIN organizations parent_org ON o.parent_id = parent_org.id " +
            "GROUP BY u.id " +
            "ORDER BY u.create_time DESC")
    IPage<User> selectUsersWithOrganization(Page<User> page);
}