package com.zzu.kaoyan.module.activity.service.impl;

import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.activity.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RankServiceImpl implements RankService {

    private final UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getTotalRank() {
        return userMapper.selectRankList();
        //return null;
    }
}