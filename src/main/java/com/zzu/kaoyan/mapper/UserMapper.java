package com.zzu.kaoyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.common.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 已经提供了 insert、selectById、updateById、deleteById 等基本方法
    // 不需要写任何代码！
}