-- =====================================================
-- 考研交流论坛 - 测试数据 SQL (基于自增ID与新增 tags 字段)
-- =====================================================

-- ----------------------------
-- 1. 用户数据 (sys_user)
-- ----------------------------
INSERT INTO `sys_user` (`username`, `password`, `email`, `phone`, `role`, `avatar_url`, `target_major`, `points`, `is_deleted`) VALUES
                                                                                                                                    ('考研逆袭哥', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQV5eKlS0JQo8GZ9BZkYQ4X5YqUjEe', 'kaoyan@qq.com', '13800138001', 'USER', 'https://cdn.example.com/avatar/01.jpg', '计算机技术', 1250, 0),
                                                                                                                                    ('408学姐', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQV5eKlS0JQo8GZ9BZkYQ4X5YqUjEe', 'xuejie@163.com', '13800138002', 'MODERATOR', 'https://cdn.example.com/avatar/02.jpg', '计算机科学与技术', 4320, 0),
                                                                                                                                    ('数学小王子', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQV5eKlS0JQo8GZ9BZkYQ4X5YqUjEe', 'math@126.com', '13800138003', 'USER', 'https://cdn.example.com/avatar/03.jpg', '应用数学', 980, 0),
                                                                                                                                    ('英语困难户', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQV5eKlS0JQo8GZ9BZkYQ4X5YqUjEe', 'english@outlook.com', NULL, 'USER', NULL, '英语笔译', 320, 0),
                                                                                                                                    ('研途小助手', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQV5eKlS0JQo8GZ9BZkYQ4X5YqUjEe', 'admin@kaoyan.com', '13800138005', 'ADMIN', 'https://cdn.example.com/avatar/admin.jpg', '行政管理', 9999, 0),
                                                                                                                                    ('潜水观察员', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MrQV5eKlS0JQo8GZ9BZkYQ4X5YqUjEe', 'qianshui@test.com', NULL, 'USER', 'https://cdn.example.com/avatar/06.jpg', '电子信息', 450, 0);

-- ----------------------------
-- 2. 板块数据 (forum_board)
-- ----------------------------
INSERT INTO `forum_board` (`name`, `description`, `cover_url`, `post_count`, `is_deleted`) VALUES
                                                                                               ('408 计算机统考', '讨论计算机组成原理、数据结构、操作系统、计算机网络', 'https://cdn.example.com/board/408.jpg', 5, 0),
                                                                                               ('考研数学', '数学一/二/三 真题交流与答疑', 'https://cdn.example.com/board/math.jpg', 3, 0),
                                                                                               ('资料共享区', '历年真题、网课笔记、模拟试卷共享', 'https://cdn.example.com/board/share.jpg', 2, 0);

-- ----------------------------
-- 3. 帖子数据 (forum_post)
-- 注意: tags 字段为 JSON 数组，内容紧跟 content 之后
-- ----------------------------
INSERT INTO `forum_post` (`board_id`, `user_id`, `title`, `content`, `tags`, `attachment_urls`, `view_count`, `like_count`, `comment_count`, `is_deleted`) VALUES
                                                                                                                                                               (1, 1, '24考研408真题复盘，数据结构大题有点坑', '<p>大家觉得今年408的数据结构大题难吗？尤其是最后一道算法题，我感觉思路对了但代码没写完。大家来交流一下！</p><p><img src="https://cdn.example.com/post/408_q1.jpg" alt="题目截图"></p>', '["408", "数据结构", "真题"]', '["https://cdn.example.com/attach/408_answer.pdf"]', 1250, 32, 4, 0),
                                                                                                                                                               (1, 2, '【经验贴】408四本书复习顺序及时间规划', '<p>很多学弟学妹问复习顺序，我个人建议：数据结构 -> 计组 -> 操作系统 -> 计算机网络。理由是……</p>', '["经验", "规划", "408"]', NULL, 3420, 87, 6, 0),
                                                                                                                                                               (2, 3, '李永乐复习全书第六章多元函数微分错题求助', '<p>第6.23题，为什么答案用拉格朗日乘数法而不直接用隐函数求导？有没有大神帮忙解释一下？</p>', '["高数", "多元微分", "答疑"]', NULL, 520, 15, 3, 0),
                                                                                                                                                               (2, 1, '数学一模拟卷测评：合工大共创 vs 李林6套卷', '<p>今天刚做完这两套卷子，详细说一下我的感受。共创计算量偏大，李林题目新颖。建议基础好的做李林……</p>', '["模拟卷", "数学一", "测评"]', '["https://cdn.example.com/attach/math_mock_analysis.xlsx"]', 890, 28, 2, 0),
                                                                                                                                                               (3, 2, '【无水印】2025汤家凤高数辅导讲义 PDF', '<p>刚找到的高清版本，包含基础篇和强化篇，大家按需下载。</p>', '["资料", "高数", "汤家凤"]', '["https://cdn.example.com/resource/tang_gaoshu.pdf"]', 5670, 154, 5, 0),
                                                                                                                                                               (1, 4, '求助：计组 Cache 映射方式总是算错地址结构', '<p>每次遇到直接映射和组相联的地址划分就晕，求一套清晰的解题流程。</p>', '["计组", "Cache", "答疑"]', NULL, 230, 8, 2, 0),
                                                                                                                                                               (3, 5, '英语一 历年真题阅读手译本 PDF', '<p>自己整理的手译本，包含2010-2024年真题阅读逐句翻译和空格填写，适合基础薄弱的同学。</p>', '["英语", "真题", "手译"]', '["https://cdn.example.com/resource/eng_handwrite.pdf"]', 2310, 56, 1, 0),
                                                                                                                                                               (1, 6, '关于408要不要啃教材的讨论', '<p>看到很多经验贴说直接看王道，但也有大佬说操作系统必须看教材。大家怎么看？</p>', '["讨论", "教材", "王道"]', NULL, 150, 10, 1, 0),
                                                                                                                                                               (2, 2, '线代向量组线性相关性这块该怎么建立直观理解？', '<p>我知道秩的概念，但是遇到证明题还是懵。有没有推荐的视频或者理解角度？</p>', '["线代", "向量组", "答疑"]', NULL, 310, 12, 1, 0);

-- ----------------------------
-- 4. 帖子点赞数据 (forum_post_like)
-- ----------------------------
INSERT INTO `forum_post_like` (`post_id`, `user_id`) VALUES
                                                         (1, 2), (1, 3), (1, 4), (1, 5), (1, 6),  -- 帖子1被多人点赞
                                                         (2, 1), (2, 3), (2, 4), (2, 5), (2, 6),
                                                         (3, 1), (3, 2), (3, 4), (3, 5),
                                                         (4, 1), (4, 2), (4, 3), (4, 5),
                                                         (5, 1), (5, 2), (5, 3), (5, 4), (5, 6),
                                                         (6, 1), (6, 2),
                                                         (7, 1), (7, 2), (7, 3), (7, 6),
                                                         (8, 2), (8, 3),
                                                         (9, 1), (9, 3);

-- ----------------------------
-- 5. 评论数据 (forum_comment)
-- ----------------------------
INSERT INTO `forum_comment` (`post_id`, `user_id`, `reply_to_id`, `content`, `is_deleted`) VALUES
-- 帖子1的评论
(1, 2, NULL, '那道题确实是区分度很高的一题，我当时是画了树来辅助理解', 0),
(1, 3, 1, '学姐，画的是什么树？B树还是二叉树？', 0),
(1, 2, 2, '我画的是平衡二叉树，题目本质是考AVL的旋转', 0),
(1, 4, NULL, '大家都这么强吗，我直接放弃那道题了', 0),

-- 帖子2的评论
(2, 3, NULL, '学姐，数据结构一轮刷完大概要多久？', 0),
(2, 2, 5, '我当时全职备考，一个月左右刷完了王道单科', 0),
(2, 1, NULL, '建议早点开始，408内容真的多', 0),
(2, 5, 1, '跟着学姐走，有饭吃！', 0),

-- 帖子3的评论
(3, 2, NULL, '用拉格朗日是因为约束条件是曲线，隐函数求导在这里不好处理边界', 0),
(3, 1, 9, '谢谢学姐，我再去看看视频', 0),
(3, 3, NULL, '这题武忠祥老师讲过类似的例题，B站有切片', 0),

-- 帖子4的评论
(4, 2, NULL, '测评很客观，我目前也在做这两套', 0),
(4, 5, 12, '李林卷子在哪买的？', 0),

-- 帖子5的评论
(5, 1, NULL, '感谢分享，好人一生平安！', 0),
(5, 3, NULL, '请问有配套的习题册吗？', 0),
(5, 4, NULL, '资源质量很高，感谢学姐', 0),

-- 帖子6的评论
(6, 1, NULL, '我推荐你去看哈工大刘宏伟老师的MOOC，讲得很清楚', 0),
(6, 6, 17, '谢谢，我去搜搜看', 0),

-- 帖子7的评论
(7, 2, NULL, '这个手译本对语法基础差的很有帮助', 0),

-- 帖子8的评论
(8, 2, NULL, '操作系统推荐看教材，PV操作那里教材讲得更细', 0),

-- 帖子9的评论
(9, 1, NULL, '我推荐去看3Blue1Brown的线性代数本质系列，直观很多', 0);

-- ----------------------------
-- 6. 打卡记录 (interaction_check_in)
-- ----------------------------
INSERT INTO `interaction_check_in` (`user_id`, `study_hours`, `notes`, `created_date`) VALUES
                                                                                           (1, 8, '完成：408数据结构习题 + 英语阅读2篇', '2026-04-15'),
                                                                                           (1, 7, '数学660 50题 + 背单词1小时', '2026-04-16'),
                                                                                           (2, 10, '模考408 2018年真题，分数112，继续查漏补缺', '2026-04-15'),
                                                                                           (2, 9, '复习操作系统PV操作、计组Cache', '2026-04-16'),
                                                                                           (3, 6, '高数强化课两节 + 1800基础篇', '2026-04-15'),
                                                                                           (3, 8, '线代向量组练习题 + 英语长难句', '2026-04-16'),
                                                                                           (4, 5, '背单词200个，作文模板整理', '2026-04-15'),
                                                                                           (4, 6, '英语阅读2篇，正确率3/5，继续加油', '2026-04-16'),
                                                                                           (6, 9, '完成计算机网络第二章复习 + 王道课后题', '2026-04-15');

-- ----------------------------
-- 7. 资料文件 (resource_file)
-- ----------------------------
INSERT INTO `resource_file` (`board_id`, `uploader_id`, `title`, `description`, `file_url`, `file_type`, `file_size`, `download_count`, `is_deleted`) VALUES
                                                                                                                                                          (3, 2, '2025年408统考大纲 PDF', '官方发布的最新考试大纲，高清可打印', 'https://cdn.example.com/resource/408_2025_syllabus.pdf', 'pdf', 2456000, 1289, 0),
                                                                                                                                                          (3, 5, '英语二 真题词汇高频汇总', '根据近十年真题整理的高频词汇表，含例句', 'https://cdn.example.com/resource/eng2_vocab.xlsx', 'xlsx', 512000, 456, 0),
                                                                                                                                                          (2, 3, '张宇18讲 笔记整理版', '自己听课整理的笔记，重点题型归纳', 'https://cdn.example.com/resource/zhangyu18_note.pdf', 'pdf', 8930000, 789, 0),
                                                                                                                                                          (1, 1, '计算机网络 思维导图全集', '涵盖谢希仁第七版全部章节，结构清晰', 'https://cdn.example.com/resource/cn_mindmap.png', 'png', 4230000, 634, 0),
                                                                                                                                                          (3, 2, '2015-2024年 英语一真题 高清版', '无水印，直接打印使用', 'https://cdn.example.com/resource/eng1_2015_2024.zip', 'zip', 35000000, 2012, 0);