package com.zzu.kaoyan.module.message.dto;

import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.module.post.entity.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "搜索结果")
public class SearchResultVO {

    @Schema(description = "匹配的帖子列表")
    private List<Post> posts;

    @Schema(description = "匹配的用户列表")
    private List<User> users;
}