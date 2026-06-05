package com.zzu.kaoyan.module.mistake.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("mistake_notification")
public class MistakeNotificationPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;   // REVIEW_REMINDER | MASTERY_MILESTONE | STAGE_MASTERED
    private String title;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
}
