package com.zzu.kaoyan.module.interact.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class TestController {
    //@SaIgnore
    @GetMapping("/mock-login")
    public Result<String> mockLogin() {
        // 强行让 ID 为 1001 的测试用户登录（假设数据库里有这个用户，或者只要是个Long型就行）
        StpUtil.login(1L);
        
        // 获取生成的 Token 字符串并返回
        return Result.success(StpUtil.getTokenValue());
    }
}