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

    /**
     * 自定义 Safelist，在 {@code Safelist.relaxed()} 基础上不限制 img[src] 的协议，
     * 以允许相对路径（如 /uploads/images/xxx.png）。
     * 危险协议（javascript: 等）在反序列化阶段额外处理。
     */
    private static final Safelist SAFELIST = buildSafelist();

    private static Safelist buildSafelist() {
        // 手动构建，标签和属性等同 Safelist.relaxed()，
        // 但 img[src] 不调 addProtocols（允许所有协议，包括相对 URL）
        return new Safelist()
                .addTags("a", "b", "blockquote", "br", "caption", "cite", "code", "col",
                        "colgroup", "dd", "div", "dl", "dt", "em", "h1", "h2", "h3", "h4",
                        "h5", "h6", "i", "img", "li", "ol", "p", "pre", "q", "small",
                        "span", "strike", "strong", "sub", "sup", "table", "tbody", "td",
                        "tfoot", "th", "thead", "tr", "u", "ul")
                .addAttributes(":all", "class")
                .addAttributes("a", "href", "title")
                .addAttributes("img", "alt", "title", "width", "height", "src")
                .addAttributes("blockquote", "cite")
                .addAttributes("col", "span", "width")
                .addAttributes("colgroup", "span", "width")
                .addAttributes("ol", "start", "type")
                .addAttributes("q", "cite")
                .addAttributes("table", "summary", "width")
                .addAttributes("td", "abbr", "axis", "colspan", "rowspan", "width")
                .addAttributes("th", "abbr", "axis", "colspan", "rowspan", "scope", "width")
                .addProtocols("a", "href", "ftp", "http", "https", "mailto");
    }

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
            // 显式移除 javascript: 协议，弥补 safelist 中 img[src] 无协议限制
            value = value.replaceAll("(?i)javascript\\s*:", "blocked:");
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
