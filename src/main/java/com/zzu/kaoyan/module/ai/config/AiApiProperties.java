package com.zzu.kaoyan.module.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DeepSeek API 配置，对应 application.properties 中的 ai.api.* 前缀。
 */
@ConfigurationProperties(prefix = "ai.api")
public class AiApiProperties {

    /** API 地址，兼容 OpenAI 格式的 chat/completions */
    private String endpoint = "https://api.deepseek.com/chat/completions";

    /** API Key */
    private String key = "";

    /** 模型名称 */
    private String model = "deepseek-chat";

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
