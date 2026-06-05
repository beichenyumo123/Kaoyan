package com.zzu.kaoyan.module.schoolselect.entity.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("recommendation_history")
public class RecommendationHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String inputJson;
    private String resultJson;

    private LocalDateTime createdAt;
}
