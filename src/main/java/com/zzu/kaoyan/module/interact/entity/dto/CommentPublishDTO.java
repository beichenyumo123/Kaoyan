package com.zzu.kaoyan.module.interact.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发布评论/回复请求参数")
public class CommentPublishDTO {

    @Schema(description = "评论内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "楼主说得对！")
    private String content;

    @Schema(description = "回复的评论ID，顶层评论传null", example = "128")
    private Long replyToId;
}