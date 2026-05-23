package com.zzu.kaoyan.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 指定需要拦截的路由
                    SaRouter.match("/api/**")
                            // 排除不需要拦截的路由（放行登录、注册、开放接口、Swagger文档等）
                            .notMatch(
                                    "/api/auth/login",
                                    "/api/auth/register",
                                    "/api/auth/captcha",
                                    "/api/boards",
                                    "/api/school-select/schools",
                                    "/swagger-ui/**",
                                    "/api-docs/**",
                                    "/doc.html"
                            )
                            // 匹配上的路由执行校验逻辑
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }
}