package com.zzu.kaoyan.module.experience.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.exception.BusinessException;
import com.zzu.kaoyan.common.result.ResultCode;
import com.zzu.kaoyan.common.util.SensitiveWordUtil;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.activity.entity.po.PointsLogPO;
import com.zzu.kaoyan.module.activity.mapper.PointsLogMapper;
import com.zzu.kaoyan.module.certification.entity.UserVerification;
import com.zzu.kaoyan.module.certification.mapper.UserVerificationMapper;
import com.zzu.kaoyan.module.experience.dto.ExperiencePostDTO;
import com.zzu.kaoyan.module.experience.entity.ExperienceCollect;
import com.zzu.kaoyan.module.experience.entity.ExperienceLike;
import com.zzu.kaoyan.module.experience.entity.ExperiencePost;
import com.zzu.kaoyan.module.experience.mapper.ExperienceCollectMapper;
import com.zzu.kaoyan.module.experience.mapper.ExperienceLikeMapper;
import com.zzu.kaoyan.module.experience.mapper.ExperiencePostMapper;
import com.zzu.kaoyan.module.experience.service.ExperienceService;
import com.zzu.kaoyan.module.experience.vo.ExperiencePostVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExperienceServiceImpl implements ExperienceService {

    private final ExperiencePostMapper experiencePostMapper;
    private final ExperienceLikeMapper experienceLikeMapper;
    private final ExperienceCollectMapper experienceCollectMapper;
    private final UserMapper userMapper;
    private final UserVerificationMapper userVerificationMapper;
    private final PointsLogMapper pointsLogMapper;

    public ExperienceServiceImpl(ExperiencePostMapper experiencePostMapper,
                                 ExperienceLikeMapper experienceLikeMapper,
                                 ExperienceCollectMapper experienceCollectMapper,
                                 UserMapper userMapper,
                                 UserVerificationMapper userVerificationMapper,
                                 PointsLogMapper pointsLogMapper) {
        this.experiencePostMapper = experiencePostMapper;
        this.experienceLikeMapper = experienceLikeMapper;
        this.experienceCollectMapper = experienceCollectMapper;
        this.userMapper = userMapper;
        this.userVerificationMapper = userVerificationMapper;
        this.pointsLogMapper = pointsLogMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExperiencePostVO create(Long userId, ExperiencePostDTO dto) {
        User user = userMapper.selectById(userId);

        ExperiencePost post = new ExperiencePost();
        BeanUtils.copyProperties(dto, post);
        post.setUserId(userId);
        post.setIsVerified(user != null && Boolean.TRUE.equals(user.getIsVerified()));
        post.setStatus(1); // 直接发布

        // 敏感词过滤 tips
        if (dto.getTips() != null) {
            SensitiveWordUtil.FilterResult result = SensitiveWordUtil.filter(dto.getTips());
            post.setTips(result.getFilteredText());
            if (result.isHasSensitive()) {
                System.out.println("WARN: 经验贴tips含敏感词 userId=" + userId +
                        " matched=" + result.getMatched());
            }
        }

        experiencePostMapper.insert(post);

        // 发布经验贴 +20 积分
        PointsLogPO log = new PointsLogPO();
        log.setUserId(userId);
        log.setPoints(20);
        log.setType("EXPERIENCE");
        log.setRelId(post.getId());
        log.setDescription("发布经验贴 +20分");
        pointsLogMapper.insert(log);

        if (user != null) {
            user.setPoints((user.getPoints() == null ? 0 : user.getPoints()) + 20);
            userMapper.updateById(user);
        }

        return getDetail(post.getId(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExperiencePostVO update(Long userId, Long experienceId, ExperiencePostDTO dto) {
        ExperiencePost post = experiencePostMapper.selectById(experienceId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        BeanUtils.copyProperties(dto, post, "id", "userId", "isVerified", "status",
                "viewCount", "likeCount", "collectCount", "deleted", "createdAt", "updatedAt");

        if (dto.getTips() != null) {
            SensitiveWordUtil.FilterResult result = SensitiveWordUtil.filter(dto.getTips());
            post.setTips(result.getFilteredText());
        }

        experiencePostMapper.updateById(post);
        return getDetail(experienceId, userId);
    }

    @Override
    public ExperiencePostVO getDetail(Long experienceId, Long currentUserId) {
        ExperiencePost post = experiencePostMapper.selectById(experienceId);
        if (post == null || Boolean.TRUE.equals(post.getDeleted())) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        // 浏览数 +1
        post.setViewCount(post.getViewCount() + 1);
        experiencePostMapper.updateById(post);

        return toVO(post, currentUserId);
    }

    @Override
    public PageInfo<ExperiencePostVO> list(int pageNum, int pageSize,
                                           Integer isVerified, String targetSchool, String undergradSchool) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<ExperiencePost> wrapper = Wrappers.<ExperiencePost>lambdaQuery()
                .eq(ExperiencePost::getStatus, 1)
                .orderByDesc(ExperiencePost::getCreatedAt);

        if (isVerified != null) {
            wrapper.eq(ExperiencePost::getIsVerified, isVerified == 1);
        }
        if (targetSchool != null && !targetSchool.isBlank()) {
            wrapper.eq(ExperiencePost::getTargetSchool, targetSchool);
        }
        if (undergradSchool != null && !undergradSchool.isBlank()) {
            wrapper.eq(ExperiencePost::getUndergradSchool, undergradSchool);
        }

        PageInfo<ExperiencePost> page = new PageInfo<>(experiencePostMapper.selectList(wrapper));
        return toVOPage(page, null);
    }

    @Override
    public PageInfo<ExperiencePostVO> search(String undergradSchool, String targetSchool,
                                              BigDecimal minScore, BigDecimal maxScore,
                                              Integer isVerified, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<ExperiencePost> wrapper = Wrappers.<ExperiencePost>lambdaQuery()
                .eq(ExperiencePost::getStatus, 1)
                .orderByDesc(ExperiencePost::getCreatedAt);

        if (undergradSchool != null && !undergradSchool.isBlank()) {
            wrapper.eq(ExperiencePost::getUndergradSchool, undergradSchool);
        }
        if (targetSchool != null && !targetSchool.isBlank()) {
            wrapper.eq(ExperiencePost::getTargetSchool, targetSchool);
        }
        if (isVerified != null) {
            wrapper.eq(ExperiencePost::getIsVerified, isVerified == 1);
        }
        if (minScore != null) {
            wrapper.ge(ExperiencePost::getInitialExamTotal, minScore);
        }
        if (maxScore != null) {
            wrapper.le(ExperiencePost::getInitialExamTotal, maxScore);
        }

        PageInfo<ExperiencePost> page = new PageInfo<>(experiencePostMapper.selectList(wrapper));
        return toVOPage(page, null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long experienceId) {
        ExperiencePost post = experiencePostMapper.selectById(experienceId);
        if (post == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }

        User user = userMapper.selectById(userId);
        if (!post.getUserId().equals(userId) && (user == null || !"ADMIN".equals(user.getRole()))) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        experiencePostMapper.deleteById(experienceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long experienceId, Long userId) {
        LambdaQueryWrapper<ExperienceLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExperienceLike::getExperienceId, experienceId)
                .eq(ExperienceLike::getUserId, userId);
        ExperienceLike existing = experienceLikeMapper.selectOne(wrapper);

        if (existing != null) {
            experienceLikeMapper.deleteById(existing.getId());
            return false; // 已取消点赞
        } else {
            ExperienceLike like = new ExperienceLike();
            like.setExperienceId(experienceId);
            like.setUserId(userId);
            experienceLikeMapper.insert(like);
            return true; // 已点赞
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleCollect(Long experienceId, Long userId) {
        LambdaQueryWrapper<ExperienceCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExperienceCollect::getExperienceId, experienceId)
                .eq(ExperienceCollect::getUserId, userId);
        ExperienceCollect existing = experienceCollectMapper.selectOne(wrapper);

        if (existing != null) {
            experienceCollectMapper.deleteById(existing.getId());
            return false;
        } else {
            ExperienceCollect collect = new ExperienceCollect();
            collect.setExperienceId(experienceId);
            collect.setUserId(userId);
            experienceCollectMapper.insert(collect);
            return true;
        }
    }

    @Override
    public PageInfo<ExperiencePostVO> myCollects(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<ExperienceCollect> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExperienceCollect::getUserId, userId)
                .orderByDesc(ExperienceCollect::getCreatedAt);
        List<ExperienceCollect> collects = experienceCollectMapper.selectList(wrapper);

        if (collects.isEmpty()) {
            PageInfo<ExperiencePostVO> empty = new PageInfo<>();
            empty.setList(new ArrayList<>());
            empty.setTotal(0);
            return empty;
        }

        List<Long> experienceIds = collects.stream()
                .map(ExperienceCollect::getExperienceId)
                .collect(Collectors.toList());

        List<ExperiencePost> posts = experiencePostMapper.selectBatchIds(experienceIds);
        // 保持收藏顺序
        List<ExperiencePost> ordered = experienceIds.stream()
                .map(id -> posts.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null))
                .filter(p -> p != null && p.getStatus() == 1)
                .collect(Collectors.toList());

        PageInfo<ExperiencePost> page = new PageInfo<>();
        page.setList(ordered);
        page.setTotal(ordered.size()); // 简化处理

        return toVOPage(page, userId);
    }

    @Override
    public PageInfo<ExperiencePostVO> listByUserId(Long targetUserId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<ExperiencePost> wrapper = Wrappers.<ExperiencePost>lambdaQuery()
                .eq(ExperiencePost::getUserId, targetUserId)
                .eq(ExperiencePost::getStatus, 1)
                .orderByDesc(ExperiencePost::getCreatedAt);
        PageInfo<ExperiencePost> page = new PageInfo<>(experiencePostMapper.selectList(wrapper));
        return toVOPage(page, null);
    }

    // ========== 内部方法 ==========

    private ExperiencePostVO toVO(ExperiencePost post, Long currentUserId) {
        ExperiencePostVO vo = new ExperiencePostVO();
        BeanUtils.copyProperties(post, vo);

        // 作者信息
        vo.setAuthor(buildAuthorVO(post.getUserId()));

        // 当前用户是否点赞/收藏
        if (currentUserId != null) {
            vo.setIsLiked(experienceLikeMapper.selectCount(
                    Wrappers.<ExperienceLike>lambdaQuery()
                            .eq(ExperienceLike::getExperienceId, post.getId())
                            .eq(ExperienceLike::getUserId, currentUserId)
            ) > 0);
            vo.setIsCollected(experienceCollectMapper.selectCount(
                    Wrappers.<ExperienceCollect>lambdaQuery()
                            .eq(ExperienceCollect::getExperienceId, post.getId())
                            .eq(ExperienceCollect::getUserId, currentUserId)
            ) > 0);
        }

        return vo;
    }

    private PageInfo<ExperiencePostVO> toVOPage(PageInfo<ExperiencePost> page, Long currentUserId) {
        PageInfo<ExperiencePostVO> voPage = new PageInfo<>();
        BeanUtils.copyProperties(page, voPage);
        voPage.setList(page.getList().stream()
                .map(p -> toVO(p, currentUserId))
                .collect(Collectors.toList()));
        return voPage;
    }

    private ExperiencePostVO.AuthorVO buildAuthorVO(Long userId) {
        ExperiencePostVO.AuthorVO author = new ExperiencePostVO.AuthorVO();
        author.setUserId(userId);

        User user = userMapper.selectById(userId);
        if (user != null) {
            author.setUsername(user.getUsername());
            author.setAvatarUrl(user.getAvatarUrl());
            author.setIsVerified(user.getIsVerified());

            if (Boolean.TRUE.equals(user.getIsVerified())) {
                UserVerification verification = userVerificationMapper.selectOne(
                        Wrappers.<UserVerification>lambdaQuery()
                                .eq(UserVerification::getUserId, userId)
                                .eq(UserVerification::getStatus, 1)
                );
                if (verification != null) {
                    author.setVerifiedSchool(verification.getTargetSchool());
                    author.setVerifiedMajor(verification.getTargetMajor());
                }
            }
        }

        return author;
    }
}