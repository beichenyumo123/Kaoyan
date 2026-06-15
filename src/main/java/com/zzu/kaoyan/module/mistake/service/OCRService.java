package com.zzu.kaoyan.module.mistake.service;

import com.zzu.kaoyan.module.mistake.entity.vo.OCRResultVO;

public interface OCRService {

    /**
     * 对上传的图片进行OCR识别
     * @param imagePath 服务器上的图片路径
     * @param subject 科目提示（可选，帮助OCR更精准）
     * @return OCR识别结果
     */
    OCRResultVO recognize(String imagePath, String subject);
}
