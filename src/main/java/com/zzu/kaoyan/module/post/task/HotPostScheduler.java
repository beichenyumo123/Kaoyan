package com.zzu.kaoyan.module.post.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzu.kaoyan.module.post.entity.Post;
import com.zzu.kaoyan.mapper.PostMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnBean(RedisConnectionFactory.class)
public class HotPostScheduler {

    private static final Logger log = LoggerFactory.getLogger(HotPostScheduler.class);
    private static final String ZSET_KEY = "hot:posts";
    private static final double GRAVITY = 1.8;

    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    public HotPostScheduler(PostMapper postMapper, RedisTemplate<String, Object> redisTemplate) {
        this.postMapper = postMapper;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedRate = 600000)
    public void computeHotPosts() {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            List<Post> posts = postMapper.selectList(
                    new LambdaQueryWrapper<Post>()
                            .select(Post::getId, Post::getLikeCount, Post::getCreatedAt)
                            .eq(Post::getIsDeleted, 0)
                            .ge(Post::getCreatedAt, sevenDaysAgo));

            if (posts.isEmpty()) {
                log.debug("近 7 天无帖子，跳过热度计算");
                return;
            }

            redisTemplate.delete(ZSET_KEY);

            LocalDateTime now = LocalDateTime.now();
            for (Post post : posts) {
                double score = computeScore(post.getLikeCount(), post.getCreatedAt(), now);
                redisTemplate.opsForZSet().add(ZSET_KEY, post.getId(), score);
            }

            log.info("热度计算完成 — 共 {} 篇帖子入 ZSet", posts.size());
        } catch (Exception e) {
            log.error("热度计算失败", e);
        }
    }

    private double computeScore(Integer likeCount, LocalDateTime createdAt, LocalDateTime now) {
        int p = likeCount != null ? likeCount : 0;
        double t = Duration.between(createdAt, now).toSeconds() / 3600.0;
        if (t < 0) t = 0;
        return (p - 1) / Math.pow(t + 2, GRAVITY);
    }
}
