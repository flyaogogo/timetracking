package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timetracking.entity.Task;
import com.timetracking.mapper.TaskMapper;
import com.timetracking.mapper.ProjectMapper;
import com.timetracking.mapper.ProjectMemberMapper;
import com.timetracking.util.PermissionUtil;
import com.timetracking.util.EnhancedPermissionUtil;
import com.timetracking.service.TimeTrackingStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {
    
    @Autowired
    private ProjectMapper projectMapper;
    
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    
    @Autowired
    private TimeTrackingStatisticsService statisticsService;
    
    public IPage<Task> getTaskList(int current, int size, Long projectId, String keyword) {
        Page<Task> page = new Page<>(current, size);
        
        // 获取当前用户信息
        Long currentUserId = PermissionUtil.getCurrentUserId();
        boolean canViewAll = PermissionUtil.canViewAllProjects();
        
        if (canViewAll) {
            // 管理员可以查看所有任务
            if (projectId != null) {
                // 管理员查看指定项目的任务
                return baseMapper.selectTasksByProjectAndKeyword(page, projectId, keyword);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                // 管理员按关键词搜索所有任务
                return baseMapper.selectTasksWithDetailsAndKeyword(page, keyword);
            } else {
                // 管理员查看所有任务
                return baseMapper.selectTasksWithDetails(page);
            }
        } else {
            // 非管理员用户只能查看参与项目中分配给自己的任务
            if (projectId != null) {
                // 检查项目访问权限
                if (!hasProjectAccess(projectId)) {
                    return new Page<>(current, size); // 返回空页面
                }
                // 查看指定项目中分配给自己的任务
                return baseMapper.selectUserRelatedTasksByProject(page, currentUserId, projectId, keyword);
            } else {
                // 查看所有参与项目中分配给自己的任务
                return baseMapper.selectUserRelatedTasks(page, currentUserId, keyword);
            }
        }
    }
    
    /**
     * 根据用户权限获取任务列表
     */
    public IPage<Task> getTaskListByPermission(int current, int size, Long projectId, String keyword, Long userId) {
        Page<Task> page = new Page<>(current, size);
        
        if (userId == null) {
            return page; // 返回空页面
        }
        
        // 管理员可以查看所有任务
        if (PermissionUtil.isAdmin()) {
            if (projectId != null) {
                return baseMapper.selectTasksByProjectAndKeyword(page, projectId, keyword);
            } else {
                return baseMapper.selectTasksWithDetailsAndKeyword(page, keyword);
            }
        }
        
        // 其他用户只能查看参与项目的任务
        if (projectId != null) {
            return baseMapper.selectUserRelatedTasksByProject(page, userId, projectId, keyword);
        } else {
            return baseMapper.selectUserRelatedTasks(page, userId, keyword);
        }
    }
    
    public Task getTaskWithDetails(Long id) {
        Task task = baseMapper.selectTaskWithDetails(id);
        if (task == null) {
            return null;
        }
        
        // 检查任务访问权限
        if (!hasTaskAccess(task)) {
            return null;
        }
        
        return task;
    }
    
    public IPage<Task> getMyTasks(int current, int size, Long userId) {
        // 检查是否有权限查看指定用户的任务
        if (!PermissionUtil.canViewUserData(userId)) {
            return new Page<>(current, size); // 返回空页面
        }
        
        Page<Task> page = new Page<>(current, size);
        // 返回用户参与项目的所有任务，不限制assignee_id
        // 使用selectUserRelatedTasks方法，它会返回该用户参与项目的所有任务
        return baseMapper.selectUserRelatedTasks(page, userId, null);
    }
    
    public IPage<Task> getUserRelatedTasks(int current, int size, Long userId, String keyword) {
        // 检查是否有权限查看指定用户的任务
        if (!PermissionUtil.canViewUserData(userId)) {
            return new Page<>(current, size); // 返回空页面
        }
        
        Page<Task> page = new Page<>(current, size);
        return baseMapper.selectUserRelatedTasks(page, userId, keyword);
    }
    
    /**
     * 创建任务
     */
    public Task createTask(Task task) {
        save(task);
        
        // 创建任务后，更新项目工时汇总
        if (task.getProjectId() != null) {
            statisticsService.updateProjectActualHours(task.getProjectId());
        }
        
        return task;
    }
    
    /**
     * 更新任务
     */
    public Task updateTask(Task task) {
        updateById(task);
        
        // 更新任务后，重新计算实际工时和进度
        if (task.getId() != null) {
            statisticsService.updateTaskActualHoursAndProgress(task.getId());
        }
        
        return getById(task.getId());
    }
    
    /**
     * 删除任务
     */
    public void deleteTask(Long id) {
        Task task = getById(id);
        removeById(id);
        
        // 删除任务后，更新项目工时汇总
        if (task != null && task.getProjectId() != null) {
            statisticsService.updateProjectActualHours(task.getProjectId());
        }
    }
    
    /**
     * 更新任务状态
     */
    public void updateTaskStatus(Long id, String status) {
        Task task = new Task();
        task.setId(id);
        
        // Convert string status to enum
        Task.TaskStatus taskStatus;
        switch (status.toUpperCase()) {
            case "TODO":
                taskStatus = Task.TaskStatus.TODO;
                break;
            case "IN_PROGRESS":
                taskStatus = Task.TaskStatus.IN_PROGRESS;
                break;
            case "REVIEW":
                taskStatus = Task.TaskStatus.REVIEW;
                break;
            case "COMPLETED":
                taskStatus = Task.TaskStatus.COMPLETED;
                break;
            case "PAUSED":
                taskStatus = Task.TaskStatus.PAUSED;
                break;
            case "CANCELLED":
                taskStatus = Task.TaskStatus.CANCELLED;
                break;
            default:
                taskStatus = Task.TaskStatus.TODO;
                break;
        }
        
        task.setStatus(taskStatus);
        updateById(task);
        
        // 状态更新后，重新计算实际工时和进度
        statisticsService.updateTaskActualHoursAndProgress(id);
    }
    
    /**
     * 检查用户是否为项目成员
     */
    public boolean isProjectMember(Long projectId, Long userId) {
        if (projectId == null || userId == null) {
            return false;
        }
        
        try {
            return projectMemberMapper.selectByProjectAndUser(projectId, userId) != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取项目经理任务统计 - 只统计用户担任项目经理的项目的任务
     */
    public Map<String, Object> getProjectManagerTaskStats(Long managerId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (managerId == null) {
            stats.put("totalTasks", 0);
            stats.put("completedTasks", 0);
            stats.put("pendingTasks", 0);
            stats.put("inProgressTasks", 0);
            return stats;
        }
        
        try {
            // 获取用户担任项目经理的项目的任务统计
            Map<String, Object> taskStats = baseMapper.selectProjectManagerTaskStats(managerId);
            
            int totalTasks = taskStats.get("totalTasks") != null ? 
                ((Number) taskStats.get("totalTasks")).intValue() : 0;
            int completedTasks = taskStats.get("completedTasks") != null ? 
                ((Number) taskStats.get("completedTasks")).intValue() : 0;
            int inProgressTasks = taskStats.get("inProgressTasks") != null ? 
                ((Number) taskStats.get("inProgressTasks")).intValue() : 0;
            int pendingTasks = totalTasks - completedTasks - inProgressTasks;
            
            stats.put("totalTasks", totalTasks);
            stats.put("completedTasks", completedTasks);
            stats.put("inProgressTasks", inProgressTasks);
            stats.put("pendingTasks", Math.max(0, pendingTasks));
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            stats.put("totalTasks", 0);
            stats.put("completedTasks", 0);
            stats.put("pendingTasks", 0);
            stats.put("inProgressTasks", 0);
        }
        
        return stats;
    }
    
    /**
     * 获取项目任务概览
     */
    public Map<String, Object> getProjectTasksOverview(Long projectId) {
        Map<String, Object> overview = new HashMap<>();
        
        try {
            QueryWrapper<Task> wrapper = new QueryWrapper<>();
            wrapper.eq("project_id", projectId);
            
            // 总任务数
            int totalTasks = Math.toIntExact(count(wrapper));
            overview.put("totalTasks", totalTasks);
            
            // 已完成任务数
            QueryWrapper<Task> completedWrapper = new QueryWrapper<>();
            completedWrapper.eq("project_id", projectId).eq("status", "COMPLETED");
            int completedTasks = Math.toIntExact(count(completedWrapper));
            overview.put("completedTasks", completedTasks);
            
            // 进行中任务数
            QueryWrapper<Task> inProgressWrapper = new QueryWrapper<>();
            inProgressWrapper.eq("project_id", projectId).eq("status", "IN_PROGRESS");
            int inProgressTasks = Math.toIntExact(count(inProgressWrapper));
            overview.put("inProgressTasks", inProgressTasks);
            
            // 待开始任务数
            QueryWrapper<Task> todoWrapper = new QueryWrapper<>();
            todoWrapper.eq("project_id", projectId).eq("status", "TODO");
            int todoTasks = Math.toIntExact(count(todoWrapper));
            overview.put("todoTasks", todoTasks);
            
            // 计算完成率
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;
            overview.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            
            // 获取项目工时统计
            Map<String, Object> timeStats = statisticsService.getProjectTimeStatistics(projectId);
            overview.putAll(timeStats);
            
        } catch (Exception e) {
            // 如果查询失败，返回默认值
            overview.put("totalTasks", 0);
            overview.put("completedTasks", 0);
            overview.put("inProgressTasks", 0);
            overview.put("todoTasks", 0);
            overview.put("completionRate", 0.0);
        }
        
        return overview;
    }
    
    /**
     * 获取项目经理任务概览
     */
    public Map<String, Object> getProjectManagerTasksOverview(Long managerId) {
        Map<String, Object> overview = new HashMap<>();
        // TODO: Implement actual overview logic
        overview.put("managedProjects", 0);
        overview.put("totalTasks", 0);
        overview.put("completedTasks", 0);
        return overview;
    }
    
    /**
     * 检查项目访问权限
     */
    private boolean hasProjectAccess(Long projectId) {
        // 管理员可以访问所有项目
        if (PermissionUtil.canViewAllProjects()) {
            return true;
        }
        
        // 检查用户是否参与该项目
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        return projectMapper.isUserInProject(currentUserId, projectId) > 0;
    }
    
    /**
     * 检查任务访问权限
     */
    private boolean hasTaskAccess(Task task) {
        // 管理员可以访问所有任务
        if (PermissionUtil.canViewAllProjects()) {
            return true;
        }
        
        Long currentUserId = PermissionUtil.getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        
        // 检查用户是否参与该任务所属的项目
        return hasProjectAccess(task.getProjectId());
    }
    
    /**
     * 下载Excel导入模板
     */
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        // 创建Excel工作簿
        org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.xssf.usermodel.XSSFSheet sheet = workbook.createSheet("任务导入模板");
        
        // 设置列宽
        int[] columnWidths = {20, 15, 30, 15, 10, 12, 12, 12, 15, 15, 20};
        for (int i = 0; i < columnWidths.length; i++) {
            sheet.setColumnWidth(i, columnWidths[i] * 256);
        }
        
        // 创建样式
        org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        headerStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        org.apache.poi.xssf.usermodel.XSSFFont headerFont = workbook.createFont();
        headerFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        
        org.apache.poi.xssf.usermodel.XSSFCellStyle requiredStyle = workbook.createCellStyle();
        requiredStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.YELLOW.getIndex());
        requiredStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        requiredStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        requiredStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        requiredStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        requiredStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        requiredStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
        requiredStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        
        org.apache.poi.xssf.usermodel.XSSFCellStyle exampleStyle = workbook.createCellStyle();
        exampleStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_GREEN.getIndex());
        exampleStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        exampleStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        exampleStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        exampleStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        exampleStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        
        org.apache.poi.xssf.usermodel.XSSFCellStyle noteStyle = workbook.createCellStyle();
        noteStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        noteStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        noteStyle.setBorderTop(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        noteStyle.setBorderBottom(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        noteStyle.setBorderLeft(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        noteStyle.setBorderRight(org.apache.poi.ss.usermodel.BorderStyle.THIN);
        noteStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.LEFT);
        noteStyle.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.TOP);
        noteStyle.setWrapText(true);
        
        // 创建表头行
        org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {"任务名称", "项目ID", "任务描述", "任务类型", "优先级", "执行人ID", "审核人ID", "预估工时", "开始日期", "结束日期", "备注"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.xssf.usermodel.XSSFCell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 创建必填说明行
        org.apache.poi.xssf.usermodel.XSSFRow requiredRow = sheet.createRow(1);
        String[] requiredTexts = {"必填", "必填，项目编号", "可选，任务详细描述", "必填，下拉选择", "必填，1-5", "必填，用户ID", "可选，用户ID", "可选，如8.5", "可选，格式：yyyy-MM-dd", "可选，格式：yyyy-MM-dd", "填写说明"};
        for (int i = 0; i < requiredTexts.length; i++) {
            org.apache.poi.xssf.usermodel.XSSFCell cell = requiredRow.createCell(i);
            cell.setCellValue(requiredTexts[i]);
            cell.setCellStyle(requiredStyle);
        }
        
        // 创建示例数据行
        org.apache.poi.xssf.usermodel.XSSFRow exampleRow = sheet.createRow(2);
        String[] exampleData = {"开发登录功能", "1", "实现用户登录认证", "DEVELOPMENT", "3", "1", "2", "8.0", "2023-01-01", "2023-01-02", "示例数据，可删除"};
        for (int i = 0; i < exampleData.length; i++) {
            org.apache.poi.xssf.usermodel.XSSFCell cell = exampleRow.createCell(i);
            cell.setCellValue(exampleData[i]);
            cell.setCellStyle(exampleStyle);
        }
        
        // 创建填写说明行
        org.apache.poi.xssf.usermodel.XSSFRow noteTitleRow = sheet.createRow(4);
        noteTitleRow.createCell(0).setCellValue("填写说明：");
        noteTitleRow.getCell(0).setCellStyle(noteStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(4, 4, 0, 10));
        
        String[] notes = {
            "1.请使用下拉列表选择任务类型和优先级，确保数据格式正确",
            "2.日期格式必须为yyyy-MM-dd，如2023-01-01",
            "3.项目ID和用户ID必须为数字，可在系统中查询获取",
            "4.预估工时可输入小数，如8.5表示8小时30分钟",
            "5.必填字段必须填写，否则导入会失败"
        };
        for (int i = 0; i < notes.length; i++) {
            org.apache.poi.xssf.usermodel.XSSFRow noteRow = sheet.createRow(5 + i);
            noteRow.createCell(0).setCellValue(notes[i]);
            noteRow.getCell(0).setCellStyle(noteStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(5 + i, 5 + i, 0, 10));
        }
        
        // 设置数据验证（下拉列表）
        // 任务类型下拉列表
        String[] taskTypes = {"DEVELOPMENT", "TESTING", "DESIGN", "DOCUMENT"};
        org.apache.poi.ss.util.CellRangeAddressList taskTypeRange = new org.apache.poi.ss.util.CellRangeAddressList(2, 1000, 3, 3);
        org.apache.poi.ss.usermodel.DataValidationHelper dvHelper = sheet.getDataValidationHelper();
        org.apache.poi.ss.usermodel.DataValidationConstraint taskTypeConstraint = dvHelper.createExplicitListConstraint(taskTypes);
        org.apache.poi.ss.usermodel.DataValidation taskTypeValidation = dvHelper.createValidation(taskTypeConstraint, taskTypeRange);
        sheet.addValidationData(taskTypeValidation);
        
        // 优先级下拉列表
        String[] priorities = {"1", "2", "3", "4", "5"};
        org.apache.poi.ss.util.CellRangeAddressList priorityRange = new org.apache.poi.ss.util.CellRangeAddressList(2, 1000, 4, 4);
        org.apache.poi.ss.usermodel.DataValidationConstraint priorityConstraint = dvHelper.createExplicitListConstraint(priorities);
        org.apache.poi.ss.usermodel.DataValidation priorityValidation = dvHelper.createValidation(priorityConstraint, priorityRange);
        sheet.addValidationData(priorityValidation);
        
        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=task_import_template.xlsx");
        
        // 写入响应流
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
    /**
     * Excel导入任务
     */
    public com.timetracking.vo.Result importTasks(MultipartFile file, Long userId) {
        try {
            // 创建Excel工作簿
            org.apache.poi.ss.usermodel.Workbook workbook;
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.toLowerCase().endsWith(".xlsx")) {
                workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(file.getInputStream());
            } else if (fileName != null && fileName.toLowerCase().endsWith(".xls")) {
                workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook(file.getInputStream());
            } else {
                return com.timetracking.vo.Result.error("不支持的文件格式，请上传.xlsx或.xls文件");
            }
            
            // 获取第一个工作表
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                workbook.close();
                return com.timetracking.vo.Result.error("Excel文件中没有工作表");
            }
            
            // 初始化统计信息
            int successCount = 0;
            int failureCount = 0;
            java.util.List<String> errorMessages = new java.util.ArrayList<>();
            
            // 获取最后一行索引
            int lastRowNum = sheet.getLastRowNum();
            
            // 遍历所有行，从第3行开始（第0行是表头，第1行是必填说明，第2行是示例数据）
            for (int rowNum = 3; rowNum <= lastRowNum; rowNum++) {
                org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowNum);
                if (row == null) continue;
                
                // 检查第一列是否为空，如果为空则跳过该行
                org.apache.poi.ss.usermodel.Cell firstCell = row.getCell(0);
                if (firstCell == null || firstCell.getCellType() == org.apache.poi.ss.usermodel.CellType.BLANK) {
                    continue;
                }
                
                // 获取第一列的值，用于判断是否是说明行
                String firstCellValue = getCellValue(firstCell);
                
                // 跳过说明行：
                // 1. 如果第一列包含"填写说明"或"示例数据"则跳过该行
                if (firstCellValue.contains("填写说明") || firstCellValue.contains("示例数据")) {
                    continue;
                }
                
                // 2. 检查是否是模板说明行（通过检查任务名称列是否包含模板关键字）
                if (firstCellValue.contains("任务名称") || firstCellValue.contains("必填")) {
                    continue;
                }
                
                // 3. 检查是否是填写说明的1-5点内容（以数字+点开头，如"1."、"2."等）
                if (firstCellValue.matches("^\\d+\\..*$")) {
                    continue;
                }
                
                // 4. 检查第11列（备注）是否包含示例数据标记，如果有则跳过
                org.apache.poi.ss.usermodel.Cell noteCell = row.getCell(10);
                if (noteCell != null) {
                    String noteValue = getCellValue(noteCell);
                    if (noteValue.contains("示例数据") || noteValue.contains("可删除")) {
                        continue;
                    }
                }
                
                try {
                    // 创建任务对象
                    com.timetracking.entity.Task task = new com.timetracking.entity.Task();
                    
                    // 读取任务名称（列A，索引0）
                    String taskName = getCellValue(row.getCell(0));
                    if (taskName == null || taskName.trim().isEmpty()) {
                        throw new IllegalArgumentException("任务名称不能为空");
                    }
                    task.setTaskName(taskName);
                    
                    // 读取所属项目ID（列B，索引1）
                    Long projectId = parseLong(getCellValue(row.getCell(1)));
                    if (projectId == null) {
                        throw new IllegalArgumentException("所属项目ID不能为空");
                    }
                    task.setProjectId(projectId);
                    
                    // 读取任务描述（列C，索引2）
                    String description = getCellValue(row.getCell(2));
                    task.setDescription(description);
                    
                    // 读取任务类型（列D，索引3）
                    String taskTypeStr = getCellValue(row.getCell(3));
                    if (taskTypeStr == null || taskTypeStr.trim().isEmpty()) {
                        throw new IllegalArgumentException("任务类型不能为空");
                    }
                    com.timetracking.entity.Task.TaskType taskType;
                    try {
                        taskType = com.timetracking.entity.Task.TaskType.valueOf(taskTypeStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("无效的任务类型：" + taskTypeStr + "，请使用下拉列表选择");
                    }
                    task.setTaskType(taskType);
                    
                    // 读取优先级（列E，索引4）
                    Integer priority = parseInt(getCellValue(row.getCell(4)));
                    if (priority == null) {
                        throw new IllegalArgumentException("优先级不能为空");
                    }
                    if (priority < 1 || priority > 5) {
                        throw new IllegalArgumentException("优先级必须在1-5之间");
                    }
                    task.setPriority(priority);
                    
                    // 读取执行人ID（列F，索引5）
                    Long assigneeId = parseLong(getCellValue(row.getCell(5)));
                    if (assigneeId == null) {
                        throw new IllegalArgumentException("执行人ID不能为空");
                    }
                    task.setAssigneeId(assigneeId);
                    
                    // 读取审核人ID（列G，索引6）
                    Long reviewerId = parseLong(getCellValue(row.getCell(6)));
                    task.setReviewerId(reviewerId);
                    
                    // 读取预估工时（列H，索引7）
                    Double estimatedHoursDouble = parseDouble(getCellValue(row.getCell(7)));
                    java.math.BigDecimal estimatedHours = java.math.BigDecimal.ZERO;
                    if (estimatedHoursDouble != null) {
                        estimatedHours = java.math.BigDecimal.valueOf(estimatedHoursDouble);
                    }
                    task.setEstimatedHours(estimatedHours);
                    
                    // 读取开始日期（列I，索引8）
                    org.apache.poi.ss.usermodel.Cell startCell = row.getCell(8);
                    if (startCell != null) {
                        try {
                            java.time.LocalDate startDate;
                            // 检查单元格类型
                            if (startCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                                // 处理Excel数字日期
                                java.util.Date date = startCell.getDateCellValue();
                                startDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                            } else {
                                // 处理字符串日期
                                String startDateStr = getCellValue(startCell);
                                if (startDateStr != null && !startDateStr.trim().isEmpty()) {
                                    startDate = parseDate(startDateStr);
                                } else {
                                    startDate = null;
                                }
                            }
                            if (startDate != null) {
                                task.setStartDate(startDate);
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException("开始日期格式错误，支持的格式有：yyyy-MM-dd, yyyy/MM/dd, yyyy/M/d, yyyy/M/dd, yyyy/MM/d, MM/dd/yyyy, M/d/yyyy, M/dd/yyyy, MM/d/yyyy, dd/MM/yyyy, d/M/yyyy, dd/M/yyyy, d/MM/yyyy, yyyy.MM.dd, yyyy.M.d, yyyy.M.dd, yyyy.MM.d, yyyy年MM月dd日, yyyy年M月d日, yyyy年M月dd日, yyyy年MM月d日");
                        }
                    }
                    
                    // 读取结束日期（列J，索引9）
                    org.apache.poi.ss.usermodel.Cell endCell = row.getCell(9);
                    if (endCell != null) {
                        try {
                            java.time.LocalDate endDate;
                            // 检查单元格类型
                            if (endCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                                // 处理Excel数字日期
                                java.util.Date date = endCell.getDateCellValue();
                                endDate = date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                            } else {
                                // 处理字符串日期
                                String endDateStr = getCellValue(endCell);
                                if (endDateStr != null && !endDateStr.trim().isEmpty()) {
                                    endDate = parseDate(endDateStr);
                                } else {
                                    endDate = null;
                                }
                            }
                            if (endDate != null) {
                                task.setEndDate(endDate);
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException("结束日期格式错误，支持的格式有：yyyy-MM-dd, yyyy/MM/dd, yyyy/M/d, yyyy/M/dd, yyyy/MM/d, MM/dd/yyyy, M/d/yyyy, M/dd/yyyy, MM/d/yyyy, dd/MM/yyyy, d/M/yyyy, dd/M/yyyy, d/MM/yyyy, yyyy.MM.dd, yyyy.M.d, yyyy.M.dd, yyyy.MM.d, yyyy年MM月dd日, yyyy年M月d日, yyyy年M月dd日, yyyy年MM月d日");
                        }
                    }
                    
                    // 读取备注（列K，索引10）
                    String remark = getCellValue(row.getCell(10));
                    // 备注字段可以保存到task的其他字段，或者如果有remark字段可以直接设置
                    // task.setRemark(remark);
                    
                    // 设置默认难度（如果Task实体有difficulty字段）
                    task.setDifficulty(3); // 默认难度为3
                    
                    // 设置默认状态
                    task.setStatus(com.timetracking.entity.Task.TaskStatus.TODO);
                    
                    // 保存任务
                    save(task);
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    errorMessages.add("第" + (rowNum + 1) + "行导入失败：" + e.getMessage());
                }
            }
            
            // 关闭工作簿
            workbook.close();
            
            // 构建返回结果
            java.util.Map<String, Object> resultMap = new java.util.HashMap<>();
            resultMap.put("successCount", successCount);
            resultMap.put("failureCount", failureCount);
            resultMap.put("errorMessages", errorMessages);
            
            return com.timetracking.vo.Result.success("导入任务完成", resultMap);
        } catch (Exception e) {
            return com.timetracking.vo.Result.error("导入任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取单元格值
     */
    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;
        
        org.apache.poi.ss.usermodel.DataFormatter dataFormatter = new org.apache.poi.ss.usermodel.DataFormatter();
        return dataFormatter.formatCellValue(cell).trim();
    }
    
    /**
     * 解析Long类型
     */
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("值必须是有效的数字：" + value);
        }
    }
    
    /**
     * 解析Integer类型
     */
    private Integer parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("值必须是有效的整数：" + value);
        }
    }
    
    /**
     * 解析Double类型
     */
    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("值必须是有效的数字：" + value);
        }
    }
    
    /**
     * 解析多种格式的日期字符串，支持一位和两位的月份/日期
     */
    private java.time.LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        
        // 定义支持的日期格式，使用M/d而不是MM/dd来支持一位和两位的月份/日期
        String[] formats = {
            // 完整格式（两位月两位日）
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "MM/dd/yyyy",
            "dd/MM/yyyy",
            "yyyy.MM.dd",
            "yyyy年MM月dd日",
            // 一位月一位日
            "yyyy/M/d",
            "M/d/yyyy",
            "d/M/yyyy",
            "yyyy.M.d",
            "yyyy年M月d日",
            // 一位月两位日
            "yyyy/M/dd",
            "M/dd/yyyy",
            "dd/M/yyyy",
            "yyyy.M.dd",
            "yyyy年M月dd日",
            // 两位月一位日
            "yyyy/MM/d",
            "MM/d/yyyy",
            "d/MM/yyyy",
            "yyyy.MM.d",
            "yyyy年MM月d日"
        };
        
        for (String format : formats) {
            try {
                return java.time.LocalDate.parse(dateStr, java.time.format.DateTimeFormatter.ofPattern(format));
            } catch (Exception e) {
                // 继续尝试其他格式
            }
        }
        
        // 如果所有格式都无法解析，尝试使用默认格式器
        try {
            return java.time.LocalDate.parse(dateStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法解析日期格式：" + dateStr);
        }
    }
}