-- 创建项目状态变更历史表
CREATE TABLE IF NOT EXISTS project_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    project_id BIGINT NOT NULL COMMENT '项目ID',
    old_status VARCHAR(20) NOT NULL COMMENT '变更前状态',
    new_status VARCHAR(20) NOT NULL COMMENT '变更后状态',
    change_reason VARCHAR(255) DEFAULT NULL COMMENT '变更原因',
    changed_by VARCHAR(50) NOT NULL COMMENT '变更人',
    changed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_changed_time (changed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='项目状态变更历史表';

-- 插入初始数据示例（可选）
-- INSERT INTO project_status_history (project_id, old_status, new_status, change_reason, changed_by, changed_time) 
-- VALUES (1, 'PLANNING', 'IN_PROGRESS', '项目开始实施', 'admin', NOW());
