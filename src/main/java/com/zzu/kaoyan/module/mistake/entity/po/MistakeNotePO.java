package com.zzu.kaoyan.module.mistake.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("mistake_note")
public class MistakeNotePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String subject;
    private String questionContent;
    private String answer;
    private String imageUrl;
    private String knowledgePoints;
    private String source;
    private Integer difficulty;
    private Integer masteryLevel;
    private Integer reviewStage;
    private Integer reviewCount;
    private LocalDate nextReviewDate;
    private LocalDate lastReviewDate;
    private Long chatMessageId;
    private String sourceType;
    private Integer isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
