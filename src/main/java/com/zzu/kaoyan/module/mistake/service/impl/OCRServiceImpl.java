package com.zzu.kaoyan.module.mistake.service.impl;

import com.zzu.kaoyan.module.mistake.entity.vo.OCRResultVO;
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

@Slf4j
@Service
public class OCRServiceImpl implements OCRService {

    private static final String PYTHON_SCRIPT = "scripts/ocr/paddle_ocr.py";

    @Value("${app.upload.path:./uploads}")
    private String uploadPath;

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

        result.setSuggestedSubject(detectSubject(result.getText()));
        result.setSuggestedKnowledgePoints(extractKeywords(result.getText()));

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

        ProcessBuilder pb = new ProcessBuilder(
                "python", scriptPath.toString(), fullImagePath.toString()
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

    private String detectSubject(String text) {
        if (text == null || text.isEmpty()) return null;
        String key = text.substring(0, Math.min(50, text.length()));

        if (key.contains("政") || key.contains("毛") || key.contains("马") || key.contains("思想")) return "政治";
        if (key.contains("阅读") || key.contains("翻译") || key.contains("作文") || key.contains("cloze")) return "英语";
        if (key.contains("极限") || key.contains("导数") || key.contains("积分") || key.contains("矩阵")) return "数学";
        if (key.contains("OS") || key.contains("进程") || key.contains("内存") || key.contains("算法") || key.contains("数据结构")) return "408计算机";

        return null;
    }

    private String extractKeywords(String text) {
        if (text == null || text.isEmpty()) return null;

        List<String> keywords = new ArrayList<>();
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            // 提取含有"知识点"/"考点"/"章/节"的行作为关键词
            if (line.contains("知识点") || line.contains("考点") || line.contains("章") || line.contains("节")) {
                keywords.add(line.replaceAll("[#*\\-\\s+]", "").trim());
            }
        }
        return keywords.isEmpty() ? null : String.join(", ", keywords.subList(0, Math.min(5, keywords.size())));
    }
}
