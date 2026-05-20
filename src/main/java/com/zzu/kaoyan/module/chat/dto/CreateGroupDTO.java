package com.zzu.kaoyan.module.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupDTO {
    @NotBlank(message = "群名称不能为空")
    @Size(max = 50, message = "群名称最多50字")
    private String name;

    @Size(max = 255, message = "群简介最多255字")
    private String description;
}
