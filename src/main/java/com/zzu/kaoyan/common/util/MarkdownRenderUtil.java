package com.zzu.kaoyan.common.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.util.Arrays;

/**
 * Markdown → HTML 渲染工具。
 * 使用 flexmark 做 Markdown 解析（CommonMark + GFM 扩展），
 * 再用 jsoup 做 XSS 清洗。
 */
public class MarkdownRenderUtil {

    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;

    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
                TablesExtension.create(),
                StrikethroughExtension.create(),
                TaskListExtension.create()
        ));
        // 允许 HTML 标签（flexmark 会保留，后续由 jsoup 清洗）
        options.set(HtmlRenderer.SOFT_BREAK, " ");

        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }

    /**
     * 渲染 Markdown 为 HTML
     */
    public static String render(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        String rawHtml = RENDERER.render(PARSER.parse(markdown));
        return sanitize(rawHtml);
    }

    /**
     * 使用 jsoup 清洗 HTML，防止 XSS。
     * 在 Safelist.relaxed() 基础上额外放行代码/表格/数学相关标签。
     */
    static String sanitize(String html) {
        Safelist safelist = Safelist.relaxed()
                .addTags("code", "pre", "table", "thead", "tbody", "tfoot", "tr", "th", "td",
                         "caption", "colgroup", "col", "del", "ins", "kbd", "samp", "var",
                         "math", "maction", "maligngroup", "malignmark", "menclose", "merror",
                         "mfenced", "mfrac", "mglyph", "mi", "mlabeledtr", "mlongdiv", "mmultiscripts",
                         "mn", "mo", "mover", "mpadded", "mphantom", "mroot", "mrow", "ms", "mscarries",
                         "mscarry", "msgroup", "mstack", "mstyle", "msub", "msup", "msubsup", "mtable",
                         "mtd", "mtext", "mtr", "munder", "munderover", "semantics", "annotation")
                .addAttributes("th", "align")
                .addAttributes("td", "align")
                .addAttributes("code", "class")
                .addAttributes("span", "class", "style")
                .addAttributes("div", "class")
                .addProtocols("img", "src", "data", "http", "https");

        return Jsoup.clean(html, safelist);
    }
}
