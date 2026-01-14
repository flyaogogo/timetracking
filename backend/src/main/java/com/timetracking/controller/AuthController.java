package com.timetracking.controller;

import com.timetracking.entity.User;
import com.timetracking.service.UserService;
import com.timetracking.util.JwtUtil;
import com.timetracking.vo.LoginRequest;
import com.timetracking.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        System.out.println("Login request received: " + loginRequest.getUsername());
        try {
            // 验证验证码
            HttpSession session = request.getSession();
            String storedCaptcha = (String) session.getAttribute("captcha");
            String inputCaptcha = loginRequest.getCaptcha();
            
            System.out.println("Validating captcha...");
            System.out.println("Stored captcha: " + storedCaptcha);
            System.out.println("Input captcha: " + inputCaptcha);
            
            if (storedCaptcha == null || inputCaptcha == null || !storedCaptcha.equalsIgnoreCase(inputCaptcha)) {
                return Result.error("验证码错误");
            }
            
            // 验证码验证通过后移除，防止重复使用
            session.removeAttribute("captcha");
            
            User user = userService.findByUsername(loginRequest.getUsername());
            System.out.println("User found: " + (user != null ? user.getUsername() : "null"));
            
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            if (user.getStatus() == 0) {
                return Result.error("用户已被禁用");
            }
            
            System.out.println("Validating password...");
            // 使用BCrypt密码验证
            boolean passwordValid = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            System.out.println("Password validation result: " + passwordValid);
            
            if (!passwordValid) {
                return Result.error("密码错误");
            }
            
            System.out.println("Password validated successfully, generating JWT token...");
            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("user", user);
            
            System.out.println("Returning success result with JWT token");
            return Result.success("登录成功", data);
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return Result.error("登录失败：" + e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Result getUserInfo() {
        return Result.success("获取用户信息成功");
    }
}