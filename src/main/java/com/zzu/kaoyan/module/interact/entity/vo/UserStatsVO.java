package com.zzu.kaoyan.module.interact.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户社交数据统计 VO")
public class UserStatsVO {
    @Schema(description = "用户ID")
    private Long userId;
    
    @Schema(description = "发帖总数")
    private Integer postCount;
    
    @Schema(description = "获得的点赞总数")
    private Integer likeReceivedCount;
}