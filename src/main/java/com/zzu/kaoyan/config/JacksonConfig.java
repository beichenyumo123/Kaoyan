package com.zzu.kaoyan.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.zzu.kaoyan.common.handler.XssStringDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            builder.serializerByType(Long.class, ToStringSerializer.instance);
        };
    }

    /**
     * 注册全局 XSS 清洗模块：所有 JSON String 字段反序列化时自动执行 Jsoup.clean()
     */
    @Bean
    public Module xssCleanModule() {
        SimpleModule module = new SimpleModule("XssCleanModule");
        module.addDeserializer(String.class, new XssStringDeserializer());
        return module;
    }
}