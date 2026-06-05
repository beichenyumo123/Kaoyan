package com.zzu.kaoyan.module.mistake.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "知识点节点（递归树结构）")
public class KnowledgePointVO {

    @Schema(description = "知识点ID")
    private Long id;

    @Schema(description = "父节点ID，null=根节点")
    private Long parentId;

    @Schema(description = "知识点名称")
    private String name;

    @Schema(description = "所属科目")
    private String subject;

    @Schema(description = "层级深度 0=根科目 1=章 2=节 3=具体知识点")
    private Integer level;

    @Schema(description = "排序序号")
    private Integer sortOrder;

    @Schema(description = "子节点列表")
    private List<KnowledgePointVO> children = new ArrayList<>();
}
