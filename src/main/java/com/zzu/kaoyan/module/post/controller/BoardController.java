package com.zzu.kaoyan.module.post.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.common.result.Result;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.module.post.dto.BoardDTO;
import com.zzu.kaoyan.module.post.entity.Board;
import com.zzu.kaoyan.module.post.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "板块接口")
@RestController
@RequestMapping("/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "获取所有板块")
    @GetMapping
    public Result<List<Board>> list() {
        LambdaQueryWrapper<Board> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Board::getIsDeleted, 0);
        wrapper.orderByAsc(Board::getId);
        List<Board> list = boardService.list(wrapper);
        return Result.success(list);
    }

    @Operation(summary = "新增板块")
    @PostMapping
    public Result<Long> add(@RequestBody BoardDTO dto) {
        Board board = new Board();
        board.setName(dto.getName());
        board.setDescription(dto.getDescription());
        board.setCoverUrl(dto.getCoverUrl());
        board.setPostCount(0);
        board.setIsDeleted(0);

        boolean success = boardService.save(board);
        return success ? Result.success(board.getId())
                : Result.error(ResultCode.SYSTEM_ERROR.getCode(), "新增板块失败");
    }

    @Operation(summary = "根据ID查询板块详情")
    @GetMapping("/{id}")
    public Result<Board> detail(@PathVariable Long id) {
        Board board = boardService.getById(id);
        if (board == null || board.getIsDeleted() == 1) {
            return Result.error(ResultCode.NOT_FOUND.getCode(), "板块不存在");
        }
        return Result.success(board);
    }
}