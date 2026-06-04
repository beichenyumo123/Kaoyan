package com.zzu.kaoyan.module.schoolselect.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "相似上岸者案例")
public class SimilarUserCaseVO {

    private Long userId;
    private String username;
    private String undergradSchool;
    private BigDecimal undergradGpa;
    private String englishLevel;
    private Integer prepDuration;
    private String admittedSchool;
    private String admittedMajor;
    private Integer examScoreTotal;
    private BigDecimal similarity;
}
