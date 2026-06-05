package com.zzu.kaoyan.module.mistake.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "PDF导出请求参数")
public class PdfExportRequestDTO {

    @NotEmpty(message = "错题ID列表不能为空")
    @Schema(description = "要导出的错题ID列表", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    private List<Long> noteIds;

    @Schema(description = "是否包含答案与解析，默认true", example = "true")
    private Boolean includeAnswer = true;

    @Schema(description = "是否内嵌原题图片，默认false（含图片会显著增大PDF体积）", example = "false")
    private Boolean includeImage = false;
}
