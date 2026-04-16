package com.zzu.kaoyan.module.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("forum_board")
@Schema(description = "板块信息")
public class Board extends Model<Board> {

    @TableId(type = IdType.AUTO)
    @Schema(description = "板块ID")
    private Long id;

    @Schema(description = "板块名称")
    private String name;

    @Schema(description = "板块描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverUrl;

    @Schema(description = "帖子数量")
    private Integer postCount;

    @Schema(description = "是否删除 0-否 1-是")
    private Integer isDeleted;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}