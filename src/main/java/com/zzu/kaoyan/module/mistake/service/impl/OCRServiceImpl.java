package com.zzu.kaoyan.module.mistake.service.impl;

import com.zzu.kaoyan.config.mistake.SubjectKeywordsConfig;
import com.zzu.kaoyan.module.mistake.entity.vo.OCRResultVO;
import com.zzu.kaoyan.module.mistake.service.KnowledgePointService;
import com.zzu.kaoyan.module.mistake.service.OCRService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OCRServiceImpl implements OCRService {

    private static final String PYTHON_SCRIPT = "scripts/ocr/paddle_ocr.py";

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

    private final SubjectKeywordsConfig subjectKeywordsConfig;
    private final KnowledgePointService knowledgePointService;

    public OCRServiceImpl(SubjectKeywordsConfig subjectKeywordsConfig,
                          KnowledgePointService knowledgePointService) {
        this.subjectKeywordsConfig = subjectKeywordsConfig;
        this.knowledgePointService = knowledgePointService;
    }

    @Override
    public OCRResultVO recognize(String imagePath, String subject) {
        OCRResultVO result = new OCRResultVO();
        result.setImageUrl(imagePath);

        try {
            // 尝试调用 PaddleOCR Python 脚本
            String text = callPaddleOCR(imagePath);
            result.setText(text);
        } catch (Exception e) {
            log.warn("OCR 识别失败，返回空结果: {}", e.getMessage());
            result.setText("");
        }

        String text = result.getText();
        result.setSuggestedSubject(detectSubject(text));
        result.setSuggestedKnowledgePoints(extractKeywords(text));
        result.setSuggestedDifficulty(suggestDifficulty(text));

        // 匹配知识树中的知识点
        List<Long> matchedIds = knowledgePointService.matchFromText(text);
        result.setMatchedKnowledgePointIds(matchedIds);
        // 用 ID 列表占位，前端可根据 ID 去查名称
        result.setMatchedKnowledgePointNames(
                matchedIds.stream().map(String::valueOf).collect(Collectors.toList()));

        return result;
    }

    private String callPaddleOCR(String imagePath) throws Exception {
        Path scriptPath = Path.of(PYTHON_SCRIPT);
        if (!Files.exists(scriptPath)) {
            throw new RuntimeException("OCR 脚本不存在: " + PYTHON_SCRIPT);
        }

        Path fullImagePath = Path.of(uploadPath).resolve(
                imagePath.startsWith("/uploads/") ? imagePath.substring(9) : imagePath
        );

        if (!Files.exists(fullImagePath)) {
            throw new RuntimeException("图片文件不存在: " + fullImagePath);
        }

        // 优先使用虚拟环境 Python，回退到系统 python
        String pythonCmd = System.getenv().getOrDefault("OCR_PYTHON_CMD", "python");
        ProcessBuilder pb = new ProcessBuilder(
                pythonCmd, scriptPath.toString(), fullImagePath.toString()
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("OCR 执行失败，退出码: " + exitCode);
        }

        return output.toString().trim();
    }

    /**
     * 基于关键词命中率计分推测科目。遍历所有 OCR 文本，统计每个科目的关键词命中数，
     * 返回得分最高的科目。
     */
    private String detectSubject(String text) {
        if (text == null || text.isEmpty()) return null;

        int bestScore = 0;
        String bestSubject = null;
        for (var entry : subjectKeywordsConfig.getSubjectKeywords().entrySet()) {
            int score = 0;
            for (String keyword : entry.getValue()) {
                if (text.contains(keyword)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestSubject = entry.getKey();
            }
        }
        // 至少命中 1 个关键词才返回
        return bestScore > 0 ? bestSubject : null;
    }

    /**
     * 从 OCR 文本提取知识点：
     * 1. 优先从知识树数据库中精确匹配（查 knowledge_point 表）
     * 2. 匹配失败时回退到旧逻辑（提取含有"知识点"/"考点"的行）
     */
    private String extractKeywords(String text) {
        if (text == null || text.isEmpty()) return null;

        List<String> keywords = new ArrayList<>();

        // 方式1：数据库匹配（查知识树中的叶子节点）
        List<String> dbMatches = knowledgePointService.matchFromText(text).stream()
                .limit(5)
                .map(id -> {
                    // 通过 ID 查找名称；简化处理：直接用 DB 中的 name
                    return id.toString();
                })
                .collect(Collectors.toList());

        // 方式2：回退到旧逻辑
        if (dbMatches.isEmpty()) {
            String[] lines = text.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.contains("知识点") || line.contains("考点") || line.contains("章") || line.contains("节")) {
                    keywords.add(line.replaceAll("[#*\\-\\s+]", "").trim());
                }
            }
            return keywords.isEmpty() ? null : String.join(", ", keywords.subList(0, Math.min(5, keywords.size())));
        }

        return String.join(", ", dbMatches);
    }

    /**
     * 基于文本特征推测难度：
     * - 文本越长、含公式标记越多 → 难度越高
     * - 返回 1-5 整数
     */
    private int suggestDifficulty(String text) {
        if (text == null || text.isEmpty()) return 3;
        int length = text.length();
        // 统计数学符号密度（积分、求和、导数等）
        long mathSymbols = text.chars().filter(c -> "∫∑∂√∞→⇒∀∃∈∪∩⊆⊂≤≥≠≈".indexOf(c) >= 0).count();
        double density = (double) mathSymbols / Math.max(length, 1);

        if (length < 50 && density < 0.02) return 1;
        if (length < 100 && density < 0.05) return 2;
        if (length < 200 && density < 0.08) return 3;
        if (length < 400 || density < 0.12) return 4;
        return 5;
    }
}
