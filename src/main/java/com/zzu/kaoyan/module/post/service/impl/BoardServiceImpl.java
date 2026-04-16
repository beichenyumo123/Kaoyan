package com.zzu.kaoyan.module.post.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzu.kaoyan.mapper.BoardMapper; // ✅ 修正为根目录mapper包
import com.zzu.kaoyan.module.post.entity.Board;
import com.zzu.kaoyan.module.post.service.BoardService;
import org.springframework.stereotype.Service;

@Service
public class BoardServiceImpl extends ServiceImpl<BoardMapper, Board> implements BoardService {

}