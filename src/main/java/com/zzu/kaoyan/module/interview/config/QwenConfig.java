package com.zzu.kaoyan.module.interview.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 通义千问 (DashScope) API 配置类
 * <p>
 * 使用 OpenAI 兼容接口：https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
 * 模型选择：qwen3.7-plus / qwen-plus（均衡）/ qwen-max（最强）/ qwen-turbo（最快）
 */
@Configuration
public class QwenConfig {

    // ============================================================
    // 以下值从 application.properties 读取，未配置时使用默认值
    // ============================================================

    /** API 地址（OpenAI 兼容模式）*/
    @Value("${interview.ai.qwen.api-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    public String apiUrl;

    /** DashScope API Key（Bearer Token）*/
    @Value("${interview.ai.qwen.api-key:}")
    public String apiKey;

    /** 模型名称 */
    public static final String MODEL = "qwen3.7-plus";

    /** 默认温度 */
    public static final Double TEMPERATURE = 0.7;

    /** 单次最大生成 Token 数 */
    public static final Integer MAX_TOKENS = 1024;

    // ============================================================

    @Bean
    public RestTemplate qwenRestTemplate() {
        return new RestTemplate();
    }
}
