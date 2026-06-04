package com.zzu.kaoyan.module.experience.service;

import com.github.pagehelper.PageInfo;
import com.zzu.kaoyan.module.experience.dto.ExperiencePostDTO;
import com.zzu.kaoyan.module.experience.vo.ExperiencePostVO;

public interface ExperienceService {

    /** 创建经验贴 */
    ExperiencePostVO create(Long userId, ExperiencePostDTO dto);

    /** 编辑经验贴 */
    ExperiencePostVO update(Long userId, Long experienceId, ExperiencePostDTO dto);

    /** 经验贴详情 */
    ExperiencePostVO getDetail(Long experienceId, Long currentUserId);

    /** 分页列表（支持筛选） */
    PageInfo<ExperiencePostVO> list(int pageNum, int pageSize, Integer isVerified, String targetSchool, String undergradSchool);

    /** 精准检索（用于择校引擎：按本科+目标+分数区间） */
    PageInfo<ExperiencePostVO> search(String undergradSchool, String targetSchool,
                                      java.math.BigDecimal minScore, java.math.BigDecimal maxScore,
                                      Integer isVerified, int pageNum, int pageSize);

    /** 删除 */
    void delete(Long userId, Long experienceId);

    /** 切换点赞 */
    boolean toggleLike(Long experienceId, Long userId);

    /** 切换收藏 */
    boolean toggleCollect(Long experienceId, Long userId);

    /** 我的收藏 */
    PageInfo<ExperiencePostVO> myCollects(Long userId, int pageNum, int pageSize);

    /** 某用户的所有经验贴 */
    PageInfo<ExperiencePostVO> listByUserId(Long targetUserId, int pageNum, int pageSize);
}