package com.zzu.kaoyan.module.mistake.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建错题请求")
public class MistakeNoteCreateDTO {

    @NotBlank(message = "科目不能为空")
    @Schema(description = "科目", example = "408计算机", allowableValues = {
            "政治", "英语(一)", "英语(二)", "数学(一)", "数学(二)", "数学(三)", "408计算机", "其他"
    })
    private String subject;

    @NotBlank(message = "题目内容不能为空")
    @Schema(description = "题目内容（OCR识别的文本或手动输入）", example = "在操作系统中，进程调度算法中，哪种算法可能产生饥饿现象？")
    private String questionContent;

    @Schema(description = "答案与解析", example = "短作业优先(SJF)算法可能导致长作业长期得不到调度，产生饥饿现象。")
    private String answer;

    @Schema(description = "题目原图URL（先调 /api/upload/image 上传获取）", example = "/uploads/images/202605/abc123.jpg")
    private String imageUrl;

    @Schema(description = "知识点标签，多个用逗号分隔", example = "操作系统-进程调度,数据结构-二叉树")
    private String knowledgePoints;

    @Schema(description = "题目来源", example = "2023真题")
    private String source;

    @Schema(description = "难度 1-5", example = "3", minimum = "1", maximum = "5")
    private Integer difficulty;
}
