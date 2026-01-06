package com.timetracking.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.timetracking.entity.User;
import com.timetracking.entity.UserOrganization;
import com.timetracking.mapper.UserMapper;
import com.timetracking.mapper.UserOrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private UserOrganizationMapper userOrganizationMapper;
    
    public User findByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return getOne(wrapper);
    }
    
    public IPage<User> getUserList(int current, int size, String keyword) {
        Page<User> page = new Page<>(current, size);
        return baseMapper.selectUsersWithOrganization(page);
    }
    
    @Transactional
    public boolean createUser(User user) {
        // 检查用户名是否已存在
        if (findByUsername(user.getUsername()) != null) {
            return false;
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(1);
        
        boolean result = save(user);
        
        // 如果指定了组织，创建用户组织关系
        if (result && user.getOrganizationId() != null) {
            UserOrganization userOrg = new UserOrganization();
            userOrg.setUserId(user.getId());
            userOrg.setOrganizationId(user.getOrganizationId());
            userOrg.setIsPrimary(true);
            userOrg.setPosition(user.getPosition());
            userOrganizationMapper.insert(userOrg);
        }
        
        return result;
    }
    
    @Transactional
    public boolean updateUser(User user) {
        // 如果密码不为空，则加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 不更新密码
        }
        
        boolean result = updateById(user);
        
        // 更新用户组织关系
        if (result && user.getOrganizationId() != null) {
            // 删除现有的主要组织关系
            QueryWrapper<UserOrganization> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", user.getId()).eq("is_primary", true);
            userOrganizationMapper.delete(wrapper);
            
            // 创建新的主要组织关系
            UserOrganization userOrg = new UserOrganization();
            userOrg.setUserId(user.getId());
            userOrg.setOrganizationId(user.getOrganizationId());
            userOrg.setIsPrimary(true);
            userOrg.setPosition(user.getPosition());
            userOrganizationMapper.insert(userOrg);
        }
        
        return result;
    }
    
    public boolean validatePassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}