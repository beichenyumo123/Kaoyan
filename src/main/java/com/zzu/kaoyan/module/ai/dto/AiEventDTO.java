package com.zzu.kaoyan.module.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

/**
 * 行为事件上报 DTO
 */
@Data
public class AiEventDTO {

    /**
     * 事件类型：VIEW_POST / COLLECT_POST / SEARCH / LIKE_POST
     */
    @NotBlank(message = "eventType 不能为空")
    private String eventType;

    /**
     * 事件数据
     * VIEW_POST: {postId, boardId, duration}
     * COLLECT_POST: {postId, boardId}
     * SEARCH: {keyword, resultCount}
     * LIKE_POST: {postId}
     */
    private Map<String, Object> eventData;
}
