package com.zzu.kaoyan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 全局跨域配置类
 * 解决前后端分离架构下的跨域请求问题
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名进行跨域调用 (开发阶段为了方便可以设为*，生产环境建议配置具体的前端域名)
        config.addAllowedOriginPattern("*");
        // 允许跨越发送 cookie
        config.setAllowCredentials(true);
        // 放行全部原始头信息
        config.addAllowedHeader("*");
        // 允许所有请求方法跨域调用 (GET, POST, PUT, DELETE, OPTIONS等)
        config.addAllowedMethod("*");
        // 暴露响应头，比如有时候前端需要获取一些自定义的 Header
        config.addExposedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 对所有 API 路由生效
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}