package com.timetracking.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.timetracking.entity.ProjectStatusHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 项目状态变更历史Mapper接口
 */
@Mapper
public interface ProjectStatusHistoryMapper extends BaseMapper<ProjectStatusHistory> {

    /**
     * 根据项目ID获取状态变更历史
     * @param projectId 项目ID
     * @return 状态变更历史列表
     */
    @Select("SELECT * FROM project_status_history WHERE project_id = #{projectId} ORDER BY changed_time DESC")
    List<ProjectStatusHistory> getByProjectId(Long projectId);

    /**
     * 获取项目的最新状态变更记录
     * @param projectId 项目ID
     * @return 最新状态变更记录
     */
    @Select("SELECT * FROM project_status_history WHERE project_id = #{projectId} ORDER BY changed_time DESC LIMIT 1")
    ProjectStatusHistory getLatestByProjectId(Long projectId);
}
