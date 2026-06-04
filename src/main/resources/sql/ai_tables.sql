-- ============================================================
-- AI 多智能体学习伴侣模块 — 建表 & 种子数据
-- ============================================================

-- 1. AI 每日任务表
CREATE TABLE IF NOT EXISTS ai_daily_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    task_date DATE NOT NULL COMMENT '任务日期',
    task_content VARCHAR(1024) NOT NULL COMMENT '任务内容',
    importance VARCHAR(16) DEFAULT 'MEDIUM' COMMENT '重要程度 HIGH/MEDIUM/LOW',
    status INT DEFAULT 0 COMMENT '完成状态 0-未完成 1-已完成',
    agent_tips VARCHAR(512) COMMENT '智能体叮嘱',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, task_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 每日任务';

-- 2. AI 干预日志表
CREATE TABLE IF NOT EXISTS ai_intervention_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    agent_name VARCHAR(32) NOT NULL COMMENT 'Agent 名称（Psychology/Supervisor/Tutor）',
    trigger_reason VARCHAR(256) COMMENT '触发原因',
    intervention_content TEXT NOT NULL COMMENT '干预内容',
    user_reaction VARCHAR(16) DEFAULT 'UNREAD' COMMENT '用户反馈 UNREAD/READ',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_unread (user_id, user_reaction)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 干预日志';

-- 3. 用户 AI 档案表
CREATE TABLE IF NOT EXISTS user_ai_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    cognitive_profile TEXT COMMENT '认知画像（JSON：打卡天数、学习时长、偏好等）',
    psychological_profile TEXT COMMENT '心理画像（最近情绪状态、干预记录摘要）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户 AI 档案';

-- 4. 考研知识点库（答疑 Agent RAG 数据源）
CREATE TABLE IF NOT EXISTS ai_knowledge_point (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject VARCHAR(64) NOT NULL COMMENT '学科（数据结构/操作系统/计算机网络/计算机组成原理/高等数学/线性代数/概率论/英语/政治）',
    chapter VARCHAR(128) COMMENT '章节',
    title VARCHAR(256) NOT NULL COMMENT '知识点标题',
    content TEXT NOT NULL COMMENT '知识点详细内容',
    keywords VARCHAR(512) COMMENT '关键词，逗号分隔',
    importance VARCHAR(16) DEFAULT 'MEDIUM' COMMENT '重要程度 HIGH/MEDIUM/LOW',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_subject (subject),
    FULLTEXT INDEX ft_keywords (keywords) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考研知识点库';

-- 5. AI 周报持久化表
CREATE TABLE IF NOT EXISTS ai_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    week_start DATE NOT NULL COMMENT '周报周期起始日（周一）',
    week_end DATE NOT NULL COMMENT '周报周期结束日（周日）',
    markdown TEXT NOT NULL COMMENT '周报 Markdown 内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_week (user_id, week_start),
    INDEX idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 周报';

-- 6. 用户行为事件表（Agent 感知用户行为）
CREATE TABLE IF NOT EXISTS ai_user_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    event_type VARCHAR(32) NOT NULL COMMENT '事件类型 VIEW_POST/COLLECT_POST/SEARCH/LIKE_POST',
    event_data JSON COMMENT '事件数据：{"postId":123,"boardId":2,"keyword":"B树","duration":120}',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_type (user_id, event_type),
    INDEX idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户行为事件';

-- ============================================================
-- 种子数据：核心考点
-- ============================================================

INSERT INTO ai_knowledge_point (subject, chapter, title, content, keywords, importance) VALUES
-- 数据结构
('数据结构', '树', 'B树与B+树的区别',
 '1. B树所有节点都存储数据，B+树只有叶子节点存储数据；\n2. B+树叶节点通过链表相连，便于范围查询；\n3. B+树的非叶子节点仅存储索引，单个节点可容纳更多key，树高更低；\n4. B树适合单点查询，B+树适合范围查询和顺序遍历；\n5. MySQL InnoDB 使用 B+树作为索引结构。',
 'B树,B+树,索引,数据库,范围查询,叶子节点', 'HIGH'),

('数据结构', '排序', '快速排序的原理与复杂度',
 '快速排序采用分治策略：选择基准元素pivot，将数组分为两部分（小于pivot和大于pivot），递归排序。\n时间复杂度：平均O(nlogn)，最坏O(n²)（已排序数组+选首元素为pivot）。\n空间复杂度：O(logn)（递归栈）。\n不稳定排序。\n优化：三数取中选pivot、小区间用插入排序。',
 '快速排序,分治,时间复杂度,不稳定排序,递归', 'HIGH'),

('数据结构', '图', 'Dijkstra最短路径算法',
 'Dijkstra算法求解单源最短路径，适用于非负权边的有向/无向图。\n核心思想：贪心，每次选取未访问的距离最小节点，更新其邻居的距离。\n时间复杂度：O(V²)（朴素），O((V+E)logV)（优先队列优化）。\n不能处理负权边（需用Bellman-Ford）。',
 'Dijkstra,最短路径,贪心,优先队列,负权边', 'HIGH'),

('数据结构', '线性表', '栈和队列的应用场景',
 '栈：函数调用栈、表达式求值（后缀表达式）、括号匹配、浏览器前进后退、DFS。\n队列：BFS、消息队列、缓冲区、CPU任务调度。\n循环队列判满：(rear+1)%maxSize == front。\n双端队列deque两端都可进出。',
 '栈,队列,DFS,BFS,循环队列,函数调用', 'MEDIUM'),

-- 操作系统
('操作系统', '进程管理', '进程与线程的区别',
 '1. 进程是资源分配的基本单位，线程是CPU调度的基本单位；\n2. 进程有独立地址空间，线程共享进程的地址空间；\n3. 进程切换开销大（需切换页表等），线程切换开销小；\n4. 线程间通信可直接读写共享变量，进程间通信需要IPC机制；\n5. 一个进程崩溃不影响其他进程，一个线程崩溃可能导致整个进程崩溃。',
 '进程,线程,地址空间,上下文切换,IPC', 'HIGH'),

('操作系统', '内存管理', '虚拟内存与页面置换算法',
 '虚拟内存将逻辑地址与物理地址分离，允许进程使用大于物理内存的地址空间。\n页面置换算法：\n1. FIFO：先进先出，可能Belady异常；\n2. LRU：最近最久未使用，用栈或计数器实现；\n3. Clock（时钟）：LRU的近似，用访问位；\n4. OPT：理论最优，无法实现（需预知未来）。\n缺页率 = 缺页次数 / 总访问次数。',
 '虚拟内存,页面置换,FIFO,LRU,Clock,缺页中断', 'HIGH'),

('操作系统', '同步互斥', '死锁的四个必要条件与预防',
 '死锁四个必要条件：互斥、占有并等待、不可剥夺、循环等待。\n预防策略：\n1. 破坏占有并等待：一次性申请所有资源；\n2. 破坏不可剥夺：允许抢占；\n3. 破坏循环等待：按序申请资源。\n银行家算法用于死锁避免（安全性检查）。',
 '死锁,互斥,银行家算法,循环等待,资源分配', 'HIGH'),

-- 计算机网络
('计算机网络', '传输层', 'TCP三次握手与四次挥手',
 '三次握手：\n1. 客户端→SYN→服务器；2. 服务器→SYN+ACK→客户端；3. 客户端→ACK→服务器。\n为什么三次：防止历史连接被服务器接受。\n四次挥手：\n1. 主动方→FIN；2. 被动方→ACK；3. 被动方→FIN；4. 主动方→ACK。\nTIME_WAIT等待2MSL确保对方收到最后的ACK。\n为什么四次：TCP全双工，每个方向需单独关闭。',
 'TCP,三次握手,四次挥手,SYN,FIN,TIME_WAIT,全双工', 'HIGH'),

('计算机网络', '应用层', 'HTTP与HTTPS的区别',
 'HTTP：明文传输，端口80，无加密。\nHTTPS = HTTP + TLS/SSL，端口443。\nHTTPS握手过程：\n1. 客户端发送支持的加密套件列表；\n2. 服务器返回证书+选定加密套件；\n3. 客户端验证证书，生成随机密钥，用服务器公钥加密发送；\n4. 双方用该密钥对称加密通信。\n对称加密用于数据传输，非对称加密用于密钥交换。',
 'HTTP,HTTPS,TLS,SSL,对称加密,非对称加密,证书', 'MEDIUM'),

-- 计算机组成原理
('计算机组成原理', '存储系统', 'Cache的工作原理与映射方式',
 'Cache解决CPU与主存速度不匹配问题。\n映射方式：\n1. 直接映射：每个主存块只能映射到Cache的固定位置，冲突率高；\n2. 全相联映射：可映射到任意位置，命中率高但硬件复杂；\n3. 组相联映射：折中方案，Cache分组，组内全相联。\n替换算法：LRU、FIFO、随机。\n写策略：写直达（同时写Cache和主存）、写回（只写Cache，脏位标记）。',
 'Cache,直接映射,全相联,组相联,LRU,写回,写直达', 'HIGH'),

('计算机组成原理', '指令系统', 'CISC与RISC的区别',
 'CISC（复杂指令集）：指令数量多、长度可变、执行周期长、微程序控制。代表：x86。\nRISC（精简指令集）：指令数量少、长度固定、单周期执行、硬布线控制、大量通用寄存器。代表：ARM、RISC-V。\nRISC特点：Load/Store架构、流水线效率高、编译器优化空间大。',
 'CISC,RISC,指令集,x86,ARM,流水线,Load/Store', 'MEDIUM'),

-- 高等数学
('高等数学', '微积分', '洛必达法则的使用条件',
 '洛必达法则用于求0/0或无穷/无穷型不定式极限。\n条件：\n1. lim f(x)/g(x) 为0/0或无穷/无穷型；\n2. f(x)和g(x)在去心邻域内可导；\n3. g''(x) 不等于 0；\n4. lim f''(x)/g''(x) 存在（或为无穷）。\n注意：使用前必须验证是不定型，否则会得出错误结果。多次使用需每次验证。',
 '洛必达法则,极限,不定式,0/0,导数', 'HIGH'),

('高等数学', '级数', '泰勒展开公式',
 'f(x)在x₀处的泰勒展开：\nf(x) = Σ f⁽ⁿ⁾(x₀)/n! · (x-x₀)ⁿ\n常用展开（x₀=0，即麦克劳林展开）：\ne^x = 1 + x + x²/2! + x³/3! + ...\nsin x = x - x³/3! + x⁵/5! - ...\ncos x = 1 - x²/2! + x⁴/4! - ...\nln(1+x) = x - x²/2 + x³/3 - ... (|x|≤1, x≠-1)\n1/(1-x) = 1 + x + x² + x³ + ... (|x|<1)',
 '泰勒展开,麦克劳林,级数,e^x,sin,cos,ln', 'HIGH'),

-- 线性代数
('线性代数', '矩阵', '矩阵的秩与线性方程组解的关系',
 '对于m×n矩阵A：\n1. r(A) = n（列满秩）→ 齐次方程Ax=0只有零解；\n2. r(A) < n → 齐次方程有非零解，基础解系含n-r(A)个向量；\n3. 非齐次方程Ax=b有解 ⟺ r(A) = r(A|b)；\n4. r(A) = r(A|b) = n → 唯一解；\n5. r(A) = r(A|b) < n → 无穷多解。',
 '矩阵的秩,线性方程组,基础解系,列满秩,增广矩阵', 'HIGH'),

-- 概率论
('概率论', '随机变量', '常见概率分布及其期望方差',
 '离散型：\n1. 二项分布B(n,p)：E=np, D=np(1-p)\n2. 泊松分布P(λ)：E=λ, D=λ\n3. 几何分布G(p)：E=1/p, D=(1-p)/p²\n连续型：\n1. 均匀分布U(a,b)：E=(a+b)/2, D=(b-a)²/12\n2. 指数分布Exp(λ)：E=1/λ, D=1/λ²\n3. 正态分布N(μ,σ²)：E=μ, D=σ²',
 '二项分布,泊松分布,正态分布,期望,方差,概率密度', 'HIGH'),

-- 英语
('英语', '阅读理解', '考研英语阅读常见题型与解题技巧',
 '主旨题：找首段、末段、各段首句，注意转折词but/however。\n细节题：定位关键词，答案通常是原文同义替换。\n推断题：注意infer/imply/suggest，答案不会太绝对。\n态度题：注意作者用词的感情色彩。\n词义题：结合上下文语境推断，注意前后解释说明。\n排除法：绝对化选项（all/never/must）通常是干扰项。',
 '阅读理解,主旨题,细节题,推断题,同义替换,排除法', 'MEDIUM'),

-- 政治
('政治', '马克思主义原理', '唯物辩证法三大规律',
 '1. 对立统一规律（核心）：矛盾是事物发展的根本动力，矛盾的普遍性与特殊性辩证统一。\n2. 量变质变规律：量变是质变的必要准备，质变是量变的必然结果，量变引起质变，质变巩固量变。\n3. 否定之否定规律：事物发展是螺旋式上升、波浪式前进，经历"肯定—否定—否定之否定"。\n方法论：具体问题具体分析、两点论与重点论统一。',
 '唯物辩证法,对立统一,量变质变,否定之否定,矛盾,方法论', 'HIGH');
