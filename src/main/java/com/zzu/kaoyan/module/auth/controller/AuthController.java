package com.zzu.kaoyan.module.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.auth.entity.LoginDTO;
import com.zzu.kaoyan.module.auth.entity.LoginVO;
import com.zzu.kaoyan.module.auth.entity.RegisterDTO;
import com.zzu.kaoyan.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录与注册", description = "用户注册与登录接口")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "用户注册", description = "接收用户注册信息，密码将在后端加密存储")
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {

        authService.register(registerDTO);

        return Result.success();
    }

    @Operation(summary = "用户登录", description = "支持用户名/邮箱登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO){
        User user = authService.verifyAccountAndPassword(loginDTO.getAccount(), loginDTO.getPassword());

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        // 4. 封装满足《API文档》规范的返回值
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRole(user.getRole());
        loginVO.setAvatarUrl(user.getAvatarUrl());

        return Result.success(loginVO);
    }



    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }
}
