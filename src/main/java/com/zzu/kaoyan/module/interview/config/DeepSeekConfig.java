package com.zzu.kaoyan.module.interview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * DeepSeek API 配置类
 * 统一管理 RestTemplate 实例和 API 调用所需的常量
 */
@Configuration
public class DeepSeekConfig {

    // ============================================================
    // TODO：【重要】DeepSeek API 配置 —— 接入前请填写以下三项
    // ============================================================

    /**
     * DeepSeek Chat API 请求地址
     * 参考文档：https://api-docs.deepseek.com/
     */
    public static final String API_URL = "TODO_FILL_DEEPSEEK_API_URL";   // 例如: https://api.deepseek.com/v1/chat/completions

    /**
     * DeepSeek API Key (Bearer Token)
     */
    public static final String API_KEY = "TODO_FILL_DEEPSEEK_API_KEY";   // 例如: sk-xxxxxxxxxxxxxxxx

    /**
     * 使用的模型名称
     */
    public static final String MODEL = "deepseek-chat";

    /**
     * 默认温度参数
     */
    public static final Double TEMPERATURE = 0.7;

    /**
     * 最大生成 Token 数
     */
    public static final Integer MAX_TOKENS = 1024;

    /**
     * HTTP 请求超时时间（秒）
     */
    public static final int TIMEOUT_SECONDS = 60;

    // ============================================================

    /**
     * 创建用于调用 DeepSeek API 的 RestTemplate Bean
     */
    @Bean
    public RestTemplate deepSeekRestTemplate() {
        // 使用简单工厂创建，后续如需连接池可替换为 RestTemplateBuilder 配置
        return new RestTemplate();
    }
}
