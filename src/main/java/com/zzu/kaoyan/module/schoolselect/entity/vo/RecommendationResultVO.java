package com.zzu.kaoyan.module.schoolselect.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "智能择校推荐结果")
public class RecommendationResultVO {

    @Schema(description = "保底院校")
    private List<SchoolTierVO> safety;

    @Schema(description = "合适院校")
    private List<SchoolTierVO> match;

    @Schema(description = "冲刺院校")
    private List<SchoolTierVO> reach;

    @Schema(description = "与你相似的上岸者案例")
    private List<SimilarUserCaseVO> similarUsers;
}
