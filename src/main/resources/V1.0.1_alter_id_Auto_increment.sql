--新增 forum_post的id自增
ALTER TABLE forum_post MODIFY COLUMN id INT AUTO_INCREMENT;
--新增 forum_board的id自增
ALTER TABLE forum_board MODIFY COLUMN id INT AUTO_INCREMENT;
--为forum_post增加tags
ALTER TABLE forum_post
    ADD COLUMN tags JSON NULL COMMENT '标签列表'
        AFTER content;