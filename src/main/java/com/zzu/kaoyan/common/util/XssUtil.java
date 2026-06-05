package com.zzu.kaoyan.common.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

/**
 * XSS 防护工具类
 * 使用 Jsoup 对 HTML 内容进行白名单过滤，防止存储型 XSS 攻击
 */
public class XssUtil {

    /**
     * 富文本白名单 - 允许的 HTML 标签和属性
     */
    private static final Safelist RICH_TEXT_WHITELIST = Safelist.relaxed()
            // 允许的标签已在 relaxed() 中包含，可按需添加
            .addAttributes(":all", "style")  // 允许所有标签的 style 属性
            .addProtocols("img", "src", "http", "https");  // 允许图片协议

    /**
     * 纯文本白名单 - 移除所有 HTML 标签
     */
    private static final Safelist PLAIN_TEXT_WHITELIST = Safelist.none();

    /**
     * 过滤富文本内容（保留部分格式）
     * @param htmlContent 原始 HTML 内容
     * @return 过滤后的安全 HTML 内容
     */
    public static String filterRichText(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }
        return Jsoup.clean(htmlContent, RICH_TEXT_WHITELIST);
    }

    /**
     * 过滤纯文本内容（移除所有 HTML 标签）
     * @param content 原始内容
     * @return 纯文本内容
     */
    public static String filterPlainText(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        return Jsoup.clean(content, PLAIN_TEXT_WHITELIST).trim();
    }

    /**
     * 检查内容是否包含潜在危险标签
     * @param content 内容
     * @return 是否包含危险标签
     */
    public static boolean containsDangerousTags(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        String lowerContent = content.toLowerCase();
        return lowerContent.contains("<script") ||
               lowerContent.contains("javascript:") ||
               lowerContent.contains("onerror=") ||
               lowerContent.contains("onload=") ||
               lowerContent.contains("onclick=");
    }
}
