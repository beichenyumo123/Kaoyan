package com.zzu.kaoyan.module.experience.vo;

import com.zzu.kaoyan.module.experience.entity.ExperiencePost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "经验贴详情响应")
public class ExperiencePostVO {

    @Schema(description = "经验贴ID")
    private Long id;

    @Schema(description = "作者信息")
    private AuthorVO author;

    @Schema(description = "关联论坛帖子ID")
    private Long forumPostId;

    @Schema(description = "本科院校")
    private String undergradSchool;

    @Schema(description = "本科专业")
    private String undergradMajor;

    @Schema(description = "是否跨考")
    private Boolean isCrossMajor;

    @Schema(description = "是否二战")
    private Boolean isSecondAttempt;

    @Schema(description = "目标院校")
    private String targetSchool;

    @Schema(description = "目标专业")
    private String targetMajor;

    @Schema(description = "初试总分")
    private BigDecimal initialExamTotal;

    @Schema(description = "政治")
    private BigDecimal initialExamPolitics;

    @Schema(description = "英语")
    private BigDecimal initialExamEnglish;

    @Schema(description = "数学")
    private BigDecimal initialExamMath;

    @Schema(description = "专业课")
    private BigDecimal initialExamMajor;

    @Schema(description = "复试分")
    private BigDecimal reExamScore;

    @Schema(description = "备考时间线")
    private List<ExperiencePost.TimelineItem> timelineJson;

    @Schema(description = "用书推荐")
    private List<ExperiencePost.BookItem> booksJson;

    @Schema(description = "备考心得")
    private String tips;

    @Schema(description = "作者是否已认证（快照）")
    private Boolean isVerified;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "当前用户是否点赞")
    private Boolean isLiked;

    @Schema(description = "当前用户是否收藏")
    private Boolean isCollected;

    @Schema(description = "发布时间")
    private LocalDateTime createdAt;

    @Data
    @Schema(description = "作者简况")
    public static class AuthorVO {
        private Long userId;
        private String username;
        private String avatarUrl;
        private Boolean isVerified;
        private String verifiedSchool;
        private String verifiedMajor;
    }
}