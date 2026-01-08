package com.timetracking.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timetracking.entity.ProjectStatusHistory;
import com.timetracking.mapper.ProjectStatusHistoryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目状态变更历史Service
 */
@Service
public class ProjectStatusHistoryService extends ServiceImpl<ProjectStatusHistoryMapper, ProjectStatusHistory> {

    private static final Logger logger = LoggerFactory.getLogger(ProjectStatusHistoryService.class);

    @Autowired
    private ProjectStatusHistoryMapper projectStatusHistoryMapper;

    /**
     * 根据项目ID获取状态变更历史
     * @param projectId 项目ID
     * @return 状态变更历史列表
     */
    public List<ProjectStatusHistory> getByProjectId(Long projectId) {
        logger.info("获取项目状态变更历史，项目ID: {}", projectId);
        List<ProjectStatusHistory> historyList = projectStatusHistoryMapper.getByProjectId(projectId);
        logger.info("获取到状态变更历史数量: {}", historyList.size());
        return historyList;
    }

    /**
     * 获取项目的最新状态变更记录
     * @param projectId 项目ID
     * @return 最新状态变更记录
     */
    public ProjectStatusHistory getLatestByProjectId(Long projectId) {
        return projectStatusHistoryMapper.getLatestByProjectId(projectId);
    }

    /**
     * 保存状态变更历史
     * @param projectStatusHistory 状态变更历史实体
     * @return 是否保存成功
     */
    public boolean saveStatusHistory(ProjectStatusHistory projectStatusHistory) {
        return save(projectStatusHistory);
    }
}
