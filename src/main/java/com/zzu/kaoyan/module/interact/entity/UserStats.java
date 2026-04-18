package com.zzu.kaoyan.module.interact.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_user_stats")
public class UserStats {
    @TableId
    private Long userId;            // 关联 sys_user 表的主键 ID
    private Integer postCount;       // 发帖总数
    private Integer likeReceivedCount; // 获得的获赞总数
    private LocalDateTime updatedAt; // 最后更新时间
}