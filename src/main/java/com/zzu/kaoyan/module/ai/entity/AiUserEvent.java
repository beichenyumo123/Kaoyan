package com.zzu.kaoyan.module.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为事件实体（Agent 感知用户行为）
 */
@Data
@TableName("ai_user_event")
public class AiUserEvent {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /**
     * 事件类型
     * VIEW_POST / COLLECT_POST / SEARCH / LIKE_POST
     */
    private String eventType;

    /** 事件数据 JSON：{"postId":123,"boardId":2,"keyword":"B树","duration":120} */
    private String eventData;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
