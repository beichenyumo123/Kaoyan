package com.zzu.kaoyan.module.interact.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zzu.kaoyan.module.interact.entity.ForumReport;
import com.zzu.kaoyan.module.interact.entity.dto.ReportQueryDTO;
import com.zzu.kaoyan.module.interact.entity.dto.SubmitReportDTO;
import com.zzu.kaoyan.module.interact.mapper.ReportMapper;
import com.zzu.kaoyan.module.interact.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl extends ServiceImpl<ReportMapper, ForumReport> implements ReportService {

    @Override
    @Transactional(rollbackFor = Exception.class) // 开启事务
    public void submitReport(Long reporterId, SubmitReportDTO dto) {
        
        // 1. 业务校验：枚举值合法性检查
        List<String> validTypes = Arrays.asList("POST", "COMMENT", "USER");
        if (!validTypes.contains(dto.getTargetType().toUpperCase())) {
            // 注意：这里抛出的 RuntimeException 会被你之前的 GlobalExceptionHandler 捕获并返回给前端
            // 建议后续替换为你自定义的业务异常类，例如 throw new BusinessException("非法的举报类型");
            throw new RuntimeException("非法的举报类型"); 
        }

        // 2. 防刷拦截：检查该用户是否对同一个目标重复提交了举报（且管理员还没处理）
        LambdaQueryWrapper<ForumReport> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ForumReport::getReporterId, reporterId)
                .eq(ForumReport::getTargetType, dto.getTargetType().toUpperCase())
                .eq(ForumReport::getTargetId, dto.getTargetId())
                .eq(ForumReport::getStatus, 0); // 0 表示还在待处理状态
        
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("您已提交过该举报，后台正在加紧处理中，请勿重复提交");
        }

        // 3. 封装实体类并保存到数据库
        ForumReport report = new ForumReport();
        report.setReporterId(reporterId);
        report.setTargetType(dto.getTargetType().toUpperCase());
        report.setTargetId(dto.getTargetId());
        report.setReason(dto.getReason());
        report.setStatus(0); // 默认状态为 0-待处理

        // 调用 MyBatis-Plus 提供的 save 方法直接入库
        this.save(report);
    }

    @Override
    public Page<ForumReport> getReportList(ReportQueryDTO queryDTO) {
        // 1. 初始化 MyBatis-Plus 分页对象
        Page<ForumReport> pageParam = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());

        // 2. 构建动态查询条件
        LambdaQueryWrapper<ForumReport> wrapper = new LambdaQueryWrapper<>();

        // 如果前端传了 status 参数，就精确匹配 status
        if (queryDTO.getStatus() != null) {
            wrapper.eq(ForumReport::getStatus, queryDTO.getStatus());
        }

        // 如果前端传了 targetType 参数，就精确匹配类型
        if (queryDTO.getTargetType() != null && !queryDTO.getTargetType().isEmpty()) {
            wrapper.eq(ForumReport::getTargetType, queryDTO.getTargetType().toUpperCase());
        }

        // 3. 核心排序逻辑：
        // 优先按照状态升序 (0-待处理排在最前面，方便审核人员第一眼看到)
        // 状态相同时，按照创建时间降序 (最新提交的排在前面)
        wrapper.orderByAsc(ForumReport::getStatus)
                .orderByDesc(ForumReport::getCreatedAt);

        // 4. 执行分页查询并返回
        return this.page(pageParam, wrapper);
    }
}