package com.zzu.kaoyan.module.post.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzu.kaoyan.module.post.entity.Post;

public interface PostService extends IService<Post> {
    // 基础CRUD由IService自动提供，仅需自定义业务方法
}