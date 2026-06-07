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

    /** Chat API 地址（OpenAI 兼容模式）*/
    @Value("${interview.ai.qwen.api-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    public String apiUrl;

    /** TTS 语音合成 API 地址（CosyVoice HTTP API）*/
    @Value("${interview.ai.qwen.tts-api-url:https://dashscope.aliyuncs.com/api/v1/services/audio/tts/SpeechSynthesizer}")
    public String ttsApiUrl;

    /** DashScope API Key（Bearer Token）*/
    @Value("${interview.ai.qwen.api-key:}")
    public String apiKey;

    /** Chat 模型名称 */
    public static final String MODEL = "qwen3.7-plus";

    /** TTS 模型名称 */
    public static final String TTS_MODEL = "cosyvoice-v1";

    /** TTS 中文语音（女声，推荐） */
    public static final String TTS_VOICE_CN = "longxiaochun";

    /** TTS 英文语音（女声） */
    public static final String TTS_VOICE_EN = "loongstella";

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
