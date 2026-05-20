package com.zzu.kaoyan.common.handler;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.zzu.kaoyan.common.annotation.SkipXssClean;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;

/**
 * 全局 String 反序列化器：对所有 JSON String 字段执行 Jsoup 清洗，
 * 移除 XSS 攻击载荷。标注 {@link SkipXssClean} 的字段跳过清洗。
 */
public class XssStringDeserializer extends StdDeserializer<String> implements ContextualDeserializer {

    private static final Safelist SAFELIST = Safelist.relaxed();

    private final boolean skipClean;

    public XssStringDeserializer() {
        super(String.class);
        this.skipClean = false;
    }

    private XssStringDeserializer(boolean skipClean) {
        super(String.class);
        this.skipClean = skipClean;
    }

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value != null && !skipClean) {
            return Jsoup.clean(value, SAFELIST);
        }
        return value;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        if (property != null && property.getAnnotation(SkipXssClean.class) != null) {
            return new XssStringDeserializer(true);
        }
        return this;
    }
}
