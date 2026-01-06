package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timetracking.entity.UserPreference;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserPreferenceMapper extends BaseMapper<UserPreference> {
    
    /**
     * 根据用户ID获取偏好设置
     */
    @Select("SELECT up.*, p.project_name as default_project_name " +
            "FROM user_preferences up " +
            "LEFT JOIN projects p ON up.default_project_id = p.id " +
            "WHERE up.user_id = #{userId}")
    UserPreference selectByUserId(@Param("userId") Long userId);
}