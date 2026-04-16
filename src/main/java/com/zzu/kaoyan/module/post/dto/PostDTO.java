package com.zzu.kaoyan.module.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "发布帖子请求")
public class PostDTO {

    @NotNull(message = "板块ID不能为空")
    @Schema(description = "板块ID")
    private Long boardId;

    @NotBlank(message = "标题不能为空")
    @Schema(description = "标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(description = "内容")
    private String content;

    @Schema(description = "附件JSON")
    private String attachmentUrls;
}