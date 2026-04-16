--新增 forum_post的id自增
ALTER TABLE forum_post MODIFY COLUMN id INT AUTO_INCREMENT;
--新增 forum_board的id自增
ALTER TABLE forum_board MODIFY COLUMN id INT AUTO_INCREMENT;