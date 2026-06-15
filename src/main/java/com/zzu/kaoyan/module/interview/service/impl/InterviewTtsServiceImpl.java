package com.zzu.kaoyan.module.interview.service.impl;

import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesisParam;
import com.alibaba.dashscope.audio.ttsv2.SpeechSynthesizer;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.interview.config.QwenConfig;
import com.zzu.kaoyan.module.interview.service.InterviewTtsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * DashScope CosyVoice TTS 语音合成（Java SDK）
 * <p>
 * 通过 DashScope Java SDK 调用 CosyVoice TTS，底层使用 WebSocket 协议。
 * 中文：cosyvoice-v1 + longxiaochun（龙小春）
 * 英文：cosyvoice-v1 + loongstella
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewTtsServiceImpl implements InterviewTtsService {

    private final QwenConfig qwenConfig;

    /** 中文音色（女性，自然亲切） */
    private static final String VOICE_CN = "longxiaochun";
    /** 英文音色（女性） */
    private static final String VOICE_EN = "loongstella";

    @Override
    public byte[] synthesize(String text, String interviewType) {
        if (text == null || text.isBlank()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "合成文本不能为空");
        }

        boolean isEnglish = "ENGLISH".equalsIgnoreCase(interviewType);
        String voice = isEnglish ? VOICE_EN : VOICE_CN;

        SpeechSynthesisParam param = SpeechSynthesisParam.builder()
                .apiKey(qwenConfig.apiKey)
                .model(QwenConfig.TTS_MODEL)
                .voice(voice)
                .build();

        SpeechSynthesizer synthesizer = new SpeechSynthesizer(param, null);

        try {
            ByteBuffer audio = synthesizer.call(text);
            if (audio == null) {
                throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "TTS 返回空音频");
            }

            byte[] bytes = new byte[audio.remaining()];
            audio.get(bytes);
            log.info("TTS 合成成功，文本长度={}, 音频={} bytes, voice={}", text.length(), bytes.length, voice);
            return bytes;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("TTS 合成失败", e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "TTS 语音合成失败: " + e.getMessage());
        } finally {
            synthesizer.getDuplexApi().close(1000, "done");
        }
    }
}
