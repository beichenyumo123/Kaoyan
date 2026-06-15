package com.zzu.kaoyan.module.schoolselect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.schoolselect.entity.dto.RecommendationRequestDTO;
import com.zzu.kaoyan.module.schoolselect.entity.po.AdmissionRecord;
import com.zzu.kaoyan.module.schoolselect.entity.po.SchoolInfo;
import com.zzu.kaoyan.module.schoolselect.entity.po.SchoolMajor;
import com.zzu.kaoyan.module.schoolselect.entity.vo.RecommendationResultVO;
import com.zzu.kaoyan.module.schoolselect.entity.vo.SchoolTierVO;
import com.zzu.kaoyan.module.schoolselect.entity.vo.SimilarUserCaseVO;
import com.zzu.kaoyan.module.schoolselect.mapper.AdmissionRecordMapper;
import com.zzu.kaoyan.module.schoolselect.mapper.SchoolInfoMapper;
import com.zzu.kaoyan.module.schoolselect.mapper.SchoolMajorMapper;
import com.zzu.kaoyan.module.schoolselect.service.MatchEngineService;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.common.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchEngineServiceImpl implements MatchEngineService {

    private final SchoolInfoMapper schoolInfoMapper;
    private final SchoolMajorMapper schoolMajorMapper;
    private final AdmissionRecordMapper admissionRecordMapper;
    private final UserMapper userMapper;

    private static final Map<String, Integer> LEVEL_SCORE = Map.of(
        "C9", 100, "985", 80, "211", 65,
        "DOUBLE_FIRST_CLASS", 60, "DOUBLE_NON", 45, "ORDINARY", 30
    );

    private static final Map<String, Integer> ENGLISH_TIER = Map.of(
        "TEM8", 5, "TEM4", 4, "CET6", 3, "CET4", 2, "NONE", 1
    );

    @Override
    public RecommendationResultVO recommend(RecommendationRequestDTO dto) {
        double userScore = calcUserScore(dto);

        List<SchoolInfo> allSchools = schoolInfoMapper.selectList(
            new LambdaQueryWrapper<SchoolInfo>().orderByDesc(SchoolInfo::getAvgAdmissionScore));

        List<SchoolMajor> allMajors = schoolMajorMapper.selectList(
            new LambdaQueryWrapper<SchoolMajor>().orderByDesc(SchoolMajor::getAvgScore));

        Map<Long, List<SchoolMajor>> majorsBySchool = allMajors.stream()
            .collect(Collectors.groupingBy(SchoolMajor::getSchoolId));

        // Score every school
        List<ScoredSchool> scored = new ArrayList<>();
        for (SchoolInfo school : allSchools) {
            if (school.getAvgAdmissionScore() == null) continue;
            double schoolNormScore = school.getAvgAdmissionScore() / 500.0 * 100.0;
            double matchScore = calcMatchScore(school, dto);
            double admitProb = calcAdmitProb(userScore, schoolNormScore);
            scored.add(new ScoredSchool(school, matchScore, admitProb, schoolNormScore));
        }

        // Build tiers: safety first, then match, then reach (with dedup)
        Set<Long> usedIds = new HashSet<>();
        RecommendationResultVO result = new RecommendationResultVO();

        result.setSafety(buildTier(scored, userScore, "safety", usedIds, majorsBySchool, dto));
        result.setMatch(buildTier(scored, userScore, "match", usedIds, majorsBySchool, dto));
        result.setReach(buildTier(scored, userScore, "reach", usedIds, majorsBySchool, dto));

        result.setSimilarUsers(findSimilarUsers(dto));
        return result;
    }

    // --- User score ---

    private double calcUserScore(RecommendationRequestDTO dto) {
        double examPart = 0;
        if (dto.getMockExamScore() != null && dto.getMockExamScore() > 0) {
            examPart = dto.getMockExamScore() / 500.0 * 100.0;
        }
        double gpaPart = dto.getGpa().doubleValue() / 4.0 * 100.0;

        if (dto.getMockExamScore() != null && dto.getMockExamScore() > 0) {
            return examPart * 0.7 + gpaPart * 0.3;
        }
        return gpaPart;
    }

    // --- School match score ---

    private double calcMatchScore(SchoolInfo school, RecommendationRequestDTO dto) {
        double score = 0;

        // Score gap (40%)
        if (dto.getMockExamScore() != null && dto.getMockExamScore() > 0
                && school.getAvgAdmissionScore() != null) {
            double gap = Math.abs(dto.getMockExamScore() - school.getAvgAdmissionScore());
            score += Math.max(0, 100 - gap / 5.0) * 0.40;
        } else {
            score += 50 * 0.40;
        }

        // School level (15%)
        score += LEVEL_SCORE.getOrDefault(school.getLevel(), 30) * 0.15;

        // English match (10%)
        score += calcEnglishMatch(dto.getEnglishLevel()) * 0.10;

        // Prep fit (10%)
        score += 50 * 0.10;

        // GPA fit (5%)
        score += Math.min(dto.getGpa().doubleValue() / 4.0 * 100, 100) * 0.05;

        // Location (20%) — neutral baseline for v1
        score += 50 * 0.20;

        return Math.min(100, Math.max(0, score));
    }

    private double calcEnglishMatch(String level) {
        return ENGLISH_TIER.getOrDefault(level, 1) / 5.0 * 100;
    }

    // --- Tier builder (score-first, then matchScore within tier) ---

    private List<SchoolTierVO> buildTier(List<ScoredSchool> scored, double userScore,
                                         String tier, Set<Long> usedIds,
                                         Map<Long, List<SchoolMajor>> majorsBySchool,
                                         RecommendationRequestDTO dto) {
        List<ScoredSchool> pool;
        int minResults = 2;

        switch (tier) {
            case "safety":
                // Safety: schools noticeably easier to get into
                List<ScoredSchool> safetyPool = scored.stream()
                    .filter(s -> s.schoolNormScore < userScore - 3 && !usedIds.contains(s.school.getId()))
                    .collect(Collectors.toList());
                if (safetyPool.size() < minResults) {
                    safetyPool = scored.stream()
                        .filter(s -> s.schoolNormScore < userScore && !usedIds.contains(s.school.getId()))
                        .collect(Collectors.toList());
                }
                pool = safetyPool;
                break;
            case "reach":
                List<ScoredSchool> reachPool = scored.stream()
                    .filter(s -> s.schoolNormScore > userScore && !usedIds.contains(s.school.getId()))
                    .collect(Collectors.toList());
                if (reachPool.size() < minResults) {
                    reachPool = scored.stream()
                        .filter(s -> s.schoolNormScore > userScore - 5 && !usedIds.contains(s.school.getId()))
                        .collect(Collectors.toList());
                }
                pool = reachPool;
                break;
            default: // match
                List<ScoredSchool> matchPool = scored.stream()
                    .filter(s -> Math.abs(s.schoolNormScore - userScore) <= 10 && !usedIds.contains(s.school.getId()))
                    .collect(Collectors.toList());
                if (matchPool.size() < minResults) {
                    matchPool = scored.stream()
                        .filter(s -> Math.abs(s.schoolNormScore - userScore) <= 15 && !usedIds.contains(s.school.getId()))
                        .collect(Collectors.toList());
                }
                pool = matchPool;
                break;
        }

        // Sort by matchScore descending, take top 3
        pool.sort((a, b) -> Double.compare(b.matchScore, a.matchScore));
        List<ScoredSchool> selected = pool.stream().limit(3).collect(Collectors.toList());

        List<SchoolTierVO> result = new ArrayList<>();
        for (ScoredSchool ss : selected) {
            usedIds.add(ss.school.getId());
            SchoolTierVO vo = new SchoolTierVO();
            vo.setSchoolId(ss.school.getId());
            vo.setSchoolName(ss.school.getName());
            vo.setSchoolLevel(ss.school.getLevel());
            vo.setLocation(ss.school.getLocation());
            vo.setLogoUrl(ss.school.getLogoUrl());
            vo.setAvgAdmissionScore(ss.school.getAvgAdmissionScore());
            vo.setMatchScore((int) Math.round(ss.matchScore));
            vo.setAdmitProbability(BigDecimal.valueOf(ss.admitProb).setScale(2, RoundingMode.HALF_UP));

            List<SchoolMajor> majors = majorsBySchool.getOrDefault(ss.school.getId(), List.of());
            vo.setRelatedMajors(majors.stream().map(SchoolMajor::getMajorName).distinct().limit(3).collect(Collectors.toList()));

            if (ss.school.getAvgAdmissionScore() != null && dto.getMockExamScore() != null) {
                int delta = ss.school.getAvgAdmissionScore() - dto.getMockExamScore();
                vo.setMatchReason(generateReason(ss.school.getName(), tier, delta));
            } else {
                vo.setMatchReason(generateReason(ss.school.getName(), tier, 0));
            }

            result.add(vo);
        }
        return result;
    }

    private String generateReason(String name, String tier, int delta) {
        switch (tier) {
            case "safety":
                if (delta < 0) {
                    return "院校" + name + "往年录取均分低于您的模考" + Math.abs(delta) + "分，是稳妥保底选择";
                } else if (delta == 0) {
                    return "院校" + name + "往年录取均分与您的模考持平，作为保底胜算很大";
                } else {
                    return "院校" + name + "往年录取分数尚可，作为保底值得考虑";
                }
            case "match":
                if (Math.abs(delta) <= 10) {
                    return "院校" + name + "往年录取均分与您的模考分数相近，值得重点关注";
                }
                return "院校" + name + "与您的水平较为匹配，建议深入了解";
            case "reach":
                if (delta > 0) {
                    return "院校" + name + "往年录取均分高于您的模考" + delta + "分，需要加油冲刺";
                }
                return "院校" + name + "竞争激烈，作为冲刺目标需要加倍努力";
            default:
                return "院校" + name + "是您的潜在目标";
        }
    }

    // --- Admit probability (logistic) ---

    private double calcAdmitProb(double userScore, double schoolScore) {
        double k = 8.0;
        return 1.0 / (1.0 + Math.exp(-(userScore - schoolScore) / k));
    }

    // --- Similar users ---

    private List<SimilarUserCaseVO> findSimilarUsers(RecommendationRequestDTO dto) {
        List<AdmissionRecord> allRecords = admissionRecordMapper.selectList(
            new LambdaQueryWrapper<AdmissionRecord>()
                .eq(AdmissionRecord::getIsVerified, true));

        if (allRecords.isEmpty()) return List.of();

        List<SimilarUserCaseVO> result = new ArrayList<>();
        double userGpa = dto.getGpa().doubleValue();

        for (AdmissionRecord record : allRecords) {
            if (record.getUndergradGpa() == null) continue;

            double recGpa = record.getUndergradGpa().doubleValue();
            if (Math.abs(recGpa - userGpa) > 0.5) continue;

            if (record.getPrepDuration() != null && dto.getPrepDuration() != null) {
                if (Math.abs(record.getPrepDuration() - dto.getPrepDuration()) > 6) continue;
            }

            double similarity = calcSimilarity(dto, record);

            SimilarUserCaseVO vo = new SimilarUserCaseVO();
            vo.setUserId(record.getUserId());
            vo.setUsername(desensitizeUsername(record.getUserId()));
            vo.setUndergradSchool(record.getUndergradSchool());
            vo.setUndergradGpa(record.getUndergradGpa());
            vo.setEnglishLevel(record.getEnglishLevel());
            vo.setPrepDuration(record.getPrepDuration());
            vo.setExamScoreTotal(record.getExamScoreTotal());
            vo.setSimilarity(BigDecimal.valueOf(similarity).setScale(2, RoundingMode.HALF_UP));

            // Look up admitted school name
            if (record.getSchoolId() != null) {
                SchoolInfo school = schoolInfoMapper.selectById(record.getSchoolId());
                vo.setAdmittedSchool(school != null ? school.getName() : "未知");
            } else {
                vo.setAdmittedSchool("未知");
            }
            vo.setAdmittedMajor(record.getMajorName());

            result.add(vo);
        }

        result.sort(Comparator.comparing(SimilarUserCaseVO::getSimilarity).reversed());
        return result.stream().limit(5).collect(Collectors.toList());
    }

    private double calcSimilarity(RecommendationRequestDTO dto, AdmissionRecord record) {
        double sim = 0;
        // GPA similarity (30%)
        if (record.getUndergradGpa() != null) {
            double gpaDiff = Math.abs(record.getUndergradGpa().doubleValue() - dto.getGpa().doubleValue());
            sim += (1.0 - gpaDiff / 4.0) * 0.30;
        }
        // Prep duration similarity (25%)
        if (record.getPrepDuration() != null && dto.getPrepDuration() != null) {
            double prepDiff = Math.abs(record.getPrepDuration() - dto.getPrepDuration());
            sim += (1.0 - prepDiff / 24.0) * 0.25;
        }
        // English tier similarity (20%)
        int userEng = ENGLISH_TIER.getOrDefault(dto.getEnglishLevel(), 1);
        int recEng = ENGLISH_TIER.getOrDefault(record.getEnglishLevel(), 1);
        sim += (1.0 - Math.abs(userEng - recEng) / 4.0) * 0.20;

        // Score proximity (25%)
        if (record.getExamScoreTotal() != null && dto.getMockExamScore() != null) {
            double scoreDiff = Math.abs(record.getExamScoreTotal() - dto.getMockExamScore());
            sim += (1.0 - scoreDiff / 500.0) * 0.25;
        }

        return Math.max(0, Math.min(1, sim));
    }

    private String desensitizeUsername(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getUsername() == null) return "匿名用户";
        String name = user.getUsername();
        if (name.length() <= 2) return name.charAt(0) + "*";
        return name.charAt(0) + "**";
    }

    // --- Inner class ---

    private static class ScoredSchool {
        final SchoolInfo school;
        final double matchScore;
        final double admitProb;
        final double schoolNormScore;

        ScoredSchool(SchoolInfo school, double matchScore, double admitProb, double schoolNormScore) {
            this.school = school;
            this.matchScore = matchScore;
            this.admitProb = admitProb;
            this.schoolNormScore = schoolNormScore;
        }
    }

}
