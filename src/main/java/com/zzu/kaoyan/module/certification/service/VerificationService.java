package com.zzu.kaoyan.module.certification.service;

import com.zzu.kaoyan.module.certification.dto.VerificationSubmitDTO;
import com.zzu.kaoyan.module.certification.vo.VerificationVO;

public interface VerificationService {

    /** 提交认证申请 */
    VerificationVO submit(Long userId, VerificationSubmitDTO dto);

    /** 查询当前用户的认证状态 */
    VerificationVO getMyStatus(Long userId);

    /** 管理员分页查询认证列表 */
    Object listByStatus(Integer status, int pageNum, int pageSize);

    /** 管理员审核 */
    VerificationVO review(Long adminId, Long verificationId, Integer status, String comment);
}