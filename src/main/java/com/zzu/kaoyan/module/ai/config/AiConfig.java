package com.zzu.kaoyan.module.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * AI 模块配置：RestTemplate + DeepSeek API 参数。
 */
@Configuration
public class AiConfig {

    @Bean
    @ConfigurationProperties(prefix = "ai.api")
    public AiApiProperties aiApiProperties() {
        return new AiApiProperties();
    }

    /**
     * 专用于 LLM 调用的 RestTemplate，独立配置超时（LLM 响应较慢）。
     */
    @Bean
    public RestTemplate aiRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(60_000);
        return new RestTemplate(factory);
    }
}
