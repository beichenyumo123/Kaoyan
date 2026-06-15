package com.zzu.kaoyan.module.mistake.controller;

import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.module.mistake.entity.vo.KnowledgePointVO;
import com.zzu.kaoyan.module.mistake.service.KnowledgePointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mistake/knowledge-points")
@RequiredArgsConstructor
@Tag(name = "知识体系", description = "考研知识点树形结构，支持按科目浏览和关键词搜索")
public class KnowledgePointController {

    private final KnowledgePointService knowledgePointService;

    @Operation(summary = "获取知识树", description = "返回指定科目的完整知识点树。不传 subject 则返回全部科目。")
    @GetMapping("/tree")
    public Result<List<KnowledgePointVO>> getTree(
            @Parameter(description = "科目名称", example = "408计算机")
            @RequestParam(required = false) String subject) {
        return Result.success(knowledgePointService.getTree(subject));
    }

    @Operation(summary = "搜索知识点", description = "按关键词模糊搜索知识点，返回扁平列表")
    @GetMapping("/search")
    public Result<List<KnowledgePointVO>> search(
            @Parameter(description = "搜索关键词", required = true, example = "进程")
            @RequestParam String keyword) {
        return Result.success(knowledgePointService.search(keyword));
    }
}
