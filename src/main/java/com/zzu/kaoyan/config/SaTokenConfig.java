package com.zzu.kaoyan.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证配置类
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SaTokenConfig.class);

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin() 登录校验
        registry.addInterceptor(new SaInterceptor(handle -> {
                    try {
                        // 指定需要拦截的路由
                        SaRouter.match("/api/**")
                                // 排除不需要拦截的路由（放行登录、注册、开放接口、Swagger文档等）
                                .notMatch(
                                        "/api/auth/login",
                                        "/api/auth/register",
                                        "/api/auth/captcha",
                                        "/api/boards",
                                        "/api/ai/ask/stream",   // SSE 流式端点，内部手动校验
                                        "/api/school-select/schools",
                                    "/swagger-ui/**",
                                        "/api-docs/**",
                                        "/doc.html"
                                )
                                // 匹配上的路由执行校验逻辑
                                .check(r -> StpUtil.checkLogin());
                    } catch (SaTokenContextException e) {
                        // SaToken 上下文未初始化（SSE async dispatch / error page 转发时），
                        // 异常发生在 SaRouter.match() 内部（isMatchCurrURI → SaHolder.getRequest），
                        // 此时无法判断路由，直接跳过，由各端点内部自行处理认证
                        log.debug("SaToken 上下文不可用，跳过拦截器校验");
                    }
                }))
                .addPathPatterns("/**");
    }
}