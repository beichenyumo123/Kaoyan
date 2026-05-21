package com.zzu.kaoyan.module.mistake.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("mistake_review_log")
public class ReviewLogPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long noteId;
    private Long userId;
    private Integer reviewStage;
    private Integer masteryBefore;
    private Integer masteryAfter;
    private Integer isCorrect;
    private LocalDateTime reviewedAt;
}
