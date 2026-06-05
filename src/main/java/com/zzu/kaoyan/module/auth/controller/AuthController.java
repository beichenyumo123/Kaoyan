package com.zzu.kaoyan.module.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.util.IdUtil;
import com.zzu.kaoyan.common.annotation.RateLimit;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.auth.entity.LoginDTO;
import com.zzu.kaoyan.module.auth.entity.LoginVO;
import com.zzu.kaoyan.module.auth.entity.RegisterDTO;
import com.zzu.kaoyan.module.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "登录与注册", description = "用户注册与登录接口")
public class AuthController {
    private final AuthService authService;
    private final StringRedisTemplate stringRedisTemplate;

    public AuthController(AuthService authService, StringRedisTemplate stringRedisTemplate) {
        this.authService = authService;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Operation(summary = "获取图形验证码", description = "生成4位验证码图片，返回 base64 和 uuid")
    @GetMapping("/captcha")
    @RateLimit(time = 60, maxCount = 10, message = "验证码请求过于频繁，请稍后再试")
    public Result<Map<String, String>> getCaptcha() {
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(150, 50, 4, 4);

        String uuid = IdUtil.fastSimpleUUID();
        String redisKey = "captcha:" + uuid;

        stringRedisTemplate.opsForValue().set(redisKey, captcha.getCode(), 2, TimeUnit.MINUTES);

        Map<String, String> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("base64", captcha.getImageBase64Data());
        return Result.success(result);
    }

    @Operation(summary = "用户注册", description = "接收用户注册信息，密码将在后端加密存储，注册成功后自动登录并返回Token")
    @PostMapping("/register")
    public Result<LoginVO> register(@Validated @RequestBody RegisterDTO registerDTO) {
        validateCaptcha(registerDTO.getCaptchaCode(), registerDTO.getCaptchaUuid());

        User user = authService.register(registerDTO);

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRole(user.getRole());
        loginVO.setAvatarUrl(user.getAvatarUrl());

        log.info("注册成功并自动登录！{},{}", loginVO.getUserId(), loginVO.getUsername());

        return Result.success(loginVO);
    }

    @Operation(summary = "用户登录", description = "支持邮箱/手机号+密码登录，返回JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO){
        validateCaptcha(loginDTO.getCaptchaCode(), loginDTO.getCaptchaUuid());

        User user = authService.verifyAccountAndPassword(loginDTO.getAccount(), loginDTO.getPassword());

        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRole(user.getRole());
        loginVO.setAvatarUrl(user.getAvatarUrl());

        log.info("登录成功！{},{}",loginVO.getUserId(),loginVO.getUsername());

        return Result.success(loginVO);
    }



    @Operation(summary = "用户注销")
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

    /**
     * 校验图形验证码，校验通过后删除 Redis 中的验证码（一次性使用）
     */
    private void validateCaptcha(String captchaCode, String captchaUuid) {
        String redisKey = "captcha:" + captchaUuid;
        String redisCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (redisCode == null || !redisCode.equalsIgnoreCase(captchaCode)) {
            throw new BusinessException(400, "验证码错误或已过期");
        }
        stringRedisTemplate.delete(redisKey);
    }
}
