package com.zzu.kaoyan.module.post.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.post.dto.BoardDTO;
import com.zzu.kaoyan.module.post.entity.Board;
import com.zzu.kaoyan.module.post.service.BoardService;
import com.zzu.kaoyan.module.post.vo.BoardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "论坛板块")
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 3.1 获取板块列表（修改data结构，只返回文档要求的字段）
    @Operation(summary = "获取板块列表")
    @GetMapping
    public Result<List<BoardVO>> list() {
        LambdaQueryWrapper<Board> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Board::getIsDeleted, 0);
        wrapper.orderByAsc(Board::getId);
        List<Board> list = boardService.list(wrapper);

        // 核心：把Board实体转成BoardVO，过滤掉所有多余字段
        List<BoardVO> voList = list.stream()
                .map(board -> {
                    BoardVO vo = new BoardVO();
                    BeanUtils.copyProperties(board, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return Result.success(voList);
    }

    // 新增板块（保持原有逻辑不变）
    @Operation(summary = "新增板块")
    @PostMapping
    public Result<Long> add(@RequestBody BoardDTO dto) {
        Board board = new Board();
        board.setName(dto.getName());
        board.setDescription(dto.getDescription());
        board.setCoverUrl(dto.getCoverUrl());
        board.setPostCount(0L);
        board.setIsDeleted(0);

        boolean success = boardService.save(board);
        return success ? Result.success(board.getId())
                : Result.error(ResultCode.SYSTEM_ERROR.getCode(), "新增板块失败");
    }

    // 根据ID查询板块详情（同步修改data结构，过滤多余字段）
    @Operation(summary = "根据ID查询板块详情")
    @GetMapping("/{id}")
    public Result<BoardVO> detail(@PathVariable Long id) {
        Board board = boardService.getById(id);
        if (board == null || board.getIsDeleted() == 1) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "板块不存在");
        }

        // 转VO返回，只保留文档要求的字段
        BoardVO vo = new BoardVO();
        BeanUtils.copyProperties(board, vo);
        return Result.success(vo);
    }
}