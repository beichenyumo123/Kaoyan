package com.zzu.kaoyan.module.interact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post_like")
public class ForumPostLike {

    @TableId(type = IdType.ASSIGN_ID)  // 强制雪花算法生成ID
    private Long id;

    private Long postId;

    private Long userId;

    private LocalDateTime createdAt;
}