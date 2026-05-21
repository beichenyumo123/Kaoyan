package com.zzu.kaoyan.config.mistake;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 科目关键词配置，用于 OCR 识别后自动推测科目。
 * 可通过 application.properties 覆盖：
 * <pre>
 * mistake.subject-keywords.政治[0]=政治
 * mistake.subject-keywords.政治[1]=毛泽东
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "mistake")
public class SubjectKeywordsConfig {

    /**
     * 科目 → 关键词列表 映射
     */
    private Map<String, List<String>> subjectKeywords = Map.of(
            "政治", List.of("政治", "毛泽东", "马克思主义", "中国特色", "社会主义",
                    "唯物", "辩证法", "矛盾", "实践", "真理", "价值", "人民群众",
                    "新民主主义", "改革开放", "剩余价值"),
            "英语(一)", List.of("reading", "translation", "cloze", "passage", "paragraph",
                    "作文", "阅读", "翻译", "完形", "写作", "长难句", "vocabulary"),
            "数学(一)", List.of("极限", "导数", "微分", "积分", "矩阵", "行列式", "概率",
                    "分布", "级数", "方程", "向量", "特征值", "二次型", "收敛",
                    "二重积分", "三重积分", "曲线积分", "曲面积分", "傅里叶"),
            "408计算机", List.of("进程", "内存", "操作系统", "数据结构", "算法",
                    "网络", "TCP", "UDP", "调度", "死锁", "Cache", "流水线",
                    "二叉树", "图", "排序", "查找", "中断", "DMA", "分页",
                    "分段", "HTTP", "DNS", "IP", "路由", "拥塞控制",
                    "CSMA", "ALU", "浮点", "指令", "总线")
    );
}
