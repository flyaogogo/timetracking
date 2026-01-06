package com.timetracking.controller;

import com.timetracking.vo.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@CrossOrigin
public class TestController {
    
    @GetMapping("/hello")
    public Result hello() {
        return Result.success("Hello World!");
    }
}