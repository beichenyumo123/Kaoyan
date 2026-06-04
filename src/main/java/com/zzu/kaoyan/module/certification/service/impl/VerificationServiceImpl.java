package com.zzu.kaoyan.module.certification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.activity.entity.po.PointsLogPO;
import com.zzu.kaoyan.module.activity.mapper.PointsLogMapper;
import com.zzu.kaoyan.module.certification.dto.VerificationSubmitDTO;
import com.zzu.kaoyan.module.certification.entity.UserVerification;
import com.zzu.kaoyan.module.certification.mapper.UserVerificationMapper;
import com.zzu.kaoyan.module.certification.service.VerificationService;
import com.zzu.kaoyan.module.certification.vo.VerificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class VerificationServiceImpl implements VerificationService {

    private final UserVerificationMapper userVerificationMapper;
    private final UserMapper userMapper;
    private final PointsLogMapper pointsLogMapper;

    public VerificationServiceImpl(UserVerificationMapper userVerificationMapper,
                                   UserMapper userMapper,
                                   PointsLogMapper pointsLogMapper) {
        this.userVerificationMapper = userVerificationMapper;
        this.userMapper = userMapper;
        this.pointsLogMapper = pointsLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerificationVO submit(Long userId, VerificationSubmitDTO dto) {
        UserVerification existing = userVerificationMapper.selectOne(
                Wrappers.<UserVerification>lambdaQuery()
                        .eq(UserVerification::getUserId, userId)
        );

        if (existing != null && existing.getStatus() != null && existing.getStatus() == 1) {
            throw new BusinessException(ResultCode.ALREADY_VERIFIED);
        }
        if (existing != null && existing.getStatus() != null && existing.getStatus() == 0) {
            throw new BusinessException(ResultCode.VERIFICATION_PENDING);
        }

        UserVerification entity = new UserVerification();
        BeanUtils.copyProperties(dto, entity);
        entity.setUserId(userId);
        entity.setStatus(0); // 待审核

        if (existing != null && existing.getStatus() == 2) {
            // 驳回后重新提交，更新原记录
            entity.setId(existing.getId());
            userVerificationMapper.updateById(entity);
        } else {
            userVerificationMapper.insert(entity);
        }

        VerificationVO vo = new VerificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public VerificationVO getMyStatus(Long userId) {
        UserVerification entity = userVerificationMapper.selectOne(
                Wrappers.<UserVerification>lambdaQuery()
                        .eq(UserVerification::getUserId, userId)
        );
        if (entity == null) {
            return null;
        }
        VerificationVO vo = new VerificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public PageInfo<VerificationVO> listByStatus(Integer status, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<UserVerification> wrapper = Wrappers.<UserVerification>lambdaQuery()
                .orderByDesc(UserVerification::getCreatedAt);
        if (status != null) {
            wrapper.eq(UserVerification::getStatus, status);
        }
        PageInfo<UserVerification> page = new PageInfo<>(userVerificationMapper.selectList(wrapper));

        PageInfo<VerificationVO> voPage = new PageInfo<>();
        BeanUtils.copyProperties(page, voPage);
        voPage.setList(page.getList().stream().map(e -> {
            VerificationVO vo = new VerificationVO();
            BeanUtils.copyProperties(e, vo);
            return vo;
        }).toList());
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerificationVO review(Long adminId, Long verificationId, Integer status, String comment) {
        UserVerification entity = userVerificationMapper.selectById(verificationId);
        if (entity == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        entity.setStatus(status);
        entity.setReviewerId(adminId);
        entity.setReviewComment(comment);
        entity.setReviewedAt(LocalDateTime.now());
        userVerificationMapper.updateById(entity);

        if (status == 1) {
            // 审核通过：更新 sys_user.is_verified
            User user = userMapper.selectById(entity.getUserId());
            if (user != null) {
                user.setIsVerified(true);
                userMapper.updateById(user);
            }

            // 认证通过 +50 积分
            PointsLogPO log = new PointsLogPO();
            log.setUserId(entity.getUserId());
            log.setPoints(50);
            log.setType("VERIFICATION");
            log.setRelId(entity.getId());
            log.setDescription("上岸认证通过 +50分");
            pointsLogMapper.insert(log);

            // 更新用户总积分
            if (user != null) {
                user.setPoints((user.getPoints() == null ? 0 : user.getPoints()) + 50);
                userMapper.updateById(user);
            }
        }

        VerificationVO vo = new VerificationVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}