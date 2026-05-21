package com.zzu.kaoyan.module.experience.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "experience_post", autoResultMap = true)
public class ExperiencePost {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long forumPostId;

    private String undergradSchool;
    private String undergradMajor;
    private Boolean isCrossMajor;
    private Boolean isSecondAttempt;

    private String targetSchool;
    private String targetMajor;

    private java.math.BigDecimal initialExamTotal;
    private java.math.BigDecimal initialExamPolitics;
    private java.math.BigDecimal initialExamEnglish;
    private java.math.BigDecimal initialExamMath;
    private java.math.BigDecimal initialExamMajor;
    private java.math.BigDecimal reExamScore;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<TimelineItem> timelineJson;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<BookItem> booksJson;

    private String tips;

    private Boolean isVerified;
    private Integer status;

    @TableField(value = "view_count")
    private Integer viewCount;

    @TableField(value = "like_count")
    private Integer likeCount;

    @TableField(value = "collect_count")
    private Integer collectCount;

    @TableField("is_deleted")
    @TableLogic
    private Boolean deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Data
    public static class TimelineItem {
        private String phase;
        private String startDate;
        private String endDate;
        private String description;
    }

    @Data
    public static class BookItem {
        private String subject;
        private String name;
        private Integer rating;
    }
}