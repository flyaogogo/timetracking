CREATE TABLE IF NOT EXISTS project_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    old_status VARCHAR(20) NOT NULL,
    new_status VARCHAR(20) NOT NULL,
    change_reason VARCHAR(255) DEFAULT NULL,
    changed_by VARCHAR(50) NOT NULL,
    changed_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    INDEX idx_project_id (project_id),
    INDEX idx_changed_time (changed_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;