package com.zzu.kaoyan.module.post.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.mapper.PostMapper;
import com.zzu.kaoyan.module.post.service.PostService;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {
    // 基础CRUD由ServiceImpl自动提供，仅需实现自定义业务方法
}