package com.zzu.kaoyan.module.mistake.service;

import com.zzu.kaoyan.module.mistake.entity.dto.PdfExportRequestDTO;

public interface MistakeNotePdfService {

    /**
     * 导出错题为 PDF 文档
     * @param dto 导出参数
     * @param userId 当前用户ID
     * @return PDF 文件字节数组
     */
    byte[] exportNotes(PdfExportRequestDTO dto, Long userId);
}
