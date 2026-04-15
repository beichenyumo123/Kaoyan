package com.zzu.kaoyan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zzu.kaoyan.common.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AuthMapper extends BaseMapper<User> {

}
