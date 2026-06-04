package com.zzu.kaoyan.module.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
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

    /**
     * 专用于 SSE 流式调用的 RestTemplate。
     * 关闭默认错误处理器（避免流式响应中 4xx/5xx 直接抛异常），读取超时设为 5 分钟。
     */
    @Bean
    public RestTemplate aiStreamRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10_000);
        factory.setReadTimeout(300_000);
        RestTemplate rt = new RestTemplate(factory);
        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false; // 不自动抛异常，由调用方处理
            }
        });
        return rt;
    }
}
