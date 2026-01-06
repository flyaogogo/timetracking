package com.timetracking.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.timetracking.entity.User;
import com.timetracking.service.UserService;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public Result getUserList(@RequestParam(defaultValue = "1") int current,
                             @RequestParam(defaultValue = "10") int size,
                             @RequestParam(required = false) String keyword) {
        IPage<User> page = userService.getUserList(current, size, keyword);
        return Result.success("获取用户列表成功", page);
    }
    
    @GetMapping("/{id}")
    public Result getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success("获取用户信息成功", user);
    }
    
    @PostMapping
    public Result createUser(@RequestBody User user) {
        if (userService.createUser(user)) {
            return Result.success("创建用户成功");
        }
        return Result.error("用户名已存在");
    }
    
    @PutMapping("/{id}")
    public Result updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        if (userService.updateUser(user)) {
            return Result.success("更新用户成功");
        }
        return Result.error("更新用户失败");
    }
    
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable Long id) {
        if (userService.removeById(id)) {
            return Result.success("删除用户成功");
        }
        return Result.error("删除用户失败");
    }
}