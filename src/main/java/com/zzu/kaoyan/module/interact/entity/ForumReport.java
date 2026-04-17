package com.zzu.kaoyan.module.interact.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_report")
public class ForumReport {
    
    // 使用雪花算法生成全局唯一ID
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long reporterId;
    
    private String targetType;
    
    private Long targetId;
    
    private String reason;
    
    // 0-待处理, 1-已处理(封禁/删除), 2-已驳回(正常)
    private Integer status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}