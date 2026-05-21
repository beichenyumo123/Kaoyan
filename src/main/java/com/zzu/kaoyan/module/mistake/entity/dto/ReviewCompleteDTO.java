package com.zzu.kaoyan.module.mistake.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "完成复习请求")
public class ReviewCompleteDTO {

    @NotNull(message = "掌握程度不能为空")
    @Schema(description = "复习后的掌握程度 0-100，答对建议+15，答错建议-10", example = "60", minimum = "0", maximum = "100")
    private Integer masteryAfter;

    @NotNull(message = "是否答对不能为空")
    @Schema(description = "本次是否答对：1=答对 0=答错", example = "1", allowableValues = {"0", "1"})
    private Integer isCorrect;
}
