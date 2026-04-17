package com.zzu.kaoyan.module.interact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zzu.kaoyan.module.interact.entity.ForumReport;
import com.zzu.kaoyan.module.interact.entity.dto.SubmitReportDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzu.kaoyan.module.interact.entity.dto.ReportQueryDTO;

public interface ReportService extends IService<ForumReport> {
    
    /**
     * 提交举报记录
     * @param reporterId 举报人ID
     * @param dto 举报参数
     */
    void submitReport(Long reporterId, SubmitReportDTO dto);

    Page<ForumReport> getReportList(ReportQueryDTO queryDTO);

}