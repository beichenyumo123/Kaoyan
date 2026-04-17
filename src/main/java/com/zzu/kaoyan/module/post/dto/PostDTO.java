package com.zzu.kaoyan.module.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class PostDTO {
    @NotNull(message = "板块ID不能为空")
    private Long boardId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题最多100字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private List<String> tags; // 选填标签
}