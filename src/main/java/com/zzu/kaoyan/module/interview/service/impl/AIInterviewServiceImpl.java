package com.zzu.kaoyan.module.interview.service.impl;

import com.zzu.kaoyan.module.interview.entity.po.InterviewQuestion;
import com.zzu.kaoyan.module.interview.entity.vo.DimensionScoresVO;
import com.zzu.kaoyan.module.interview.service.AIInterviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AIInterviewServiceImpl implements AIInterviewService {

    private final Random random = new Random();

    @Override
    public String generateQuestion(String schoolStyle, String major, String interviewType,
                                   int questionNumber, String previousQAs) {
        List<String> pool = selectQuestionPool(schoolStyle, interviewType);
        int index = (questionNumber - 1) % pool.size();
        String question = pool.get(index);

        if (questionNumber > 1 && previousQAs != null && !previousQAs.isEmpty()) {
            question = buildFollowUp(question, previousQAs, schoolStyle);
        }
        return question;
    }

    @Override
    public void evaluateAnswer(InterviewQuestion question, String answer, String schoolStyle) {
        DimensionScoresVO dims = calculateDimensionScores(question.getQuestionType(), answer);
        int avgScore = (dims.getContentDepth() + dims.getLogicClarity() + dims.getLanguageExpress()
                + dims.getProfessionalKnowledge() + dims.getAdaptability() + dims.getPsychology()) / 6;

        question.setAiScore(avgScore);
        question.setAiComment(buildComment(dims, question.getQuestionType()));
        question.setDimensionScores(toJson(dims));
    }

    @Override
    public DimensionScoresVO calculateDimensionScores(String questionType, String answer) {
        DimensionScoresVO vo = new DimensionScoresVO();
        int base = 55 + random.nextInt(40);
        int lenBonus = Math.min(15, answer.length() / 20);
        vo.setContentDepth(clamp(base + random.nextInt(10) - 5 + lenBonus));
        vo.setLogicClarity(clamp(base + random.nextInt(10) - 5 + lenBonus / 2));
        vo.setLanguageExpress(clamp(base + random.nextInt(10) - 3 + lenBonus / 2));
        vo.setProfessionalKnowledge(clamp(base + random.nextInt(10) - 8 + lenBonus));
        vo.setAdaptability(clamp(base + random.nextInt(10) - 6 + lenBonus / 2));
        vo.setPsychology(clamp(base + random.nextInt(10) - 4));
        return vo;
    }

    @Override
    public String generateSummaryReport(String schoolStyle, String major, String interviewType, String allQAs) {
        return String.format(
                "该考生在%s风格的%s专业%s面试中表现总体良好。"
                        + "专业知识掌握较为扎实，能够对核心概念进行阐述。"
                        + "在回答问题时逻辑清晰，但在深度展开和临场应变方面仍有提升空间。"
                        + "建议进一步加强专业前沿动态的关注和英语口语表达的训练。",
                getSchoolName(schoolStyle), major, getTypeName(interviewType));
    }

    private List<String> selectQuestionPool(String schoolStyle, String interviewType) {
        return switch (interviewType) {
            case "english_self" -> ENGLISH_SELF_POOL;
            case "english_qa" -> ENGLISH_QA_POOL;
            case "professional" -> PROFESSIONAL_POOL;
            case "stress" -> STRESS_POOL;
            default -> switch (schoolStyle) {
                case "tsinghua" -> TSINGHUA_COMPREHENSIVE_POOL;
                case "tongji" -> TONGJI_COMPREHENSIVE_POOL;
                default -> DEFAULT_COMPREHENSIVE_POOL;
            };
        };
    }

    private String buildFollowUp(String baseQuestion, String previousQAs, String schoolStyle) {
        String[] followUps = {
                "你刚才提到了这一点，能再深入展开说说吗？",
                "针对你上一个回答，如果遇到相反的观点你会如何应对？",
                "你刚才说的那个项目/经历，具体你承担了什么角色？遇到了什么困难？",
                "那我们来换个角度，假如结果不如预期，你的备选方案是什么？",
                "能否结合近两年的学术前沿，再补充一下你的看法？"
        };
        if ("tsinghua".equals(schoolStyle)) {
            followUps = new String[]{
                    "你提到的这个概念，能否从第一性原理出发重新推导一遍？",
                    "如果让你给本科生讲清楚这个知识点，你会怎么组织思路？",
                    "你的回答偏工程应用，能否从理论层面再做一些分析？",
                    "我注意到你回避了一个关键点，请正面回答：你的核心竞争力是什么？"
            };
        }
        return followUps[random.nextInt(followUps.length)];
    }

    private String buildComment(DimensionScoresVO dims, String questionType) {
        int avg = (dims.getContentDepth() + dims.getLogicClarity() + dims.getLanguageExpress()
                + dims.getProfessionalKnowledge() + dims.getAdaptability() + dims.getPsychology()) / 6;
        if (avg >= 80) return "回答全面且深入，逻辑清晰，展现了扎实的专业功底和良好的表达能力。";
        if (avg >= 65) return "回答整体不错，核心要点基本覆盖，可在深度和细节上进一步打磨。";
        if (avg >= 50) return "回答触及了部分要点，但深度和逻辑性有待加强，建议多结合实例。";
        return "回答较为简略，建议围绕问题核心展开论述，并结合自身经历增加说服力。";
    }

    private String toJson(DimensionScoresVO vo) {
        return String.format(
                "{\"contentDepth\":%d,\"logicClarity\":%d,\"languageExpress\":%d,"
                        + "\"professionalKnowledge\":%d,\"adaptability\":%d,\"psychology\":%d}",
                vo.getContentDepth(), vo.getLogicClarity(), vo.getLanguageExpress(),
                vo.getProfessionalKnowledge(), vo.getAdaptability(), vo.getPsychology());
    }

    private int clamp(int score) {
        return Math.max(10, Math.min(98, score));
    }

    String getSchoolName(String style) {
        return switch (style) {
            case "tsinghua" -> "清华";
            case "tongji" -> "同济";
            case "985" -> "985院校";
            case "211" -> "211院校";
            default -> "普通院校";
        };
    }

    String getTypeName(String type) {
        return switch (type) {
            case "english_self" -> "英语自我介绍";
            case "english_qa" -> "英语问答";
            case "professional" -> "专业课面试";
            case "stress" -> "压力面试";
            default -> "综合面试";
        };
    }

    // ============ 问题库 ============

    private static final List<String> ENGLISH_SELF_POOL = List.of(
            "Please introduce yourself in English, including your academic background and research interests.",
            "Tell us about a challenging project you worked on and what you learned from it.",
            "Why did you choose this university and this major for your postgraduate study?",
            "Describe your strengths and weaknesses in English. Be honest and specific.",
            "Where do you see yourself in five years after completing your postgraduate study?",
            "What's your favorite course during your undergraduate study? And why?",
            "Tell us about a book or paper that influenced your academic thinking.",
            "Describe a time when you worked in a team to solve a difficult problem."
    );

    private static final List<String> ENGLISH_QA_POOL = List.of(
            "Can you explain the concept of machine learning in simple terms?",
            "What do you think is the biggest challenge in your research field right now?",
            "How would you describe the difference between supervised and unsupervised learning?",
            "In your opinion, what makes a good researcher?",
            "Tell me about the most recent paper you read. What was its main contribution?",
            "How do you approach learning something completely new?",
            "Discuss the ethical implications of artificial intelligence in education.",
            "What role does data structures play in efficient algorithm design?"
    );

    private static final List<String> PROFESSIONAL_POOL = List.of(
            "请简述数据结构中栈和队列的区别，并分别举例实际应用场景。",
            "解释操作系统中进程和线程的区别，以及上下文切换的开销。",
            "TCP三次握手和四次挥手的过程是怎样的？为什么需要三次握手？",
            "数据库事务的ACID特性是什么？请逐一解释并举例。",
            "什么是死锁？死锁的四个必要条件是什么？如何预防？",
            "请解释HTTP和HTTPS的区别，以及SSL/TLS的工作原理。",
            "简述面向对象编程的三大特性，并说明它们在设计模式中的应用。",
            "什么是微服务架构？相比单体架构有哪些优缺点？"
    );

    private static final List<String> STRESS_POOL = List.of(
            "你刚才的回答中有一个明显的逻辑漏洞，你意识到了吗？请重新组织你的回答。",
            "你的本科成绩并不突出，凭什么认为你能胜任研究生阶段的学习？",
            "如果导师给你分配了一个你不感兴趣的方向，你会怎么办？",
            "我觉得你对这个问题的理解还停留在本科教材水平，你能说得更深入一些吗？",
            "假如这次复试你没通过，你觉得问题出在哪里？",
            "你简历上写的这个项目，听起来像是简单的CRUD，有什么技术难点吗？",
            "如果我和另一位面试官对你的评价完全相反，你认为谁说得对？为什么？",
            "你刚才引用的那个理论，它的前提假设是什么？你确定在你说的场景下成立吗？"
    );

    private static final List<String> TSINGHUA_COMPREHENSIVE_POOL = List.of(
            "请从理论层面分析你所做项目的创新点，不要停留在工程实现层面。",
            "你如何看待计算机科学基础理论（如计算理论、形式语言）对工程实践的影响？",
            "如果让你重新设计操作系统，你会对现有架构做什么根本性的改变？",
            "请描述一个你认为最美的算法，并说明为什么。",
            "在科研中，你更看重问题的理论深度还是实际应用价值？请阐述你的观点。",
            "你了解图灵奖最近三年的获奖工作吗？请谈谈其中一项对你的启发。"
    );

    private static final List<String> TONGJI_COMPREHENSIVE_POOL = List.of(
            "同济土木与计算机的交叉方向近年来有很多成果，你对此有什么了解？",
            "你如何看待学科交叉在解决实际工程问题中的作用？请结合你的经历谈谈。",
            "如果让你做一个智慧城市相关的课题，你会选择哪个切入点？为什么？",
            "在团队协作中，你通常扮演什么角色？请举例说明。",
            "你怎么看待理论研究与工程落地之间的关系？你觉得两者哪个更重要？"
    );

    private static final List<String> DEFAULT_COMPREHENSIVE_POOL = List.of(
            "请简单做一下自我介绍，包括你的本科背景、科研经历和报考动机。",
            "你本科期间最有成就感的一件事是什么？",
            "你读过哪些本专业的经典著作或论文？请谈谈你的收获。",
            "你为什么选择考研而不是直接工作？你对研究生阶段有什么规划？",
            "你对目标专业目前的研究热点有什么了解？你感兴趣的方向是什么？",
            "请描述一次你解决过的复杂技术问题，你在其中做了什么？",
            "你如何看待学术诚信问题？如果你发现同学有学术不端行为会怎么做？",
            "你认为研究生应该具备哪些素质和能力？你目前具备了哪些？"
    );
}
