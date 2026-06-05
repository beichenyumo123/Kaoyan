mysqldump: [Warning] Using a password on the command line interface can be insecure.
-- MySQL dump 10.13  Distrib 9.7.0, for Linux (x86_64)
--
-- Host: localhost    Database: kaoyan_forum
-- ------------------------------------------------------
-- Server version	9.7.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `admission_record`
--

DROP TABLE IF EXISTS `admission_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admission_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '上岸用户ID',
  `school_id` bigint NOT NULL COMMENT '录取院校ID',
  `major_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '录取专业',
  `undergrad_school` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '本科院校',
  `undergrad_gpa` decimal(3,2) DEFAULT NULL COMMENT '本科GPA',
  `english_level` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '英语水平: CET4/CET6/TEM4/TEM8/NONE',
  `prep_duration` int DEFAULT NULL COMMENT '备考时长(月)',
  `mock_exam_score` int DEFAULT NULL COMMENT '模考平均分',
  `exam_score_total` int DEFAULT NULL COMMENT '考研总分',
  `exam_score_politics` int DEFAULT NULL COMMENT '政治',
  `exam_score_english` int DEFAULT NULL COMMENT '英语',
  `exam_score_biz1` int DEFAULT NULL COMMENT '业务课一',
  `exam_score_biz2` int DEFAULT NULL COMMENT '业务课二',
  `is_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已认证',
  `verification_status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED',
  `verified_at` datetime DEFAULT NULL COMMENT '认证通过时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_school_id` (`school_id`),
  KEY `idx_verified` (`is_verified`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上岸录取记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admission_record`
--

LOCK TABLES `admission_record` WRITE;
/*!40000 ALTER TABLE `admission_record` DISABLE KEYS */;
INSERT INTO `admission_record` VALUES (1,2,7,'计算机科学与技术','河南理工大学',3.10,'CET6',8,340,355,68,72,115,100,1,'APPROVED','2025-06-15 10:00:00',0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(2,3,5,'软件工程','武汉科技大学',3.50,'CET6',10,375,392,72,78,125,117,1,'APPROVED','2025-06-20 14:00:00',0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(3,4,3,'计算机科学与技术','南京邮电大学',3.70,'CET6',12,395,412,75,82,130,125,1,'APPROVED','2025-07-01 09:00:00',0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(4,5,9,'计算机应用技术','河南农业大学',2.90,'CET4',6,305,318,65,60,100,93,1,'APPROVED','2025-07-10 16:00:00',0,'2026-06-05 18:21:36','2026-06-05 18:21:36');
/*!40000 ALTER TABLE `admission_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_daily_task`
--

DROP TABLE IF EXISTS `ai_daily_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_daily_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `task_date` date NOT NULL COMMENT '任务日期',
  `task_content` varchar(512) NOT NULL COMMENT '任务内容(如: 刷10道链表真题)',
  `importance` varchar(20) DEFAULT 'MEDIUM' COMMENT '重要程度: HIGH, MEDIUM, LOW',
  `status` tinyint DEFAULT '0' COMMENT '完成状态: 0-未完成, 1-已完成',
  `agent_tips` varchar(512) DEFAULT NULL COMMENT '规划Agent特意留下的叮嘱',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_date` (`user_id`,`task_date`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能每日任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_daily_task`
--

LOCK TABLES `ai_daily_task` WRITE;
/*!40000 ALTER TABLE `ai_daily_task` DISABLE KEYS */;
INSERT INTO `ai_daily_task` VALUES (1,8,'2026-06-04','制定考研学习计划，包括科目优先级和每日时间安排','HIGH',1,'根据你的目标院校和专业，先确定重点科目，如数学和英语，并规划每周学习进度，避免过度学习导致疲劳。','2026-06-04 06:35:23'),(2,8,'2026-06-04','开始考研英语基础词汇学习，每天记忆50个新词并复习','HIGH',1,'使用背单词APP或词汇书，结合例句和发音，利用记忆曲线定期复习，打牢语言基础。','2026-06-04 06:35:23'),(3,8,'2026-06-04','反思今日学习8小时的效果，记录学习日志和难点','MEDIUM',1,'简单总结学习内容，标记不理解的部分，为明天计划提供参考，培养自我监控习惯。','2026-06-04 06:35:23');
/*!40000 ALTER TABLE `ai_daily_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_intervention_log`
--

DROP TABLE IF EXISTS `ai_intervention_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_intervention_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `agent_name` varchar(50) NOT NULL COMMENT '智能体名字: Planner, Counselor, Sentry, Reviewer',
  `trigger_reason` varchar(255) NOT NULL COMMENT '触发介入的原因(如: 连续断签2天)',
  `intervention_content` text NOT NULL COMMENT '主动发送给用户的安抚/警告话术',
  `user_reaction` varchar(100) DEFAULT 'UNREAD' COMMENT '用户反应: UNREAD, READ, ACKNOWLEDGED(已采纳)',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_agent` (`user_id`,`agent_name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能体主动干预日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_intervention_log`
--

LOCK TABLES `ai_intervention_log` WRITE;
/*!40000 ALTER TABLE `ai_intervention_log` DISABLE KEYS */;
INSERT INTO `ai_intervention_log` VALUES (1,8,'Psychology','打卡感言分析（连续1天）','【AI 调用失败】401 Authorization Required: [no body]','READ','2026-05-21 07:07:30'),(2,8,'Psychology','打卡感言分析（连续1天）','你的状态真好，这份从容和舒适感，正是高效复习的最佳基础呢！','UNREAD','2026-06-04 06:35:12');
/*!40000 ALTER TABLE `ai_intervention_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_knowledge_point`
--

DROP TABLE IF EXISTS `ai_knowledge_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_knowledge_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `subject` varchar(64) NOT NULL COMMENT '学科（数据结构/操作系统/计算机网络/计算机组成原理/高等数学/线性代数/概率论/英语/政治）',
  `chapter` varchar(128) DEFAULT NULL COMMENT '章节',
  `title` varchar(256) NOT NULL COMMENT '知识点标题',
  `content` text NOT NULL COMMENT '知识点详细内容',
  `keywords` varchar(512) DEFAULT NULL COMMENT '关键词，逗号分隔',
  `importance` varchar(16) DEFAULT 'MEDIUM' COMMENT '重要程度 HIGH/MEDIUM/LOW',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_subject` (`subject`),
  FULLTEXT KEY `ft_keywords` (`keywords`) /*!50100 WITH PARSER `ngram` */ 
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考研知识点库';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_knowledge_point`
--

LOCK TABLES `ai_knowledge_point` WRITE;
/*!40000 ALTER TABLE `ai_knowledge_point` DISABLE KEYS */;
INSERT INTO `ai_knowledge_point` VALUES (1,'数据结构','树','B树与B+树的区别','1. B树所有节点都存储数据，B+树只有叶子节点存储数据；\n2. B+树叶节点通过链表相连，便于范围查询；\n3. B+树的非叶子节点仅存储索引，单个节点可容纳更多key，树高更低；\n4. B树适合单点查询，B+树适合范围查询和顺序遍历；\n5. MySQL InnoDB 使用 B+树作为索引结构。','B树,B+树,索引,数据库,范围查询,叶子节点','HIGH','2026-06-04 13:13:33'),(2,'数据结构','排序','快速排序的原理与复杂度','快速排序采用分治策略：选择基准元素pivot，将数组分为两部分（小于pivot和大于pivot），递归排序。\n时间复杂度：平均O(nlogn)，最坏O(n²)（已排序数组+选首元素为pivot）。\n空间复杂度：O(logn)（递归栈）。\n不稳定排序。\n优化：三数取中选pivot、小区间用插入排序。','快速排序,分治,时间复杂度,不稳定排序,递归','HIGH','2026-06-04 13:13:33'),(3,'数据结构','图','Dijkstra最短路径算法','Dijkstra算法求解单源最短路径，适用于非负权边的有向/无向图。\n核心思想：贪心，每次选取未访问的距离最小节点，更新其邻居的距离。\n时间复杂度：O(V²)（朴素），O((V+E)logV)（优先队列优化）。\n不能处理负权边（需用Bellman-Ford）。','Dijkstra,最短路径,贪心,优先队列,负权边','HIGH','2026-06-04 13:13:33'),(4,'数据结构','线性表','栈和队列的应用场景','栈：函数调用栈、表达式求值（后缀表达式）、括号匹配、浏览器前进后退、DFS。\n队列：BFS、消息队列、缓冲区、CPU任务调度。\n循环队列判满：(rear+1)%maxSize == front。\n双端队列deque两端都可进出。','栈,队列,DFS,BFS,循环队列,函数调用','MEDIUM','2026-06-04 13:13:33'),(5,'操作系统','进程管理','进程与线程的区别','1. 进程是资源分配的基本单位，线程是CPU调度的基本单位；\n2. 进程有独立地址空间，线程共享进程的地址空间；\n3. 进程切换开销大（需切换页表等），线程切换开销小；\n4. 线程间通信可直接读写共享变量，进程间通信需要IPC机制；\n5. 一个进程崩溃不影响其他进程，一个线程崩溃可能导致整个进程崩溃。','进程,线程,地址空间,上下文切换,IPC','HIGH','2026-06-04 13:13:33'),(6,'操作系统','内存管理','虚拟内存与页面置换算法','虚拟内存将逻辑地址与物理地址分离，允许进程使用大于物理内存的地址空间。\n页面置换算法：\n1. FIFO：先进先出，可能Belady异常；\n2. LRU：最近最久未使用，用栈或计数器实现；\n3. Clock（时钟）：LRU的近似，用访问位；\n4. OPT：理论最优，无法实现（需预知未来）。\n缺页率 = 缺页次数 / 总访问次数。','虚拟内存,页面置换,FIFO,LRU,Clock,缺页中断','HIGH','2026-06-04 13:13:33'),(7,'操作系统','同步互斥','死锁的四个必要条件与预防','死锁四个必要条件：互斥、占有并等待、不可剥夺、循环等待。\n预防策略：\n1. 破坏占有并等待：一次性申请所有资源；\n2. 破坏不可剥夺：允许抢占；\n3. 破坏循环等待：按序申请资源。\n银行家算法用于死锁避免（安全性检查）。','死锁,互斥,银行家算法,循环等待,资源分配','HIGH','2026-06-04 13:13:33'),(8,'计算机网络','传输层','TCP三次握手与四次挥手','三次握手：\n1. 客户端→SYN→服务器；2. 服务器→SYN+ACK→客户端；3. 客户端→ACK→服务器。\n为什么三次：防止历史连接被服务器接受。\n四次挥手：\n1. 主动方→FIN；2. 被动方→ACK；3. 被动方→FIN；4. 主动方→ACK。\nTIME_WAIT等待2MSL确保对方收到最后的ACK。\n为什么四次：TCP全双工，每个方向需单独关闭。','TCP,三次握手,四次挥手,SYN,FIN,TIME_WAIT,全双工','HIGH','2026-06-04 13:13:33'),(9,'计算机网络','应用层','HTTP与HTTPS的区别','HTTP：明文传输，端口80，无加密。\nHTTPS = HTTP + TLS/SSL，端口443。\nHTTPS握手过程：\n1. 客户端发送支持的加密套件列表；\n2. 服务器返回证书+选定加密套件；\n3. 客户端验证证书，生成随机密钥，用服务器公钥加密发送；\n4. 双方用该密钥对称加密通信。\n对称加密用于数据传输，非对称加密用于密钥交换。','HTTP,HTTPS,TLS,SSL,对称加密,非对称加密,证书','MEDIUM','2026-06-04 13:13:33'),(10,'计算机组成原理','存储系统','Cache的工作原理与映射方式','Cache解决CPU与主存速度不匹配问题。\n映射方式：\n1. 直接映射：每个主存块只能映射到Cache的固定位置，冲突率高；\n2. 全相联映射：可映射到任意位置，命中率高但硬件复杂；\n3. 组相联映射：折中方案，Cache分组，组内全相联。\n替换算法：LRU、FIFO、随机。\n写策略：写直达（同时写Cache和主存）、写回（只写Cache，脏位标记）。','Cache,直接映射,全相联,组相联,LRU,写回,写直达','HIGH','2026-06-04 13:13:33'),(11,'计算机组成原理','指令系统','CISC与RISC的区别','CISC（复杂指令集）：指令数量多、长度可变、执行周期长、微程序控制。代表：x86。\nRISC（精简指令集）：指令数量少、长度固定、单周期执行、硬布线控制、大量通用寄存器。代表：ARM、RISC-V。\nRISC特点：Load/Store架构、流水线效率高、编译器优化空间大。','CISC,RISC,指令集,x86,ARM,流水线,Load/Store','MEDIUM','2026-06-04 13:13:33'),(12,'高等数学','微积分','洛必达法则的使用条件','洛必达法则用于求0/0或无穷/无穷型不定式极限。\n条件：\n1. lim f(x)/g(x) 为0/0或无穷/无穷型；\n2. f(x)和g(x)在去心邻域内可导；\n3. g\'(x) 不等于 0；\n4. lim f\'(x)/g\'(x) 存在（或为无穷）。\n注意：使用前必须验证是不定型，否则会得出错误结果。多次使用需每次验证。','洛必达法则,极限,不定式,0/0,导数','HIGH','2026-06-04 13:13:33'),(13,'高等数学','级数','泰勒展开公式','f(x)在x₀处的泰勒展开：\nf(x) = Σ f⁽ⁿ⁾(x₀)/n! · (x-x₀)ⁿ\n常用展开（x₀=0，即麦克劳林展开）：\ne^x = 1 + x + x²/2! + x³/3! + ...\nsin x = x - x³/3! + x⁵/5! - ...\ncos x = 1 - x²/2! + x⁴/4! - ...\nln(1+x) = x - x²/2 + x³/3 - ... (|x|≤1, x≠-1)\n1/(1-x) = 1 + x + x² + x³ + ... (|x|<1)','泰勒展开,麦克劳林,级数,e^x,sin,cos,ln','HIGH','2026-06-04 13:13:33'),(14,'线性代数','矩阵','矩阵的秩与线性方程组解的关系','对于m×n矩阵A：\n1. r(A) = n（列满秩）→ 齐次方程Ax=0只有零解；\n2. r(A) < n → 齐次方程有非零解，基础解系含n-r(A)个向量；\n3. 非齐次方程Ax=b有解 ⟺ r(A) = r(A|b)；\n4. r(A) = r(A|b) = n → 唯一解；\n5. r(A) = r(A|b) < n → 无穷多解。','矩阵的秩,线性方程组,基础解系,列满秩,增广矩阵','HIGH','2026-06-04 13:13:33'),(15,'概率论','随机变量','常见概率分布及其期望方差','离散型：\n1. 二项分布B(n,p)：E=np, D=np(1-p)\n2. 泊松分布P(λ)：E=λ, D=λ\n3. 几何分布G(p)：E=1/p, D=(1-p)/p²\n连续型：\n1. 均匀分布U(a,b)：E=(a+b)/2, D=(b-a)²/12\n2. 指数分布Exp(λ)：E=1/λ, D=1/λ²\n3. 正态分布N(μ,σ²)：E=μ, D=σ²','二项分布,泊松分布,正态分布,期望,方差,概率密度','HIGH','2026-06-04 13:13:33'),(16,'英语','阅读理解','考研英语阅读常见题型与解题技巧','主旨题：找首段、末段、各段首句，注意转折词but/however。\n细节题：定位关键词，答案通常是原文同义替换。\n推断题：注意infer/imply/suggest，答案不会太绝对。\n态度题：注意作者用词的感情色彩。\n词义题：结合上下文语境推断，注意前后解释说明。\n排除法：绝对化选项（all/never/must）通常是干扰项。','阅读理解,主旨题,细节题,推断题,同义替换,排除法','MEDIUM','2026-06-04 13:13:33'),(17,'政治','马克思主义原理','唯物辩证法三大规律','1. 对立统一规律（核心）：矛盾是事物发展的根本动力，矛盾的普遍性与特殊性辩证统一。\n2. 量变质变规律：量变是质变的必要准备，质变是量变的必然结果，量变引起质变，质变巩固量变。\n3. 否定之否定规律：事物发展是螺旋式上升、波浪式前进，经历\"肯定—否定—否定之否定\"。\n方法论：具体问题具体分析、两点论与重点论统一。','唯物辩证法,对立统一,量变质变,否定之否定,矛盾,方法论','HIGH','2026-06-04 13:13:33'),(18,'数据结构','树','B树与B+树的区别','1. B树所有节点都存储数据，B+树只有叶子节点存储数据；\n2. B+树叶节点通过链表相连，便于范围查询；\n3. B+树的非叶子节点仅存储索引，单个节点可容纳更多key，树高更低；\n4. B树适合单点查询，B+树适合范围查询和顺序遍历；\n5. MySQL InnoDB 使用 B+树作为索引结构。','B树,B+树,索引,数据库,范围查询,叶子节点','HIGH','2026-06-04 15:55:19'),(19,'数据结构','排序','快速排序的原理与复杂度','快速排序采用分治策略：选择基准元素pivot，将数组分为两部分（小于pivot和大于pivot），递归排序。\n时间复杂度：平均O(nlogn)，最坏O(n²)（已排序数组+选首元素为pivot）。\n空间复杂度：O(logn)（递归栈）。\n不稳定排序。\n优化：三数取中选pivot、小区间用插入排序。','快速排序,分治,时间复杂度,不稳定排序,递归','HIGH','2026-06-04 15:55:19'),(20,'数据结构','图','Dijkstra最短路径算法','Dijkstra算法求解单源最短路径，适用于非负权边的有向/无向图。\n核心思想：贪心，每次选取未访问的距离最小节点，更新其邻居的距离。\n时间复杂度：O(V²)（朴素），O((V+E)logV)（优先队列优化）。\n不能处理负权边（需用Bellman-Ford）。','Dijkstra,最短路径,贪心,优先队列,负权边','HIGH','2026-06-04 15:55:19'),(21,'数据结构','线性表','栈和队列的应用场景','栈：函数调用栈、表达式求值（后缀表达式）、括号匹配、浏览器前进后退、DFS。\n队列：BFS、消息队列、缓冲区、CPU任务调度。\n循环队列判满：(rear+1)%maxSize == front。\n双端队列deque两端都可进出。','栈,队列,DFS,BFS,循环队列,函数调用','MEDIUM','2026-06-04 15:55:19'),(22,'操作系统','进程管理','进程与线程的区别','1. 进程是资源分配的基本单位，线程是CPU调度的基本单位；\n2. 进程有独立地址空间，线程共享进程的地址空间；\n3. 进程切换开销大（需切换页表等），线程切换开销小；\n4. 线程间通信可直接读写共享变量，进程间通信需要IPC机制；\n5. 一个进程崩溃不影响其他进程，一个线程崩溃可能导致整个进程崩溃。','进程,线程,地址空间,上下文切换,IPC','HIGH','2026-06-04 15:55:19'),(23,'操作系统','内存管理','虚拟内存与页面置换算法','虚拟内存将逻辑地址与物理地址分离，允许进程使用大于物理内存的地址空间。\n页面置换算法：\n1. FIFO：先进先出，可能Belady异常；\n2. LRU：最近最久未使用，用栈或计数器实现；\n3. Clock（时钟）：LRU的近似，用访问位；\n4. OPT：理论最优，无法实现（需预知未来）。\n缺页率 = 缺页次数 / 总访问次数。','虚拟内存,页面置换,FIFO,LRU,Clock,缺页中断','HIGH','2026-06-04 15:55:19'),(24,'操作系统','同步互斥','死锁的四个必要条件与预防','死锁四个必要条件：互斥、占有并等待、不可剥夺、循环等待。\n预防策略：\n1. 破坏占有并等待：一次性申请所有资源；\n2. 破坏不可剥夺：允许抢占；\n3. 破坏循环等待：按序申请资源。\n银行家算法用于死锁避免（安全性检查）。','死锁,互斥,银行家算法,循环等待,资源分配','HIGH','2026-06-04 15:55:19'),(25,'计算机网络','传输层','TCP三次握手与四次挥手','三次握手：\n1. 客户端→SYN→服务器；2. 服务器→SYN+ACK→客户端；3. 客户端→ACK→服务器。\n为什么三次：防止历史连接被服务器接受。\n四次挥手：\n1. 主动方→FIN；2. 被动方→ACK；3. 被动方→FIN；4. 主动方→ACK。\nTIME_WAIT等待2MSL确保对方收到最后的ACK。\n为什么四次：TCP全双工，每个方向需单独关闭。','TCP,三次握手,四次挥手,SYN,FIN,TIME_WAIT,全双工','HIGH','2026-06-04 15:55:19'),(26,'计算机网络','应用层','HTTP与HTTPS的区别','HTTP：明文传输，端口80，无加密。\nHTTPS = HTTP + TLS/SSL，端口443。\nHTTPS握手过程：\n1. 客户端发送支持的加密套件列表；\n2. 服务器返回证书+选定加密套件；\n3. 客户端验证证书，生成随机密钥，用服务器公钥加密发送；\n4. 双方用该密钥对称加密通信。\n对称加密用于数据传输，非对称加密用于密钥交换。','HTTP,HTTPS,TLS,SSL,对称加密,非对称加密,证书','MEDIUM','2026-06-04 15:55:19'),(27,'计算机组成原理','存储系统','Cache的工作原理与映射方式','Cache解决CPU与主存速度不匹配问题。\n映射方式：\n1. 直接映射：每个主存块只能映射到Cache的固定位置，冲突率高；\n2. 全相联映射：可映射到任意位置，命中率高但硬件复杂；\n3. 组相联映射：折中方案，Cache分组，组内全相联。\n替换算法：LRU、FIFO、随机。\n写策略：写直达（同时写Cache和主存）、写回（只写Cache，脏位标记）。','Cache,直接映射,全相联,组相联,LRU,写回,写直达','HIGH','2026-06-04 15:55:19'),(28,'计算机组成原理','指令系统','CISC与RISC的区别','CISC（复杂指令集）：指令数量多、长度可变、执行周期长、微程序控制。代表：x86。\nRISC（精简指令集）：指令数量少、长度固定、单周期执行、硬布线控制、大量通用寄存器。代表：ARM、RISC-V。\nRISC特点：Load/Store架构、流水线效率高、编译器优化空间大。','CISC,RISC,指令集,x86,ARM,流水线,Load/Store','MEDIUM','2026-06-04 15:55:19'),(29,'高等数学','微积分','洛必达法则的使用条件','洛必达法则用于求0/0或无穷/无穷型不定式极限。\n条件：\n1. lim f(x)/g(x) 为0/0或无穷/无穷型；\n2. f(x)和g(x)在去心邻域内可导；\n3. g\'(x) 不等于 0；\n4. lim f\'(x)/g\'(x) 存在（或为无穷）。\n注意：使用前必须验证是不定型，否则会得出错误结果。多次使用需每次验证。','洛必达法则,极限,不定式,0/0,导数','HIGH','2026-06-04 15:55:19'),(30,'高等数学','级数','泰勒展开公式','f(x)在x₀处的泰勒展开：\nf(x) = Σ f⁽ⁿ⁾(x₀)/n! · (x-x₀)ⁿ\n常用展开（x₀=0，即麦克劳林展开）：\ne^x = 1 + x + x²/2! + x³/3! + ...\nsin x = x - x³/3! + x⁵/5! - ...\ncos x = 1 - x²/2! + x⁴/4! - ...\nln(1+x) = x - x²/2 + x³/3 - ... (|x|≤1, x≠-1)\n1/(1-x) = 1 + x + x² + x³ + ... (|x|<1)','泰勒展开,麦克劳林,级数,e^x,sin,cos,ln','HIGH','2026-06-04 15:55:19'),(31,'线性代数','矩阵','矩阵的秩与线性方程组解的关系','对于m×n矩阵A：\n1. r(A) = n（列满秩）→ 齐次方程Ax=0只有零解；\n2. r(A) < n → 齐次方程有非零解，基础解系含n-r(A)个向量；\n3. 非齐次方程Ax=b有解 ⟺ r(A) = r(A|b)；\n4. r(A) = r(A|b) = n → 唯一解；\n5. r(A) = r(A|b) < n → 无穷多解。','矩阵的秩,线性方程组,基础解系,列满秩,增广矩阵','HIGH','2026-06-04 15:55:19'),(32,'概率论','随机变量','常见概率分布及其期望方差','离散型：\n1. 二项分布B(n,p)：E=np, D=np(1-p)\n2. 泊松分布P(λ)：E=λ, D=λ\n3. 几何分布G(p)：E=1/p, D=(1-p)/p²\n连续型：\n1. 均匀分布U(a,b)：E=(a+b)/2, D=(b-a)²/12\n2. 指数分布Exp(λ)：E=1/λ, D=1/λ²\n3. 正态分布N(μ,σ²)：E=μ, D=σ²','二项分布,泊松分布,正态分布,期望,方差,概率密度','HIGH','2026-06-04 15:55:19'),(33,'英语','阅读理解','考研英语阅读常见题型与解题技巧','主旨题：找首段、末段、各段首句，注意转折词but/however。\n细节题：定位关键词，答案通常是原文同义替换。\n推断题：注意infer/imply/suggest，答案不会太绝对。\n态度题：注意作者用词的感情色彩。\n词义题：结合上下文语境推断，注意前后解释说明。\n排除法：绝对化选项（all/never/must）通常是干扰项。','阅读理解,主旨题,细节题,推断题,同义替换,排除法','MEDIUM','2026-06-04 15:55:19'),(34,'政治','马克思主义原理','唯物辩证法三大规律','1. 对立统一规律（核心）：矛盾是事物发展的根本动力，矛盾的普遍性与特殊性辩证统一。\n2. 量变质变规律：量变是质变的必要准备，质变是量变的必然结果，量变引起质变，质变巩固量变。\n3. 否定之否定规律：事物发展是螺旋式上升、波浪式前进，经历\"肯定—否定—否定之否定\"。\n方法论：具体问题具体分析、两点论与重点论统一。','唯物辩证法,对立统一,量变质变,否定之否定,矛盾,方法论','HIGH','2026-06-04 15:55:19');
/*!40000 ALTER TABLE `ai_knowledge_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_report`
--

DROP TABLE IF EXISTS `ai_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `week_start` date NOT NULL COMMENT '周报周期起始日（周一）',
  `week_end` date NOT NULL COMMENT '周报周期结束日（周日）',
  `markdown` text NOT NULL COMMENT '周报 Markdown 内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_week` (`user_id`,`week_start`),
  KEY `idx_user_created` (`user_id`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI 周报';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_report`
--

LOCK TABLES `ai_report` WRITE;
/*!40000 ALTER TABLE `ai_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_user_event`
--

DROP TABLE IF EXISTS `ai_user_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_user_event` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `event_type` varchar(32) NOT NULL COMMENT '事件类型 VIEW_POST/COLLECT_POST/SEARCH/LIKE_POST',
  `event_data` json DEFAULT NULL COMMENT '事件数据：{"postId":123,"boardId":2,"keyword":"B树","duration":120}',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`user_id`,`event_type`),
  KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为事件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_user_event`
--

LOCK TABLES `ai_user_event` WRITE;
/*!40000 ALTER TABLE `ai_user_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_user_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_group`
--

DROP TABLE IF EXISTS `chat_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_group` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '群简介',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '群头像URL',
  `owner_id` bigint NOT NULL COMMENT '群主用户ID',
  `member_count` int NOT NULL DEFAULT '1' COMMENT '冗余：当前群成员数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_owner_id` (`owner_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='群组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_group`
--

LOCK TABLES `chat_group` WRITE;
/*!40000 ALTER TABLE `chat_group` DISABLE KEYS */;
INSERT INTO `chat_group` VALUES (1,'408考研交流群','一起刷题讨论408',NULL,6,4,0,'2026-05-20 12:34:35','2026-05-20 12:34:35'),(2,'测试','',NULL,11,1,0,'2026-05-20 14:36:28','2026-05-20 14:36:28'),(3,'测试2','',NULL,8,1,0,'2026-05-20 15:16:22','2026-05-20 15:16:22');
/*!40000 ALTER TABLE `chat_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experience_post`
--

DROP TABLE IF EXISTS `experience_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `experience_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '作者ID，关联 sys_user.id',
  `forum_post_id` bigint DEFAULT NULL COMMENT '关联论坛帖子ID（null=独立经验贴）',
  `undergrad_school` varchar(100) NOT NULL COMMENT '本科院校',
  `undergrad_major` varchar(100) NOT NULL COMMENT '本科专业',
  `is_cross_major` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否跨考: 0=否, 1=是',
  `is_second_attempt` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否二战: 0=否, 1=是',
  `target_school` varchar(100) NOT NULL COMMENT '目标院校',
  `target_major` varchar(100) NOT NULL COMMENT '目标专业',
  `initial_exam_total` decimal(5,1) DEFAULT NULL COMMENT '初试总分',
  `initial_exam_politics` decimal(4,1) DEFAULT NULL COMMENT '政治',
  `initial_exam_english` decimal(4,1) DEFAULT NULL COMMENT '英语',
  `initial_exam_math` decimal(4,1) DEFAULT NULL COMMENT '数学',
  `initial_exam_major` decimal(4,1) DEFAULT NULL COMMENT '专业课',
  `re_exam_score` decimal(5,1) DEFAULT NULL COMMENT '复试分',
  `timeline_json` json DEFAULT NULL COMMENT '备考时间线 [{"phase":"基础","start":"3月","end":"6月","desc":"..."}]',
  `books_json` json DEFAULT NULL COMMENT '用书推荐 [{"subject":"数学","name":"复习全书","rating":5}]',
  `tips` text COMMENT '备考心得/建议',
  `is_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '发布时作者是否已认证（快照）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0=草稿, 1=已发布',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览数',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `collect_count` int NOT NULL DEFAULT '0' COMMENT '收藏数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_forum_post_id` (`forum_post_id`),
  KEY `idx_target_school_major` (`target_school`,`target_major`),
  KEY `idx_undergrad_school` (`undergrad_school`),
  KEY `idx_is_verified` (`is_verified`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='结构化经验贴表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experience_post`
--

LOCK TABLES `experience_post` WRITE;
/*!40000 ALTER TABLE `experience_post` DISABLE KEYS */;
INSERT INTO `experience_post` VALUES (1,3,NULL,'郑州大学','软件工程',0,0,'浙江大学','计算机科学与技术',385.0,72.0,78.0,125.0,110.0,88.5,'[{\"phase\": \"基础阶段\", \"endDate\": \"6月\", \"startDate\": \"3月\", \"description\": \"过完408四门课教材第一遍，英语每天背100个单词\"}, {\"phase\": \"强化阶段\", \"endDate\": \"9月\", \"startDate\": \"7月\", \"description\": \"王道408全套刷题，数学660+880，政治开始看徐涛\"}, {\"phase\": \"冲刺阶段\", \"endDate\": \"12月\", \"startDate\": \"10月\", \"description\": \"真题模拟，408真题刷3遍，英语作文模板整理\"}]','[{\"name\": \"王道数据结构\", \"rating\": 5, \"subject\": \"数据结构\"}, {\"name\": \"王道计组\", \"rating\": 4, \"subject\": \"计算机组成原理\"}, {\"name\": \"王道操作系统\", \"rating\": 5, \"subject\": \"操作系统\"}, {\"name\": \"王道计网\", \"rating\": 4, \"subject\": \"计算机网络\"}, {\"name\": \"李永乐660题\", \"rating\": 5, \"subject\": \"数学\"}, {\"name\": \"肖秀荣1000题\", \"rating\": 5, \"subject\": \"政治\"}]','408复习的核心是反复刷真题，尤其是计组和操作系统的综合题。建议暑假结束前完成第一轮全面复习，9月后集中刷真题。不要买太多辅导书，王道+真题足够。数学每天保持3小时以上做题量，保持手感很重要。',1,1,3256,4,3,0,'2026-06-04 19:48:42','2026-06-04 19:48:42'),(2,6,NULL,'河南大学','计算机科学与技术',0,0,'哈尔滨工业大学','计算机技术',395.0,75.0,82.0,130.0,108.0,90.0,'[{\"phase\": \"基础阶段\", \"endDate\": \"6月\", \"startDate\": \"3月\", \"description\": \"教材+王道基础，每天单词+数学\"}, {\"phase\": \"强化阶段\", \"endDate\": \"9月\", \"startDate\": \"7月\", \"description\": \"王道强化课+李永乐线代+英语真题\"}, {\"phase\": \"冲刺阶段\", \"endDate\": \"12月\", \"startDate\": \"10月\", \"description\": \"真题模拟+查漏补缺\"}]','[{\"name\": \"王道数据结构\", \"rating\": 5, \"subject\": \"数据结构\"}, {\"name\": \"张宇30讲\", \"rating\": 5, \"subject\": \"数学\"}, {\"name\": \"李永乐线代\", \"rating\": 5, \"subject\": \"数学\"}]','哈工大计科复试很看重机试，建议提前刷LeetCode。初试408一定要拿到120+才有竞争力。政治不用开始太早，9月开始跟徐涛强化课即可。',1,1,2180,3,2,0,'2026-06-04 19:48:42','2026-06-04 19:48:42'),(3,4,NULL,'河南科技大学','英语',0,1,'北京外国语大学','英语笔译',378.0,70.0,88.0,NULL,115.0,85.0,'[{\"phase\": \"一战失败复盘\", \"endDate\": \"5月\", \"startDate\": \"3月\", \"description\": \"分析一战失利原因，重新制定计划\"}, {\"phase\": \"系统复习\", \"endDate\": \"10月\", \"startDate\": \"6月\", \"description\": \"翻译基础+百科知识+政治+二外日语\"}, {\"phase\": \"冲刺\", \"endDate\": \"12月\", \"startDate\": \"11月\", \"description\": \"真题模拟+作文模板\"}]','[{\"name\": \"张培基散文翻译\", \"rating\": 5, \"subject\": \"翻译基础\"}, {\"name\": \"中国文化读本\", \"rating\": 4, \"subject\": \"百科\"}, {\"name\": \"肖秀荣精讲精练\", \"rating\": 5, \"subject\": \"政治\"}]','二战最重要的是心态调整。不要因为一战失利就否定自己，分析清楚薄弱点再出发。北外笔译特别看重翻译基本功，每天至少练一篇英译汉和汉译英。',0,1,1890,3,1,0,'2026-06-04 19:48:42','2026-06-04 19:48:42'),(4,5,NULL,'河南师范大学','思想政治教育',0,0,'武汉大学','马克思主义理论',390.0,82.0,72.0,NULL,120.0,92.0,'[{\"phase\": \"基础阶段\", \"endDate\": \"8月\", \"startDate\": \"7月\", \"description\": \"徐涛强化课+精讲精练\"}, {\"phase\": \"背诵阶段\", \"endDate\": \"10月\", \"startDate\": \"9月\", \"description\": \"背诵手册+1000题二刷\"}, {\"phase\": \"冲刺阶段\", \"endDate\": \"12月\", \"startDate\": \"11月\", \"description\": \"肖八肖四+徐涛20题\"}]','[{\"name\": \"肖秀荣精讲精练\", \"rating\": 5, \"subject\": \"政治\"}, {\"name\": \"徐涛背诵手册\", \"rating\": 5, \"subject\": \"政治\"}, {\"name\": \"肖四肖八\", \"rating\": 5, \"subject\": \"政治\"}]','政治不用太早开始，但一定要认真对待。1000题建议刷两遍，错题重点标记。肖四一到手立刻开始背，每天背2-3小时。考研政治大题基本都在肖四范围内。',0,1,1560,2,2,0,'2026-06-04 19:48:42','2026-06-04 19:48:42');
/*!40000 ALTER TABLE `experience_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `experience_post_collect`
--

DROP TABLE IF EXISTS `experience_post_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `experience_post_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `experience_id` bigint NOT NULL COMMENT '经验贴ID',
  `user_id` bigint NOT NULL COMMENT '收藏用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_experience_user` (`experience_id`,`user_id`),
  KEY `idx_experience_id` (`experience_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经验贴收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experience_post_collect`
--

LOCK TABLES `experience_post_collect` WRITE;
/*!40000 ALTER TABLE `experience_post_collect` DISABLE KEYS */;
INSERT INTO `experience_post_collect` VALUES (1,1,4,'2026-06-04 19:48:42'),(2,1,5,'2026-06-04 19:48:42'),(3,1,7,'2026-06-04 19:48:42'),(4,2,3,'2026-06-04 19:48:42'),(5,2,5,'2026-06-04 19:48:42'),(6,3,5,'2026-06-04 19:48:42'),(7,4,3,'2026-06-04 19:48:42'),(8,4,6,'2026-06-04 19:48:42');
/*!40000 ALTER TABLE `experience_post_collect` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_experience_collect_insert` AFTER INSERT ON `experience_post_collect` FOR EACH ROW BEGIN
    UPDATE experience_post SET collect_count = collect_count + 1 WHERE id = NEW.experience_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_experience_collect_delete` AFTER DELETE ON `experience_post_collect` FOR EACH ROW BEGIN
    UPDATE experience_post SET collect_count = collect_count - 1 WHERE id = OLD.experience_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `experience_post_like`
--

DROP TABLE IF EXISTS `experience_post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `experience_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `experience_id` bigint NOT NULL COMMENT '经验贴ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_experience_user` (`experience_id`,`user_id`),
  KEY `idx_experience_id` (`experience_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经验贴点赞表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `experience_post_like`
--

LOCK TABLES `experience_post_like` WRITE;
/*!40000 ALTER TABLE `experience_post_like` DISABLE KEYS */;
INSERT INTO `experience_post_like` VALUES (1,1,4,'2026-06-04 19:48:42'),(2,1,5,'2026-06-04 19:48:42'),(3,1,6,'2026-06-04 19:48:42'),(4,1,7,'2026-06-04 19:48:42'),(5,2,3,'2026-06-04 19:48:42'),(6,2,4,'2026-06-04 19:48:42'),(7,2,5,'2026-06-04 19:48:42'),(8,3,3,'2026-06-04 19:48:42'),(9,3,5,'2026-06-04 19:48:42'),(10,3,6,'2026-06-04 19:48:42'),(11,4,3,'2026-06-04 19:48:42'),(12,4,4,'2026-06-04 19:48:42');
/*!40000 ALTER TABLE `experience_post_like` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_experience_like_insert` AFTER INSERT ON `experience_post_like` FOR EACH ROW BEGIN
    UPDATE experience_post SET like_count = like_count + 1 WHERE id = NEW.experience_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_experience_like_delete` AFTER DELETE ON `experience_post_like` FOR EACH ROW BEGIN
    UPDATE experience_post SET like_count = like_count - 1 WHERE id = OLD.experience_id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `forum_board`
--

DROP TABLE IF EXISTS `forum_board`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_board` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '板块名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '板块简介',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '板块封面图',
  `post_count` bigint NOT NULL DEFAULT '0' COMMENT '冗余: 帖子总数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='讨论区板块表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_board`
--

LOCK TABLES `forum_board` WRITE;
/*!40000 ALTER TABLE `forum_board` DISABLE KEYS */;
INSERT INTO `forum_board` VALUES (1,'计算机考研(408)','包含数据结构、计算机组成原理、操作系统和计算机网络讨论。','https://dummyimage.com/400x200/000/fff&text=CS+408',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(2,'考研数学','数学一、数学二、数学三复习交流。','https://dummyimage.com/400x200/007bff/fff&text=Math',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(3,'考研英语','英语一、英语二阅读、作文、翻译打卡与交流。','https://dummyimage.com/400x200/28a745/fff&text=English',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,'考研政治','马原、毛中特、史纲、思修及当代时政讨论。','https://dummyimage.com/400x200/dc3545/fff&text=Politics',1,0,'2026-04-17 20:44:07','2026-04-17 20:44:07');
/*!40000 ALTER TABLE `forum_board` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_comment`
--

DROP TABLE IF EXISTS `forum_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '所属帖子ID',
  `user_id` bigint NOT NULL COMMENT '评论发布者ID',
  `reply_to_id` bigint DEFAULT NULL COMMENT '回复的目标评论ID(顶层评论为空)',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_post_id` (`post_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2056945882768285698 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子评论表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_comment`
--

LOCK TABLES `forum_comment` WRITE;
/*!40000 ALTER TABLE `forum_comment` DISABLE KEYS */;
INSERT INTO `forum_comment` VALUES (1,1,4,NULL,'感谢大佬分享，刚好处于迷茫期，太需要这份规划了！',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(2,1,5,NULL,'请问计组部分有必要看袁春风老师的教材吗？还是直接上王道？',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(3,1,3,2,'如果现在时间充裕（5月份之前）可以看看课本打基础，时间紧的话直接吃透王道讲义即可。',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,2,2,NULL,'线代推荐去看看李永乐老师的强化班视频，非常系统，适合理清思路。',0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(2045122750531506177,1,6,NULL,'nb',0,'2026-04-17 20:50:35','2026-04-17 20:50:35'),(2045122779862274049,1,6,NULL,'nb',0,'2026-04-17 20:50:42','2026-04-17 20:50:42'),(2045147261540786178,2,6,NULL,'nb',0,'2026-04-17 22:27:59','2026-04-17 22:27:59'),(2045147371746123777,5,6,NULL,'nb',0,'2026-04-17 22:28:25','2026-04-17 22:28:25'),(2045147410576990209,5,7,NULL,'nb',0,'2026-04-17 22:28:34','2026-04-17 22:28:34'),(2045335555956387842,5,6,NULL,'？？',0,'2026-04-18 10:56:12','2026-04-18 10:56:12'),(2045335583622017026,5,6,NULL,'我的用户名呢？',0,'2026-04-18 10:56:18','2026-04-18 10:56:18'),(2045335628350074881,1,6,NULL,'用户名呢',0,'2026-04-18 10:56:29','2026-04-18 10:56:29'),(2045336144836669441,1,6,NULL,'NB',0,'2026-04-18 10:58:32','2026-04-18 10:58:32'),(2045351143416119298,5,6,NULL,'nb',0,'2026-04-18 11:58:08','2026-04-18 11:58:08'),(2045352270488518657,5,6,NULL,'牛逼',0,'2026-04-18 12:02:37','2026-04-18 12:02:37'),(2045376997667979265,6,6,NULL,'nb',0,'2026-04-18 13:40:52','2026-04-18 13:40:52'),(2045381396712734721,6,6,NULL,'nb',0,'2026-04-18 13:58:21','2026-04-18 13:58:21'),(2045384489915404289,7,6,NULL,'nb',0,'2026-04-18 14:10:38','2026-04-18 14:10:38'),(2045384511004364802,7,6,2045384489915404289,'nb',0,'2026-04-18 14:10:43','2026-04-18 14:10:43'),(2045384648929857537,6,6,2045376997667979265,'nb',0,'2026-04-18 14:11:16','2026-04-18 14:11:16'),(2045385386498215938,7,6,2045384489915404289,'NB',0,'2026-04-18 14:14:12','2026-04-18 14:14:12'),(2045385414990123009,7,6,2045384489915404289,'吃回复',0,'2026-04-18 14:14:19','2026-04-18 14:14:19'),(2045386841577771009,7,6,2045384511004364802,'NB',0,'2026-04-18 14:19:59','2026-04-18 14:19:59'),(2045390912309186561,7,6,2045384511004364802,'NB',0,'2026-04-18 14:36:10','2026-04-18 14:36:10'),(2045390931523293186,7,6,2045390912309186561,'NB',0,'2026-04-18 14:36:14','2026-04-18 14:36:14'),(2045396380482031617,7,6,2045384489915404289,'NB',0,'2026-04-18 14:57:53','2026-04-18 14:57:53'),(2045418841785257985,7,6,2045384489915404289,'nb',0,'2026-04-18 16:27:09','2026-04-18 16:27:09'),(2045435002438569986,7,6,2045384511004364802,'NB',0,'2026-04-18 17:31:22','2026-04-18 17:31:22'),(2045435015252168705,7,6,NULL,'NB',0,'2026-04-18 17:31:25','2026-04-18 17:31:25'),(2056943796508868609,9,8,NULL,'',0,'2026-05-20 11:43:12','2026-05-20 11:43:12'),(2056945882768285697,1,10,NULL,'*********',0,'2026-05-20 11:51:29','2026-05-20 11:51:29');
/*!40000 ALTER TABLE `forum_comment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_post`
--

DROP TABLE IF EXISTS `forum_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post` (
  `id` int NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '所属板块ID',
  `user_id` bigint NOT NULL COMMENT '发帖人用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子标题',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '帖子内容(富文本HTML)',
  `tags` json DEFAULT NULL COMMENT '标签列表',
  `attachment_urls` json DEFAULT NULL COMMENT '附件列表(OSS链接数组)',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览量',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_board_id` (`board_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post`
--

LOCK TABLES `forum_post` WRITE;
/*!40000 ALTER TABLE `forum_post` DISABLE KEYS */;
INSERT INTO `forum_post` VALUES (1,1,3,'2026年408复习规划（个人向分享）','<p>大家好，这是我的408复习规划，基础阶段建议看王道，强化阶段多刷真题...</p><p>附上了我的复习时间表，供大家参考！</p>','[\"408\", \"复习规划\", \"经验贴\"]','[\"https://oss.example.com/file/plan.pdf\"]',1527,3,3,0,'2026-04-17 20:44:07','2026-04-18 19:01:49'),(2,2,4,'求助！张宇18讲的线代部分看不懂怎么办？','<p>特别是特征值和特征向量那一部分，做题完全没有思路，感觉非常吃力，求大佬指点迷津！</p>','[\"数学一\", \"线性代数\", \"求助\"]','[]',330,1,1,0,'2026-04-17 20:44:07','2026-04-18 18:15:38'),(3,3,5,'分享一份自己整理的英语大小作文万能句型','<p>背熟这些句型，考试的时候直接套用，亲测有效！详见附件下载，完全免费分享给大家~</p>','[\"英语一\", \"作文\", \"干货\"]','[\"https://oss.example.com/file/english_writing.docx\"]',2000,2,0,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,4,2,'肖秀荣1000题刷题打卡集中贴（2026版）','<p>欢迎大家每天在这里打卡自己的刷题进度，互相监督，共同进步！每天坚持100道题！</p>','[\"政治\", \"打卡\", \"官方贴\"]','[]',501,0,0,0,'2026-04-17 20:44:07','2026-04-18 15:24:39'),(5,1,6,'测试','测试','[]',NULL,17,2,0,0,'2026-04-17 22:28:18','2026-05-20 15:30:14'),(6,2,6,'测试','测试','[]',NULL,11,1,0,0,'2026-04-18 12:02:59','2026-04-18 18:47:39'),(7,1,6,'测试2','测试2','[]',NULL,30,1,0,0,'2026-04-18 12:11:10','2026-04-18 18:28:23'),(8,1,6,'测试','测试','[]',NULL,19,1,0,0,'2026-04-18 17:07:34','2026-04-18 18:58:35'),(9,1,6,'测试','测试','[]',NULL,22,2,0,0,'2026-04-18 18:04:30','2026-05-20 15:30:03'),(10,1,10,'这是一条***广告***信息','************快来',NULL,NULL,3,0,0,0,'2026-05-20 11:51:29','2026-05-20 15:30:19'),(11,1,8,'测试','# 测试 ### 这是一个测试 #### 这是一个测试 - sad - sadsd - sadds - sadsad 1. a 2. b 3. c 4. d *nb* **nb** ~~撒旦~~ ###### 撒旦','[]',NULL,4,0,0,0,'2026-05-20 19:52:00','2026-05-20 20:00:55'),(12,1,8,'测试','# wu ### wudongbo ```c #include int main(){ printf(\"HelloWorld\"); } ``` 1. wudongbo 2. wudongbo 3. wudongbo - wudongbo - wudongbo -','[]',NULL,3,0,0,0,'2026-05-20 20:46:07','2026-05-20 21:08:46'),(13,1,8,'图片测试','![](/uploads/images/202605/6db2e655ad784b42bdb9cf730e8674ee.jpg) 图片测试','[]',NULL,1,0,0,0,'2026-05-20 21:10:02','2026-05-20 21:10:04'),(14,1,8,'这是一个测试','<pre><code>#include&lt;stdio.h&gt;\nint main(){\n  printf(\"Hello Wordl\");\n  }\n</code></pre>\n<ol>\n <li>a</li>\n <li>b</li>\n <li>c</li>\n <li>d</li>\n</ol>\n<p><img alt=\"\"></p>\n<h1>a</h1>\n<h3>b</h3>','[]',NULL,7,0,0,0,'2026-05-20 21:18:36','2026-05-20 21:27:20'),(15,1,8,'测试2','<h1>测试</h1>\n<h2>测试</h2>\n<ol>\n <li>a</li>\n <li>b</li>\n <li>c</li>\n <li>d</li>\n</ol>\n<ul>\n <li>1</li>\n <li>2</li>\n <li>3</li>\n <li>4</li>\n</ul>\n<pre><code>#include&lt;sostream&gt;\nint mian(){\n  \n  }\n</code></pre>\n<p><img alt=\"\"></p>','[]',NULL,4,1,0,0,'2026-05-20 21:27:08','2026-05-21 18:11:57'),(16,1,8,'图片测试','<p><img alt=\"\"></p>','[]',NULL,4,0,0,0,'2026-05-20 21:29:13','2026-05-20 21:33:50'),(17,1,8,'图片测试3','![](/uploads/images/202605/f962a6c90d124ca599d323d1132f2b34.jpg)','[]',NULL,3,0,0,0,'2026-05-20 21:41:42','2026-05-21 18:12:00'),(18,1,8,'测试4','# 1 ## 2 ### 3 1. a 2. b 3. c - 1 - 2 - 3 - 4 ```c #include int mian(){ } ``` ![](/uploads/images/202605/1d68e8f8a60a4837b4f196085fb86313.jpg)','[]',NULL,2,0,0,0,'2026-05-20 21:43:32','2026-05-20 21:43:50'),(19,1,8,'测试5','<h1>ceshi</h1>\n<h2>d</h2>\n<ol>\n <li>a</li>\n <li>b</li>\n <li>c</li>\n</ol>\n<ul>\n <li>1</li>\n <li>2</li>\n <li>3</li>\n</ul>\n<pre><code class=\"language-c\">#include&lt;sostream&gt;\nint mian(){\n  \n  }\n</code></pre>\n<p><img src=\"/uploads/images/202605/b422ff2adae8448ab1211b48e4bb83bb.jpg\" alt=\"\"></p>','[]',NULL,5,0,0,0,'2026-05-20 21:46:34','2026-05-21 18:11:50');
/*!40000 ALTER TABLE `forum_post` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_forum_post_after_insert` AFTER INSERT ON `forum_post` FOR EACH ROW BEGIN
    INSERT INTO sys_user_stats (user_id, post_count, like_received_count)
    VALUES (NEW.user_id, 1, 0)
    ON DUPLICATE KEY UPDATE
        post_count = post_count + 1;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `forum_post_collect`
--

DROP TABLE IF EXISTS `forum_post_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '收藏用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子收藏关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post_collect`
--

LOCK TABLES `forum_post_collect` WRITE;
/*!40000 ALTER TABLE `forum_post_collect` DISABLE KEYS */;
INSERT INTO `forum_post_collect` VALUES (6,1,6,'2026-04-18 18:47:53'),(7,8,6,'2026-04-18 18:48:46'),(8,9,6,'2026-04-18 18:49:24');
/*!40000 ALTER TABLE `forum_post_collect` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_post_like`
--

DROP TABLE IF EXISTS `forum_post_like`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_post_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `post_id` bigint NOT NULL COMMENT '帖子ID',
  `user_id` bigint NOT NULL COMMENT '点赞用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_post_user` (`post_id`,`user_id`) USING BTREE COMMENT '防止重复点赞'
) ENGINE=InnoDB AUTO_INCREMENT=2057090765847957506 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子点赞关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post_like`
--

LOCK TABLES `forum_post_like` WRITE;
/*!40000 ALTER TABLE `forum_post_like` DISABLE KEYS */;
INSERT INTO `forum_post_like` VALUES (1,1,4,'2026-04-17 20:44:08'),(2,1,5,'2026-04-17 20:44:08'),(3,3,3,'2026-04-17 20:44:08'),(4,3,4,'2026-04-17 20:44:08'),(2045122250729869314,1,6,'2026-04-17 20:48:36'),(2045333316848168961,5,6,'2026-04-18 10:47:18'),(2045428795762036738,7,6,'2026-04-18 17:06:42'),(2045431325170618370,8,6,'2026-04-18 17:16:45'),(2045443401733664770,9,6,'2026-04-18 18:04:44'),(2045443684639469570,6,6,'2026-04-18 18:05:52'),(2045443773990727681,2,6,'2026-04-18 18:06:13'),(2056943694058799106,9,8,'2026-05-20 11:42:47'),(2057000932500729858,5,8,'2026-05-20 15:30:14'),(2057090765847957505,15,8,'2026-05-20 21:27:12');
/*!40000 ALTER TABLE `forum_post_like` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `forum_report`
--

DROP TABLE IF EXISTS `forum_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `forum_report` (
  `id` bigint NOT NULL COMMENT '主键',
  `reporter_id` bigint NOT NULL COMMENT '举报人ID',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报目标类型: POST(帖子), COMMENT(评论), USER(用户)',
  `target_id` bigint NOT NULL COMMENT '举报目标的主键ID',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报原因描述',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态: 0-待处理, 1-已处理(封禁/删除), 2-已驳回(正常)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '处理时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='举报与审核记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_report`
--

LOCK TABLES `forum_report` WRITE;
/*!40000 ALTER TABLE `forum_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `forum_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_member`
--

DROP TABLE IF EXISTS `group_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '群组ID',
  `user_id` bigint NOT NULL COMMENT '成员用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'MEMBER' COMMENT '群内角色：OWNER(群主), ADMIN(管理员), MEMBER(普通成员)',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（退群）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入群时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_group_user` (`group_id`,`user_id`) USING BTREE COMMENT '防止重复加群',
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='群成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_member`
--

LOCK TABLES `group_member` WRITE;
/*!40000 ALTER TABLE `group_member` DISABLE KEYS */;
INSERT INTO `group_member` VALUES (1,1,6,'OWNER',0,'2026-05-20 12:34:35','2026-05-20 12:34:35'),(2,1,7,'MEMBER',0,'2026-05-20 12:34:35','2026-05-20 12:34:35'),(3,2,11,'OWNER',0,'2026-05-20 14:36:28','2026-05-20 14:36:28'),(4,3,8,'OWNER',0,'2026-05-20 15:16:22','2026-05-20 15:16:22'),(5,1,11,'MEMBER',0,'2026-05-20 15:20:33','2026-05-20 15:20:33'),(6,1,8,'MEMBER',0,'2026-05-20 15:20:42','2026-05-20 15:20:42');
/*!40000 ALTER TABLE `group_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `group_message`
--

DROP TABLE IF EXISTS `group_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL COMMENT '所属群组ID',
  `user_id` bigint NOT NULL COMMENT '发送者用户ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（撤回）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_group_id` (`group_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='群聊消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `group_message`
--

LOCK TABLES `group_message` WRITE;
/*!40000 ALTER TABLE `group_message` DISABLE KEYS */;
INSERT INTO `group_message` VALUES (1,1,6,'大家好，欢迎加入408考研群！',0,'2026-05-20 12:38:05','2026-05-20 12:38:05'),(2,1,7,'谢谢群主！',0,'2026-05-20 12:38:05','2026-05-20 12:38:05'),(3,1,7,'*********',0,'2026-05-20 12:38:05','2026-05-20 12:38:05'),(4,2,11,'hello',0,'2026-05-20 14:36:41','2026-05-20 14:36:41'),(5,2,11,'有人吗',0,'2026-05-20 14:36:50','2026-05-20 14:36:50'),(6,2,11,'hello',0,'2026-05-20 14:38:48','2026-05-20 14:38:48'),(7,2,11,'hello',0,'2026-05-20 14:52:05','2026-05-20 14:52:05'),(8,2,11,'啥情况',0,'2026-05-20 14:52:15','2026-05-20 14:52:15'),(9,2,11,'你好',0,'2026-05-20 14:52:18','2026-05-20 14:52:18'),(10,2,11,'？？？？？？？',0,'2026-05-20 14:52:25','2026-05-20 14:52:25'),(11,2,11,'hello',0,'2026-05-20 15:01:40','2026-05-20 15:01:40'),(12,2,11,'hello',0,'2026-05-20 15:01:45','2026-05-20 15:01:45'),(13,2,11,'hello',0,'2026-05-20 15:01:48','2026-05-20 15:01:48'),(14,2,11,'hello',0,'2026-05-20 15:01:54','2026-05-20 15:01:54'),(15,2,11,'hello',0,'2026-05-20 15:01:56','2026-05-20 15:01:56'),(16,2,11,'hello',0,'2026-05-20 15:03:36','2026-05-20 15:03:36'),(17,2,11,'hello',0,'2026-05-20 15:03:43','2026-05-20 15:03:43'),(18,2,11,'hello',0,'2026-05-20 15:03:49','2026-05-20 15:03:49'),(19,2,11,'hello',0,'2026-05-20 15:05:20','2026-05-20 15:05:20'),(20,2,11,'hello\'',0,'2026-05-20 15:05:32','2026-05-20 15:05:32'),(21,2,11,'hello',0,'2026-05-20 15:09:32','2026-05-20 15:09:32'),(22,2,11,'hello',0,'2026-05-20 15:09:40','2026-05-20 15:09:40'),(23,2,11,'?',0,'2026-05-20 15:09:45','2026-05-20 15:09:45'),(24,2,11,'怎么还是这样子？？？？？？？？？？？？？？',0,'2026-05-20 15:09:51','2026-05-20 15:09:51'),(25,2,11,'你好',0,'2026-05-20 15:13:33','2026-05-20 15:13:33'),(26,2,11,'？？？？？？',0,'2026-05-20 15:13:38','2026-05-20 15:13:38'),(27,2,11,'日志呢？',0,'2026-05-20 15:13:41','2026-05-20 15:13:41'),(28,2,11,'日志呢?',0,'2026-05-20 15:13:57','2026-05-20 15:13:57'),(29,2,11,'????????????????',0,'2026-05-20 15:13:59','2026-05-20 15:13:59'),(30,3,8,'？？？？？？？？？',0,'2026-05-20 15:16:31','2026-05-20 15:16:31'),(31,3,8,'？？？',0,'2026-05-20 15:16:37','2026-05-20 15:16:37'),(32,3,8,'啥意思啊',0,'2026-05-20 15:16:39','2026-05-20 15:16:39'),(33,3,8,'？',0,'2026-05-20 15:17:00','2026-05-20 15:17:00'),(34,3,8,'为啥啊',0,'2026-05-20 15:17:03','2026-05-20 15:17:03'),(35,3,8,'？？？？？？？？',0,'2026-05-20 15:17:18','2026-05-20 15:17:18'),(36,3,8,'为啥啊',0,'2026-05-20 15:17:20','2026-05-20 15:17:20'),(37,1,8,'谢谢群主',0,'2026-05-20 15:20:50','2026-05-20 15:20:50'),(38,1,11,'这是什么情况？？？？？？？？？？？',0,'2026-05-20 15:20:59','2026-05-20 15:20:59');
/*!40000 ALTER TABLE `group_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_check_in`
--

DROP TABLE IF EXISTS `interaction_check_in`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_check_in` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '打卡用户ID',
  `study_hours` int NOT NULL COMMENT '学习时长(小时)',
  `notes` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '打卡日记/笔记',
  `created_date` date NOT NULL COMMENT '打卡归属日期(YYYY-MM-DD)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '具体打卡时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_date` (`user_id`,`created_date`) USING BTREE COMMENT '每天每人只能打卡一次'
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='每日学习打卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_check_in`
--

LOCK TABLES `interaction_check_in` WRITE;
/*!40000 ALTER TABLE `interaction_check_in` DISABLE KEYS */;
INSERT INTO `interaction_check_in` VALUES (1,3,8,'今天完成了数据结构前三章的课后题，错了不少，明天继续复盘错题。','2026-04-16','2026-04-17 20:44:08'),(2,4,10,'高数刷了50题，英语背了100个单词，充实的一天！继续保持！','2026-04-16','2026-04-17 20:44:08'),(3,5,6,'今天有点感冒，状态不好，只背了2个单元的政治，明天要补回来。','2026-04-16','2026-04-17 20:44:08'),(4,3,9,'计组真题第一套完成，大题还要加强，感觉指令系统那块还是不熟。','2026-04-17','2026-04-17 20:44:08'),(5,6,24,'状态极佳，暴力学习24小时','2026-04-18','2026-04-18 11:42:38'),(6,8,8,'今天状态不错','2026-05-21','2026-05-21 15:07:29'),(7,8,8,'舒服','2026-06-04','2026-06-04 14:35:06');
/*!40000 ALTER TABLE `interaction_check_in` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_points_log`
--

DROP TABLE IF EXISTS `interaction_points_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_points_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '变动积分（正数增加）',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：CHECK_IN/POST/LIKE/COMMENT/RESOURCE',
  `rel_id` bigint DEFAULT NULL COMMENT '关联ID（打卡ID/帖子ID/评论ID）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='积分变动日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_points_log`
--

LOCK TABLES `interaction_points_log` WRITE;
/*!40000 ALTER TABLE `interaction_points_log` DISABLE KEYS */;
INSERT INTO `interaction_points_log` VALUES (1,6,3,'CHECK_IN',NULL,'打卡+3分','2026-04-18 11:42:38'),(2,8,3,'CHECK_IN',NULL,'打卡+3分','2026-05-21 15:07:29'),(3,8,3,'CHECK_IN',NULL,'打卡+3分','2026-06-04 14:35:06');
/*!40000 ALTER TABLE `interaction_points_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interaction_user_study`
--

DROP TABLE IF EXISTS `interaction_user_study`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interaction_user_study` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `continuous_days` int NOT NULL DEFAULT '0' COMMENT '连续打卡天数',
  `total_check_days` int NOT NULL DEFAULT '0' COMMENT '累计打卡天数',
  `last_check_date` date DEFAULT NULL COMMENT '最后一次打卡日期',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户学习打卡统计';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_user_study`
--

LOCK TABLES `interaction_user_study` WRITE;
/*!40000 ALTER TABLE `interaction_user_study` DISABLE KEYS */;
INSERT INTO `interaction_user_study` VALUES (1,6,1,1,'2026-04-18','2026-04-18 11:42:38'),(2,8,1,2,'2026-06-04','2026-05-21 15:07:29');
/*!40000 ALTER TABLE `interaction_user_study` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interview_record`
--

DROP TABLE IF EXISTS `interview_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `session_id` bigint NOT NULL COMMENT '所属会话ID，关联 interview_session.id',
  `role` varchar(10) NOT NULL COMMENT '角色：user(用户发言) / ai(人工智能考官)',
  `content` text NOT NULL COMMENT '对话内容文本，支持长文本',
  `fluency_score` decimal(3,1) DEFAULT NULL COMMENT '语音流利度得分（仅 role=user 时有值），范围 0.0 ~ 100.0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模拟面试对话明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_record`
--

LOCK TABLES `interview_record` WRITE;
/*!40000 ALTER TABLE `interview_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interview_report`
--

DROP TABLE IF EXISTS `interview_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_report` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `session_id` bigint NOT NULL COMMENT '所属会话ID，关联 interview_session.id，一对一关系',
  `total_score` decimal(3,1) NOT NULL COMMENT '综合评分，范围 0.0 ~ 100.0',
  `radar_chart` json DEFAULT NULL COMMENT '雷达图能力维度数据（JSON格式），包含各维度的名称与分值',
  `strength_analysis` text COMMENT '优势分析，AI 对考生表现得好的方面进行总结',
  `weakness_analysis` text COMMENT '薄弱项分析，AI 指出考生需要改进的地方',
  `suggestion` text COMMENT '改进建议，AI 给出的针对性备考建议',
  `summary` text COMMENT '综合评价总结，一段话概括整场面试表现',
  `raw_json` text COMMENT 'AI 返回的原始完整 JSON 字符串，用于问题回溯与调试',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报告生成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模拟面试评估报告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_report`
--

LOCK TABLES `interview_report` WRITE;
/*!40000 ALTER TABLE `interview_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `interview_session`
--

DROP TABLE IF EXISTS `interview_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `interview_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID，自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联 sys_user.id',
  `target_school` varchar(100) DEFAULT NULL COMMENT '目标院校，例如：北京大学',
  `target_major` varchar(100) DEFAULT NULL COMMENT '目标专业，例如：计算机科学与技术',
  `interview_type` varchar(20) NOT NULL COMMENT '面试类型：ENGLISH(英文面试) / MAJOR(专业课面试) / COMPREHENSIVE(综合面试)',
  `status` varchar(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT '会话状态：IN_PROGRESS(进行中) / REPORTED(已出报告)',
  `overall_score` decimal(3,1) DEFAULT NULL COMMENT '综合评分，范围 0.0 ~ 100.0，未出报告时为 NULL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模拟面试会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_session`
--

LOCK TABLES `interview_session` WRITE;
/*!40000 ALTER TABLE `interview_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `interview_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `knowledge_point`
--

DROP TABLE IF EXISTS `knowledge_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `knowledge_point` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `parent_id` bigint DEFAULT NULL COMMENT '父节点ID，null=根科目节点',
  `name` varchar(100) NOT NULL COMMENT '知识点名称',
  `subject` varchar(50) DEFAULT NULL COMMENT '所属科目',
  `level` int DEFAULT '0' COMMENT '层级深度 0=根科目 1=章 2=节 3=具体知识点',
  `sort_order` int DEFAULT '0' COMMENT '同层级排序',
  `is_deleted` tinyint DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_subject` (`subject`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=322 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='知识体系树';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `knowledge_point`
--

LOCK TABLES `knowledge_point` WRITE;
/*!40000 ALTER TABLE `knowledge_point` DISABLE KEYS */;
INSERT INTO `knowledge_point` VALUES (1,NULL,'408计算机','408计算机',0,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(2,1,'数据结构','408计算机',1,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(3,2,'线性表','408计算机',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(4,3,'顺序表','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(5,3,'链表','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(6,2,'栈和队列','408计算机',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(7,6,'栈的应用','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(8,6,'队列的应用','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(9,2,'树与二叉树','408计算机',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(10,9,'二叉树遍历','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(11,9,'二叉搜索树','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(12,9,'平衡二叉树','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(13,9,'哈夫曼树','408计算机',3,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(14,2,'图','408计算机',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(15,14,'图的遍历','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(16,14,'最短路径','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(17,14,'最小生成树','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(18,14,'拓扑排序','408计算机',3,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(19,2,'查找','408计算机',2,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(20,19,'二分查找','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(21,19,'散列表','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(22,19,'B树/B+树','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(23,2,'排序','408计算机',2,6,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(24,23,'快速排序','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(25,23,'堆排序','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(26,23,'归并排序','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(27,23,'基数排序','408计算机',3,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(28,1,'计算机组成原理','408计算机',1,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(29,28,'数据的表示与运算','408计算机',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(30,29,'定点数','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(31,29,'浮点数','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(32,29,'ALU','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(33,28,'存储器层次结构','408计算机',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(34,33,'Cache','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(35,33,'虚拟内存','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(36,28,'指令系统','408计算机',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(37,36,'指令格式','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(38,36,'寻址方式','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(39,28,'CPU','408计算机',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(40,39,'数据通路','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(41,39,'流水线','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(42,28,'总线与IO','408计算机',2,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(43,42,'中断','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(44,42,'DMA','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(45,1,'操作系统','408计算机',1,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(46,45,'进程管理','408计算机',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(47,46,'进程状态与转换','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(48,46,'进程同步与互斥','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(49,46,'死锁','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(50,46,'线程','408计算机',3,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(51,45,'内存管理','408计算机',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(52,51,'分页','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(53,51,'分段','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(54,51,'页面置换算法','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(55,45,'文件系统','408计算机',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(56,55,'文件目录结构','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(57,55,'磁盘调度','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(58,45,'IO管理','408计算机',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(59,1,'计算机网络','408计算机',1,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(60,59,'物理层','408计算机',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(61,60,'奈奎斯特定理','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(62,60,'香农定理','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(63,59,'数据链路层','408计算机',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(64,63,'CSMA/CD','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(65,63,'MAC地址','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(66,59,'网络层','408计算机',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(67,66,'IP协议','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(68,66,'路由算法','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(69,66,'子网划分','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(70,66,'ARP','408计算机',3,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(71,59,'传输层','408计算机',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(72,71,'TCP','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(73,71,'UDP','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(74,71,'拥塞控制','408计算机',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(75,59,'应用层','408计算机',2,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(76,75,'HTTP','408计算机',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(77,75,'DNS','408计算机',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(100,NULL,'数学(一)','数学(一)',0,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(101,100,'高等数学','数学(一)',1,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(102,101,'极限与连续','数学(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(103,102,'极限计算','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(104,102,'无穷小比较','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(105,102,'连续性判断','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(106,101,'一元微分','数学(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(107,106,'导数定义','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(108,106,'中值定理','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(109,106,'泰勒公式','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(110,101,'一元积分','数学(一)',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(111,110,'不定积分','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(112,110,'定积分','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(113,110,'定积分应用','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(114,101,'多元微分','数学(一)',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(115,114,'偏导数','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(116,114,'全微分','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(117,114,'多元极值','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(118,101,'多元积分','数学(一)',2,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(119,118,'二重积分','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(120,118,'三重积分','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(121,118,'曲线积分','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(122,118,'曲面积分','数学(一)',3,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(123,101,'无穷级数','数学(一)',2,6,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(124,123,'数项级数','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(125,123,'幂级数','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(126,123,'傅里叶级数','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(127,101,'微分方程','数学(一)',2,7,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(128,127,'一阶方程','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(129,127,'高阶线性方程','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(130,100,'线性代数','数学(一)',1,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(131,130,'行列式','数学(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(132,130,'矩阵','数学(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(133,132,'矩阵运算','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(134,132,'逆矩阵','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(135,132,'矩阵的秩','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(136,130,'向量组','数学(一)',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(137,136,'线性相关','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(138,136,'极大无关组','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(139,130,'方程组','数学(一)',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(140,130,'特征值与特征向量','数学(一)',2,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(141,130,'二次型','数学(一)',2,6,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(142,100,'概率论','数学(一)',1,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(143,142,'随机事件与概率','数学(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(144,142,'随机变量','数学(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(145,144,'分布函数','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(146,144,'常见分布','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(147,142,'多维随机变量','数学(一)',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(148,142,'数字特征','数学(一)',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(149,148,'期望','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(150,148,'方差','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(151,148,'协方差与相关系数','数学(一)',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(152,142,'大数定律与中心极限','数学(一)',2,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(153,142,'数理统计','数学(一)',2,6,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(154,153,'参数估计','数学(一)',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(155,153,'假设检验','数学(一)',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(200,NULL,'英语(一)','英语(一)',0,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(201,200,'完形填空','英语(一)',1,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(202,201,'逻辑关系题','英语(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(203,201,'词义辨析题','英语(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(204,201,'固定搭配','英语(一)',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(205,200,'阅读理解','英语(一)',1,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(206,205,'细节题','英语(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(207,205,'推理题','英语(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(208,205,'主旨题','英语(一)',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(209,205,'态度题','英语(一)',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(210,205,'词义猜测题','英语(一)',2,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(211,200,'新题型','英语(一)',1,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(212,211,'七选五','英语(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(213,211,'段落排序','英语(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(214,200,'翻译','英语(一)',1,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(215,214,'长难句分析','英语(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(216,214,'英译汉技巧','英语(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(217,200,'写作','英语(一)',1,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(218,217,'小作文','英语(一)',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(219,217,'大作文','英语(一)',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(220,217,'常用句型','英语(一)',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(300,NULL,'政治','政治',0,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(301,300,'马克思主义基本原理','政治',1,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(302,301,'唯物辩证法','政治',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(303,302,'对立统一规律','政治',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(304,302,'量变质变规律','政治',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(305,302,'否定之否定规律','政治',3,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(306,301,'认识论','政治',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(307,306,'实践与认识','政治',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(308,306,'真理与价值','政治',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(309,301,'唯物史观','政治',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(310,309,'社会基本矛盾','政治',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(311,309,'人民群众在历史中的作用','政治',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(312,301,'政治经济学','政治',2,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(313,312,'商品与价值','政治',3,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(314,312,'剩余价值','政治',3,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(315,300,'毛泽东思想和中国特色社会主义理论','政治',1,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(316,315,'新民主主义革命理论','政治',2,1,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(317,315,'社会主义改造理论','政治',2,2,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(318,315,'改革开放理论','政治',2,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(319,300,'中国近现代史纲要','政治',1,3,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(320,300,'思想道德修养与法律基础','政治',1,4,0,'2026-06-05 16:03:23','2026-06-05 16:03:23'),(321,300,'形势与政策','政治',1,5,0,'2026-06-05 16:03:23','2026-06-05 16:03:23');
/*!40000 ALTER TABLE `knowledge_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `message`
--

DROP TABLE IF EXISTS `message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL COMMENT '发送者ID',
  `to_user_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '私信内容',
  `is_read` int DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_from_user` (`from_user_id`) USING BTREE,
  KEY `idx_to_user` (`to_user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='私信表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `message`
--

LOCK TABLES `message` WRITE;
/*!40000 ALTER TABLE `message` DISABLE KEYS */;
INSERT INTO `message` VALUES (1,7,6,'呼呼呼',1,NULL),(2,7,6,'学长在吗',1,NULL),(3,7,6,'呼呼呼',1,NULL),(4,6,7,'发生什么事了',1,NULL),(5,7,6,'呼呼呼学长在吗？',1,NULL),(6,6,7,'怎么了？',1,NULL),(7,7,6,'66',1,NULL),(8,6,7,'nb',1,NULL),(9,7,6,'什么情况',1,NULL),(10,6,7,'为什么看不大',1,NULL),(11,6,7,'哭了',1,NULL),(12,7,6,'舒服',1,NULL),(13,6,7,'学长在吗？',1,NULL),(14,7,6,'好像还有点问题',1,NULL),(15,6,7,'你人呢？',1,NULL),(16,7,6,'信息提示好像还有些bug',1,NULL),(17,7,6,'明天再看看',1,NULL),(18,6,7,'ok',1,NULL),(19,6,7,'我下了',1,NULL),(20,7,6,'拜拜',1,NULL),(21,6,7,'👋',1,NULL),(22,7,6,'为什么？',1,NULL),(23,7,6,'为什么？',1,NULL),(24,7,6,'为什么',1,NULL),(25,7,6,'为什么？',1,NULL),(26,6,7,'为啥',1,NULL),(27,7,6,'这是怎么回事',1,NULL),(28,6,7,'我哭了',1,NULL),(29,6,7,'泪目了',1,NULL),(30,6,7,'这TM',1,NULL),(31,6,7,'你他妈又反转了？',1,NULL),(32,6,7,'不是',1,NULL),(33,6,7,'我服了',1,NULL),(34,6,7,'就这吧',1,NULL),(35,6,7,'哭了',1,NULL),(36,7,6,'特娘的',1,NULL),(37,6,7,'NB',1,NULL),(38,7,6,'NB',1,NULL),(39,6,3,'学长在吗',0,NULL),(40,6,7,'NB',1,NULL),(41,6,3,'NB',0,NULL),(42,6,3,'喂喂喂',0,NULL),(43,7,6,'发什么什么事了？',1,NULL),(44,6,7,'收到',1,NULL),(45,10,7,'*********出售',0,NULL);
/*!40000 ALTER TABLE `message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mistake_daily_plan`
--

DROP TABLE IF EXISTS `mistake_daily_plan`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mistake_daily_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plan_date` date NOT NULL COMMENT '计划日期',
  `note_ids` json DEFAULT NULL COMMENT '当天待复习的错题ID列表',
  `completed_ids` json DEFAULT NULL COMMENT '已完成的错题ID列表',
  `total_count` int DEFAULT '0' COMMENT '计划总数',
  `completed_count` int DEFAULT '0' COMMENT '已完成数',
  `is_completed` tinyint DEFAULT '0' COMMENT '是否全部完成',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`plan_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='每日复习计划';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mistake_daily_plan`
--

LOCK TABLES `mistake_daily_plan` WRITE;
/*!40000 ALTER TABLE `mistake_daily_plan` DISABLE KEYS */;
/*!40000 ALTER TABLE `mistake_daily_plan` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mistake_note`
--

DROP TABLE IF EXISTS `mistake_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mistake_note` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `subject` varchar(50) DEFAULT '其他' COMMENT '科目：政治/英语(一)/英语(二)/数学(一)/数学(二)/数学(三)/408计算机/其他',
  `question_content` text COMMENT '题目内容（OCR或手动输入）',
  `answer` text COMMENT '答案与解析',
  `image_url` varchar(500) DEFAULT NULL COMMENT '原题图片URL',
  `knowledge_points` varchar(500) DEFAULT NULL COMMENT '知识点标签（逗号分隔，如：操作系统-进程调度,数据结构-二叉树）',
  `source` varchar(200) DEFAULT NULL COMMENT '来源（如：2023真题、张宇1000题、王道408等）',
  `difficulty` tinyint DEFAULT '3' COMMENT '难度 1-5',
  `mastery_level` tinyint DEFAULT '0' COMMENT '掌握程度 0-100',
  `review_stage` int DEFAULT '0' COMMENT '当前艾宾浩斯复习阶段（0-7），0=未复习，7=已掌握',
  `review_count` int DEFAULT '0' COMMENT '累计复习次数',
  `next_review_date` date DEFAULT NULL COMMENT '下次复习日期',
  `last_review_date` date DEFAULT NULL COMMENT '上次复习日期',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_next_review` (`user_id`,`next_review_date`),
  KEY `idx_subject` (`user_id`,`subject`),
  KEY `idx_mastery` (`user_id`,`mastery_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mistake_note`
--

LOCK TABLES `mistake_note` WRITE;
/*!40000 ALTER TABLE `mistake_note` DISABLE KEYS */;
/*!40000 ALTER TABLE `mistake_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mistake_notification`
--

DROP TABLE IF EXISTS `mistake_notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mistake_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` varchar(50) DEFAULT 'REVIEW_REMINDER' COMMENT 'REVIEW_REMINDER | MASTERY_MILESTONE | STAGE_MASTERED',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` varchar(500) DEFAULT NULL COMMENT '通知正文',
  `is_read` tinyint DEFAULT '0' COMMENT '0=未读 1=已读',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题本通知';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mistake_notification`
--

LOCK TABLES `mistake_notification` WRITE;
/*!40000 ALTER TABLE `mistake_notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `mistake_notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mistake_review_log`
--

DROP TABLE IF EXISTS `mistake_review_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mistake_review_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `note_id` bigint NOT NULL COMMENT '错题ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `review_stage` int DEFAULT NULL COMMENT '复习时处于第几阶段',
  `mastery_before` tinyint DEFAULT NULL COMMENT '复习前掌握程度',
  `mastery_after` tinyint DEFAULT NULL COMMENT '复习后掌握程度',
  `is_correct` tinyint DEFAULT '0' COMMENT '本次是否答对',
  `reviewed_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_reviewed_at` (`user_id`,`reviewed_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='复习日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mistake_review_log`
--

LOCK TABLES `mistake_review_log` WRITE;
/*!40000 ALTER TABLE `mistake_review_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `mistake_review_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recommendation_history`
--

DROP TABLE IF EXISTS `recommendation_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recommendation_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `input_json` json NOT NULL COMMENT '用户输入快照',
  `result_json` json NOT NULL COMMENT '推荐结果快照',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='择校推荐历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recommendation_history`
--

LOCK TABLES `recommendation_history` WRITE;
/*!40000 ALTER TABLE `recommendation_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `recommendation_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resource_file`
--

DROP TABLE IF EXISTS `resource_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resource_file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `board_id` bigint NOT NULL COMMENT '所属板块ID',
  `uploader_id` bigint NOT NULL COMMENT '上传者用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '资料标题',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '资料描述',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OSS/对象存储链接',
  `file_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '文件格式(pdf, docx等)',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `download_count` int NOT NULL DEFAULT '0' COMMENT '下载次数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_board_id` (`board_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='学习资料表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resource_file`
--

LOCK TABLES `resource_file` WRITE;
/*!40000 ALTER TABLE `resource_file` DISABLE KEYS */;
INSERT INTO `resource_file` VALUES (1,1,2,'2010-2025年408真题及详细解析','包含了近十几年408统考真题，排版非常清晰，可直接打印。','https://oss.example.com/resources/408_exams.pdf','pdf',52428800,350,0,'2026-04-17 20:44:08'),(2,2,2,'高等数学核心公式速记表','冲刺阶段必备公式大全，建议打印下来每天早上背诵。','https://oss.example.com/resources/math_formula.pdf','pdf',2048000,1200,0,'2026-04-17 20:44:08'),(3,3,5,'英语长难句分析100例','从历年真题中提取的经典长难句，带语法树分析。','https://oss.example.com/resources/english_sentences.docx','docx',1048576,480,0,'2026-04-17 20:44:08');
/*!40000 ALTER TABLE `resource_file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `school_info`
--

DROP TABLE IF EXISTS `school_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `school_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '院校名称',
  `level` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '院校层次: C9/985/211/DOUBLE_FIRST_CLASS/DOUBLE_NON/ORDINARY',
  `location` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所在地',
  `logo_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '校徽URL',
  `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '官网链接',
  `is_self_line` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否34所自划线院校',
  `avg_admission_score` int DEFAULT NULL COMMENT '往年录取均分(估测)',
  `min_admission_score` int DEFAULT NULL COMMENT '往年录取最低分',
  `hot_level` int NOT NULL DEFAULT '1' COMMENT '热度等级1-10',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_level` (`level`),
  KEY `idx_location` (`location`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考研院校信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `school_info`
--

LOCK TABLES `school_info` WRITE;
/*!40000 ALTER TABLE `school_info` DISABLE KEYS */;
INSERT INTO `school_info` VALUES (1,'清华大学','C9','北京',NULL,NULL,1,420,395,10,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(2,'北京大学','C9','北京',NULL,NULL,1,418,393,10,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(3,'浙江大学','C9','浙江',NULL,NULL,1,400,375,9,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(4,'上海交通大学','C9','上海',NULL,NULL,1,405,378,9,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(5,'华中科技大学','985','湖北',NULL,NULL,1,385,358,8,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(6,'武汉大学','985','湖北',NULL,NULL,1,380,352,8,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(7,'郑州大学','211','河南',NULL,NULL,0,350,318,7,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(8,'南昌大学','211','江西',NULL,NULL,0,340,308,6,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(9,'河南大学','DOUBLE_FIRST_CLASS','河南',NULL,NULL,0,325,298,5,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(10,'深圳大学','DOUBLE_NON','广东',NULL,NULL,0,355,322,7,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(11,'杭州电子科技大学','DOUBLE_NON','浙江',NULL,NULL,0,338,310,6,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(12,'重庆邮电大学','DOUBLE_NON','重庆',NULL,NULL,0,330,300,6,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(13,'桂林电子科技大学','ORDINARY','广西',NULL,NULL,0,288,265,4,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(14,'河南理工大学','ORDINARY','河南',NULL,NULL,0,295,270,4,0,'2026-06-05 18:21:36','2026-06-05 18:21:36');
/*!40000 ALTER TABLE `school_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `school_major`
--

DROP TABLE IF EXISTS `school_major`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `school_major` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `school_id` bigint NOT NULL COMMENT '关联school_info.id',
  `major_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '专业名称',
  `major_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '专业代码',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '学科门类',
  `admission_count` int DEFAULT NULL COMMENT '往年招生人数',
  `applicant_count` int DEFAULT NULL COMMENT '往年报考人数',
  `min_score` int DEFAULT NULL COMMENT '复试最低分',
  `avg_score` int DEFAULT NULL COMMENT '录取平均分',
  `tui_mian_ratio` decimal(5,2) DEFAULT NULL COMMENT '推免比例',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_school_id` (`school_id`),
  KEY `idx_major_name` (`major_name`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院校招生专业表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `school_major`
--

LOCK TABLES `school_major` WRITE;
/*!40000 ALTER TABLE `school_major` DISABLE KEYS */;
INSERT INTO `school_major` VALUES (1,1,'计算机科学与技术','081200','工学',12,580,400,420,0.65,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(2,1,'软件工程','083500','工学',18,480,392,412,0.55,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(3,2,'计算机科学与技术','081200','工学',10,620,398,418,0.70,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(4,3,'计算机科学与技术','081200','工学',25,750,378,400,0.50,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(5,3,'软件工程','083500','工学',30,600,370,392,0.45,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(6,5,'计算机科学与技术','081200','工学',38,780,360,385,0.35,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(7,5,'网络空间安全','083900','工学',22,380,355,378,0.30,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(8,6,'计算机科学与技术','081200','工学',35,720,352,378,0.30,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(9,7,'计算机科学与技术','081200','工学',55,950,325,350,0.20,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(10,7,'软件工程','083500','工学',42,780,318,342,0.18,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(11,8,'计算机科学与技术','081200','工学',40,650,315,340,0.15,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(12,9,'计算机应用技术','081203','工学',28,480,305,325,0.12,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(13,10,'计算机科学与技术','081200','工学',48,1100,340,355,0.18,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(14,11,'计算机科学与技术','081200','工学',45,700,318,338,0.15,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(15,12,'计算机科学与技术','081200','工学',35,420,300,328,0.10,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(16,13,'计算机科学与技术','081200','工学',30,200,270,288,0.05,0,'2026-06-05 18:21:36','2026-06-05 18:21:36'),(17,14,'计算机科学与技术','081200','工学',40,350,278,295,0.05,0,'2026-06-05 18:21:36','2026-06-05 18:21:36');
/*!40000 ALTER TABLE `school_major` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名/昵称',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码(BCrypt)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱(可用于登录)',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号(可用于登录)',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'USER' COMMENT '角色: USER, MODERATOR, ADMIN',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '头像链接(存OSS链接)',
  `target_major` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '目标专业',
  `points` int NOT NULL DEFAULT '0' COMMENT '总积分',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0未删, 1已删',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_verified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否通过上岸认证',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_email` (`email`) USING BTREE,
  UNIQUE KEY `uk_phone` (`phone`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'Admin管理员','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','admin@example.com','13800000001','ADMIN','https://dummyimage.com/100x100/000/fff&text=Admin',NULL,9999,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(2,'考研高数版主','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','mod@example.com','13800000002','MODERATOR','https://dummyimage.com/100x100/007bff/fff&text=Mod','数学与应用数学',5000,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(3,'408上岸人','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user1@example.com','13800000003','USER','https://dummyimage.com/100x100/28a745/fff&text=U1','计算机科学与技术',150,0,'2026-04-17 20:44:07','2026-06-04 19:48:42',1),(4,'英语困难户','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user2@example.com','13800000004','USER','https://dummyimage.com/100x100/dc3545/fff&text=U2','金融学',200,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(5,'政治背书狂','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user3@example.com','13800000005','USER','https://dummyimage.com/100x100/ffc107/000&text=U3','法学',300,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(6,'DB','$2a$10$PYWXnjNRZ0qPWpYwQdU2aO.VKorN3YQyAhoof8D4E.vp5DVNWAdsu','1@qq.com','15713775462','USER','','408',3,0,'2026-04-17 20:46:42','2026-06-04 19:48:42',1),(7,'吴东博','$2a$10$DMcxnrmr8prWhiQxc5UmAuvtd0nVVv6gk.ASAvXP8B.HWIKEBHCTm','2@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-04-17 22:01:13','2026-04-17 22:01:13',0),(8,'db886','$2a$10$OSRZaL2uNGWAgpWspS.W4uyL30h9kae4btbzuJ8ro2XCiSaDsLDYC','123@qq.com','','USER','/uploads/images/202605/e42c48045e284f4c83fc6fc274d4a6f1.jpg','',6,0,'2026-05-20 11:42:11','2026-06-04 14:35:06',0),(9,'swtest','$2a$10$r6ZOziJTHeLeFoHH4zw.ueO3GqVij9IAC/DB21Q6OqwLxtWqC9ec.','swtest@test.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 11:50:26','2026-05-20 11:50:26',0),(10,'swtest2','$2a$10$aRLrJm/wUBqhPxsU6wR2luz5sxFl7OekZIbQVf6hQV1m.HmBD6SN6','swtest2@test.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 11:50:54','2026-05-20 11:50:54',0),(11,'wdb2','$2a$10$DZ73yHUtXMCtOpl3AihWy.Ho/4Ju8aLeGykEUSaHCk7xN0cQW4cXy','1234@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 14:35:40','2026-05-20 14:35:40',0),(12,'db88688','$2a$10$Mr..XY5qEDhUXiJ.yY6YKO48.WFovmyLOhLn5y2.5E2g1QrkDIeLu','123456@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-06-05 19:32:36','2026-06-05 19:32:36',0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_stats`
--

DROP TABLE IF EXISTS `sys_user_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_stats` (
  `user_id` bigint NOT NULL COMMENT '关联 sys_user 表的主键 ID',
  `post_count` int NOT NULL DEFAULT '0' COMMENT '发帖总数',
  `like_received_count` int NOT NULL DEFAULT '0' COMMENT '获得的获赞总数',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `experience_count` int NOT NULL DEFAULT '0' COMMENT '经验贴数',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户数据统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_stats`
--

LOCK TABLES `sys_user_stats` WRITE;
/*!40000 ALTER TABLE `sys_user_stats` DISABLE KEYS */;
INSERT INTO `sys_user_stats` VALUES (2,1,0,'2026-04-18 18:15:26',0),(3,1,3,'2026-04-18 18:15:26',0),(4,1,1,'2026-04-18 18:15:26',0),(5,1,2,'2026-04-18 18:15:26',0),(6,5,7,'2026-05-20 15:30:14',0),(8,9,1,'2026-05-20 21:46:34',0),(10,1,0,'2026-05-20 11:51:29',0);
/*!40000 ALTER TABLE `sys_user_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_ai_profile`
--

DROP TABLE IF EXISTS `user_ai_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_ai_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `cognitive_profile` text COMMENT 'AI生成的学情画像(强项、弱项、当前复习轮次)',
  `psychological_profile` varchar(512) DEFAULT '{"status": "positive", "anxiety_level": "low"}' COMMENT '情感状态画像',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI伴侣用户画像与记忆表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_ai_profile`
--

LOCK TABLES `user_ai_profile` WRITE;
/*!40000 ALTER TABLE `user_ai_profile` DISABLE KEYS */;
INSERT INTO `user_ai_profile` VALUES (1,8,'{\"totalCheckDays\":2,\"continuousDays\":1,\"totalStudyHours\":8,\"lastActive\":\"2026-06-04\",\"completedTaskCount\":1,\"completedTaskCount\":2,\"completedTaskCount\":3}','{\"recentEmotion\":\"一般\",\"lastAnalysis\":\"你的状态真好，这份从容和舒适感，正是高效复习的最佳基础呢！\",\"updatedAt\":\"2026-06-04T14:35:12.519046054\"}','2026-06-04 06:35:13','2026-06-04 07:51:02');
/*!40000 ALTER TABLE `user_ai_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_verification`
--

DROP TABLE IF EXISTS `user_verification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_verification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID，关联 sys_user.id',
  `real_name` varchar(50) NOT NULL COMMENT '真实姓名',
  `target_school` varchar(100) NOT NULL COMMENT '录取院校',
  `target_major` varchar(100) NOT NULL COMMENT '录取专业',
  `admission_year` int NOT NULL COMMENT '入学年份',
  `admission_letter_url` varchar(500) NOT NULL COMMENT '录取通知书图片URL（/uploads/...）',
  `xuexin_screenshot_url` varchar(500) NOT NULL COMMENT '学信网截图URL（/uploads/...）',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态: 0=待审核, 1=已通过, 2=已驳回',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID，关联 sys_user.id',
  `review_comment` varchar(500) DEFAULT NULL COMMENT '审核意见',
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_target_school` (`target_school`),
  KEY `idx_target_major` (`target_major`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='上岸认证记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_verification`
--

LOCK TABLES `user_verification` WRITE;
/*!40000 ALTER TABLE `user_verification` DISABLE KEYS */;
INSERT INTO `user_verification` VALUES (1,3,'张上岸','浙江大学','计算机科学与技术',2025,'/uploads/images/202505/admission_letter_3.png','/uploads/images/202505/xuexin_3.png',1,1,'审核通过，恭喜上岸！','2025-06-15 10:30:00',0,'2026-06-04 19:48:42','2026-06-04 19:48:42'),(2,6,'DB','哈尔滨工业大学','计算机技术',2025,'/uploads/images/202505/admission_letter_6.png','/uploads/images/202505/xuexin_6.png',1,1,'审核通过','2025-06-16 14:20:00',0,'2026-06-04 19:48:42','2026-06-04 19:48:42'),(3,4,'李英语','北京外国语大学','英语笔译',2025,'/uploads/images/202505/admission_letter_4.png','/uploads/images/202505/xuexin_4.png',2,1,'录取通知书图片模糊，请重新上传','2025-06-15 11:00:00',0,'2026-06-04 19:48:42','2026-06-04 19:48:42');
/*!40000 ALTER TABLE `user_verification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'kaoyan_forum'
--

--
-- Dumping routines for database 'kaoyan_forum'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-05 19:42:57
