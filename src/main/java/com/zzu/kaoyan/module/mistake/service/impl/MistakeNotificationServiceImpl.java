package com.zzu.kaoyan.module.mistake.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.module.mistake.entity.po.MistakeNotificationPO;
import com.zzu.kaoyan.module.mistake.entity.vo.MistakeNotificationVO;
import com.zzu.kaoyan.module.mistake.mapper.MistakeNotificationMapper;
import com.zzu.kaoyan.module.mistake.service.MistakeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MistakeNotificationServiceImpl implements MistakeNotificationService {

    private final MistakeNotificationMapper notificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Long userId, String type, String title, String content) {
        MistakeNotificationPO po = new MistakeNotificationPO();
        po.setUserId(userId);
        po.setType(type);
        po.setTitle(title);
        po.setContent(content);
        po.setIsRead(0);
        notificationMapper.insert(po);
        log.debug("通知已创建: userId={}, type={}, title={}", userId, type, title);
    }

    @Override
    public PageInfo<MistakeNotificationVO> list(Long userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<MistakeNotificationPO> pos = notificationMapper.selectList(
                new LambdaQueryWrapper<MistakeNotificationPO>()
                        .eq(MistakeNotificationPO::getUserId, userId)
                        .orderByDesc(MistakeNotificationPO::getCreatedAt)
        );
        List<MistakeNotificationVO> vos = pos.stream().map(this::toVO).collect(Collectors.toList());
        PageInfo<MistakeNotificationPO> poPage = new PageInfo<>(pos);
        PageInfo<MistakeNotificationVO> result = new PageInfo<>(vos);
        result.setTotal(poPage.getTotal());
        result.setPageNum(poPage.getPageNum());
        result.setPageSize(poPage.getPageSize());
        return result;
    }

    @Override
    public int unreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long notificationId, Long userId) {
        MistakeNotificationPO po = notificationMapper.selectById(notificationId);
        if (po == null || !po.getUserId().equals(userId)) {
            throw new BusinessException(404, "通知不存在");
        }
        po.setIsRead(1);
        notificationMapper.updateById(po);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        notificationMapper.update(null,
                new LambdaUpdateWrapper<MistakeNotificationPO>()
                        .eq(MistakeNotificationPO::getUserId, userId)
                        .eq(MistakeNotificationPO::getIsRead, 0)
                        .set(MistakeNotificationPO::getIsRead, 1)
        );
    }

    private MistakeNotificationVO toVO(MistakeNotificationPO po) {
        MistakeNotificationVO vo = new MistakeNotificationVO();
        vo.setId(po.getId());
        vo.setType(po.getType());
        vo.setTitle(po.getTitle());
        vo.setContent(po.getContent());
        vo.setIsRead(po.getIsRead() != null && po.getIsRead() == 1);
        vo.setCreatedAt(po.getCreatedAt());
        return vo;
    }
}
