package com.zzu.kaoyan.module.schoolselect.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "单档推荐院校")
public class SchoolTierVO {

    private Long schoolId;
    private String schoolName;
    private String schoolLevel;
    private String location;
    private String logoUrl;
    private Integer avgAdmissionScore;
    private Integer matchScore;
    private String matchReason;
    private List<String> relatedMajors;
    private BigDecimal admitProbability;
}
