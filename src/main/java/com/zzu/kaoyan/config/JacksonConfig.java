package com.zzu.kaoyan.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return builder -> {
            // 将 Long 类型统一转换为 String 返回给前端
            builder.serializerByType(Long.class, ToStringSerializer.instance);
            // 将基本数据类型 long 也统一转换为 String 返回给前端
            builder.serializerByType(Long.TYPE, ToStringSerializer.instance);
        };
    }
}