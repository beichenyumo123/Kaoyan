package com.zzu.kaoyan.module.interview.service;

/**
 * AI 面试官 TTS 语音合成服务
 */
public interface InterviewTtsService {

    /**
     * 将文本合成为 MP3 音频
     *
     * @param text          待合成的文本
     * @param interviewType 面试类型（ENGLISH 用英文语音，其他用中文语音）
     * @return MP3 音频字节数组
     */
    byte[] synthesize(String text, String interviewType);
}
