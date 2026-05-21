package com.zzu.kaoyan.module.mistake.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.module.mistake.entity.dto.PdfExportRequestDTO;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotePO;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNoteMapper;
import com.zzu.kaoyan.module.mistake.service.EbbinghausService;
import com.zzu.kaoyan.module.mistake.service.MistakeNotePdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class MistakeNotePdfServiceImpl implements MistakeNotePdfService {

    private final MistakeNoteMapper mistakeNoteMapper;
    private final EbbinghausService ebbinghausService;

    /**
     * CJK 字体文件路径，支持配置
     */
    @Value("${mistake.pdf.font-path:}")
    private String fontPathConfig;

    public MistakeNotePdfServiceImpl(MistakeNoteMapper mistakeNoteMapper,
                                     EbbinghausService ebbinghausService) {
        this.mistakeNoteMapper = mistakeNoteMapper;
        this.ebbinghausService = ebbinghausService;
    }

    @Override
    public byte[] exportNotes(PdfExportRequestDTO dto, Long userId) {
        List<MistakeNotePO> notes = mistakeNoteMapper.selectBatchIds(dto.getNoteIds());
        if (notes.isEmpty()) {
            throw new BusinessException(400, "未找到可导出的错题");
        }
        // 校验所有权
        for (MistakeNotePO note : notes) {
            if (!note.getUserId().equals(userId)) {
                throw new BusinessException(403, "无权导出他人错题");
            }
            if (note.getIsDeleted() != null && note.getIsDeleted() == 1) {
                throw new BusinessException(400, "错题 " + note.getId() + " 已被删除");
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            BaseFont bf = loadCjkFont();

            // 标题
            Font titleFont = new Font(bf, 18, Font.BOLD);
            Paragraph title = new Paragraph("考研错题本 - 复习导出", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(8);
            document.add(title);

            // 导出日期
            Font metaFont = new Font(bf, 9, Font.NORMAL);
            Paragraph meta = new Paragraph("导出日期: " + LocalDate.now().format(DateTimeFormatter.ISO_DATE)
                    + "  |  共 " + notes.size() + " 题", metaFont);
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(16);
            document.add(meta);

            // 逐题输出
            Font sectionFont = new Font(bf, 12, Font.BOLD);
            Font bodyFont = new Font(bf, 10, Font.NORMAL);
            Font smallFont = new Font(bf, 8, Font.NORMAL);

            for (int i = 0; i < notes.size(); i++) {
                MistakeNotePO note = notes.get(i);

                // 题号 + 科目
                Paragraph heading = new Paragraph(
                        (i + 1) + ". [" + (note.getSubject() != null ? note.getSubject() : "未分类") + "] "
                                + (note.getKnowledgePoints() != null ? note.getKnowledgePoints() : ""),
                        sectionFont);
                heading.setSpacingBefore(12);
                heading.setSpacingAfter(8);
                document.add(heading);

                // 元信息表
                PdfPTable infoTable = new PdfPTable(4);
                infoTable.setWidthPercentage(100);
                infoTable.setSpacingAfter(8);
                float[] colWidths = {1.5f, 2f, 1.5f, 2f};
                infoTable.setWidths(colWidths);

                addCell(infoTable, "难度: " + stars(note.getDifficulty()), smallFont);
                addCell(infoTable, "掌握度: " + note.getMasteryLevel() + "%", smallFont);
                addCell(infoTable, "复习次数: " + note.getReviewCount(), smallFont);
                addCell(infoTable, "阶段: " + ebbinghausService.getStageText(note.getReviewStage()), smallFont);

                document.add(infoTable);

                // 题目内容
                if (note.getQuestionContent() != null && !note.getQuestionContent().isBlank()) {
                    Paragraph qLabel = new Paragraph("【题目】", bodyFont);
                    qLabel.setSpacingAfter(4);
                    document.add(qLabel);
                    Paragraph qContent = new Paragraph(truncate(note.getQuestionContent(), 2000), bodyFont);
                    qContent.setIndentationLeft(20);
                    qContent.setSpacingAfter(8);
                    document.add(qContent);
                }

                // 答案与解析
                if (Boolean.TRUE.equals(dto.getIncludeAnswer())
                        && note.getAnswer() != null && !note.getAnswer().isBlank()) {
                    Paragraph aLabel = new Paragraph("【答案与解析】", bodyFont);
                    aLabel.setSpacingAfter(4);
                    document.add(aLabel);
                    Paragraph aContent = new Paragraph(truncate(note.getAnswer(), 2000), bodyFont);
                    aContent.setIndentationLeft(20);
                    aContent.setSpacingAfter(8);
                    document.add(aContent);
                }

                // 分隔线
                if (i < notes.size() - 1) {
                    Paragraph sep = new Paragraph("—".repeat(60), smallFont);
                    sep.setAlignment(Element.ALIGN_CENTER);
                    sep.setSpacingAfter(4);
                    document.add(sep);
                }
            }

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("PDF 导出失败", e);
            throw new BusinessException(500, "PDF 导出失败: " + e.getMessage());
        }
    }

    private BaseFont loadCjkFont() throws Exception {
        // 1. 优先使用配置指定的字体路径
        if (fontPathConfig != null && !fontPathConfig.isBlank()) {
            Path p = Path.of(fontPathConfig);
            if (Files.exists(p)) {
                return BaseFont.createFont(p.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
            log.warn("配置的字体文件不存在: {}", fontPathConfig);
        }

        // 2. 尝试 Windows 常见 CJK 字体
        String[] winFonts = {
                "C:/Windows/Fonts/msyh.ttc,0",   // 微软雅黑
                "C:/Windows/Fonts/simsun.ttc,0",  // 宋体
                "C:/Windows/Fonts/simhei.ttf",    // 黑体
                "C:/Windows/Fonts/msjh.ttc,0",    // 微软正黑体
        };
        for (String fontPath : winFonts) {
            try {
                return BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {
                // try next
            }
        }

        // 3. 尝试 Linux 常见 CJK 字体
        String[] linuxFonts = {
                "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0",
                "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0",
                "/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf",
        };
        for (String fontPath : linuxFonts) {
            try {
                return BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (Exception ignored) {
                // try next
            }
        }

        // 4. 最后尝试放在 classpath 下的字体
        try {
            return BaseFont.createFont("fonts/NotoSansSC-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception ignored) {
            // fallback
        }

        throw new BusinessException(500,
                "未找到中文字体文件，请将 .ttf/.ttc 字体放到 src/main/resources/fonts/ 目录，"
                        + "或在 application.properties 中设置 mistake.pdf.font-path=/path/to/font.ttf");
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorderWidth(0);
        cell.setPadding(2);
        table.addCell(cell);
    }

    private String stars(Integer difficulty) {
        if (difficulty == null) return "★★★";
        return "★".repeat(Math.max(1, Math.min(5, difficulty)))
                + "☆".repeat(Math.max(0, 5 - Math.max(1, Math.min(5, difficulty))));
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen) + "…" : text;
    }
}
