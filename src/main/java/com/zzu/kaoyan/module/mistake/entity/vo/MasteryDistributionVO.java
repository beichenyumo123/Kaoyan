package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "掌握度区间分布")
public class MasteryDistributionVO {

    @Schema(description = "掌握度区间标签", example = "0-20")
    private String range;

    @Schema(description = "该区间的题目数量")
    private Integer count;
}
