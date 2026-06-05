package com.zzu.kaoyan.module.mistake.service.impl;

import com.zzu.kaoyan.module.mistake.entity.po.KnowledgePointPO;
import com.zzu.kaoyan.module.mistake.entity.vo.KnowledgePointVO;
import com.zzu.kaoyan.module.mistake.mapper.KnowledgePointMapper;
import com.zzu.kaoyan.module.mistake.service.KnowledgePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgePointServiceImpl implements KnowledgePointService {

    private final KnowledgePointMapper knowledgePointMapper;

    @Override
    public List<KnowledgePointVO> getTree(String subject) {
        List<KnowledgePointPO> all;
        if (subject != null && !subject.isEmpty()) {
            all = knowledgePointMapper.selectBySubject(subject);
        } else {
            all = knowledgePointMapper.selectAll();
        }
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        return buildTree(all, null);
    }

    @Override
    public List<KnowledgePointVO> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Collections.emptyList();
        }
        List<KnowledgePointPO> matches = knowledgePointMapper.searchByKeyword(keyword.trim());
        return matches.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public List<Long> matchFromText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        // 获取所有叶子节点（level=3），在文本中查找匹配
        List<KnowledgePointPO> all = knowledgePointMapper.selectAll();
        List<Long> matchedIds = new ArrayList<>();
        for (KnowledgePointPO kp : all) {
            if (kp.getLevel() != null && kp.getLevel() >= 2 && text.contains(kp.getName())) {
                matchedIds.add(kp.getId());
            }
        }
        // 限制最多返回 10 个，优先 level 更深的（更精确）
        if (matchedIds.size() > 10) {
            matchedIds = matchedIds.subList(0, 10);
        }
        return matchedIds;
    }

    /**
     * 递归构建树结构
     */
    private List<KnowledgePointVO> buildTree(List<KnowledgePointPO> all, Long parentId) {
        List<KnowledgePointVO> result = new ArrayList<>();
        for (KnowledgePointPO po : all) {
            boolean parentMatch = (parentId == null && po.getParentId() == null)
                    || (parentId != null && parentId.equals(po.getParentId()));
            if (!parentMatch) continue;

            KnowledgePointVO vo = toVO(po);
            vo.setChildren(buildTree(all, po.getId()));
            result.add(vo);
        }
        return result;
    }

    private KnowledgePointVO toVO(KnowledgePointPO po) {
        KnowledgePointVO vo = new KnowledgePointVO();
        vo.setId(po.getId());
        vo.setParentId(po.getParentId());
        vo.setName(po.getName());
        vo.setSubject(po.getSubject());
        vo.setLevel(po.getLevel());
        vo.setSortOrder(po.getSortOrder());
        return vo;
    }
}
