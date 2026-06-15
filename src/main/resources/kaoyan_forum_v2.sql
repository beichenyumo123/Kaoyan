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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- GTID state at the beginning of the backup 
--

SET @@GLOBAL.GTID_PURGED=/*!80000 '+'*/ '2efc3e90-538b-11f1-899f-4cd577095f2b:1-818';

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
-- Table structure for table `ai_chat_message`
--

DROP TABLE IF EXISTS `ai_chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` bigint NOT NULL COMMENT '所属会话ID',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色: user / assistant',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '图片URL',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_created` (`session_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话消息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_message`
--

LOCK TABLES `ai_chat_message` WRITE;
/*!40000 ALTER TABLE `ai_chat_message` DISABLE KEYS */;
INSERT INTO `ai_chat_message` VALUES (39,8,'user','怎么做','/uploads/images/202606/8ffc74c620e6466cbc7ca598b6a66bcf.png','2026-06-06 21:11:49'),(40,8,'assistant','（补充知识）\n\n同学你好！你提供的文本是关于安装百度开源的深度学习框架 PaddlePaddle 以及基于它的 OCR（光学字符识别）工具库 PaddleOCR 的指令。虽然这不属于408计算机综合或公共课的直接笔试考点，但在计算机专业复试机试、科研项目或日常开发中，环境配置是必备的基础技能。\n\n以下是详细的安装步骤和避坑指南：\n\n### 一、 安装前准备\n1. **确认 Python 版本**：PaddleOCR 目前支持 Python 3.6 ~ 3.10 版本。建议使用 Python 3.8 或 3.9，兼容性最好。\n2. **创建虚拟环境（强烈建议）**：为了避免与你电脑上其他的 Python 项目产生依赖冲突，建议使用 `conda` 或 `venv` 创建独立的虚拟环境。\n   ```bash\n   # 使用 conda 创建名为 paddle_env 的环境\n   conda create -n paddle_env python=3.9\n   # 激活环境\n   conda activate paddle_env\n   ```\n\n### 二、 安装 PaddlePaddle (飞桨深度学习框架)\nPaddleOCR 依赖于 PaddlePaddle，因此必须先安装框架。请根据你的电脑硬件选择安装版本：\n\n1. **CPU 版本（适合无独立显卡或 Mac 用户）**：\n   ```bash\n   pip install paddlepaddle -i https://mirror.baidu.com/pypi/simple\n   ```\n2. **GPU 版本（适合有 NVIDIA 显卡且已安装 CUDA 的用户）**：\n   如果你有 NVIDIA 显卡，强烈建议安装 GPU 版本，推理速度会快几十倍。请先确认你的 CUDA 版本（如 11.2, 11.6 等），然后去 [PaddlePaddle 官网](https://www.paddlepaddle.org.cn/) 选择对应的安装命令。\n   例如 CUDA 11.2 的安装命令：\n   ```bash\n   python -m pip install paddlepaddle-gpu==2.5.1.post112 -f https://www.paddlepaddle.org.cn/whl/windows/mkl/avx/stable.html\n   ```\n\n### 三、 安装 PaddleOCR\n框架安装完成后，安装 OCR 工具库：\n```bash\npip install paddleocr -i https://pypi.tuna.tsinghua.edu.cn/simple\n```\n*注：PaddleOCR 依赖 `opencv-python`，通常 pip 会自动安装。如果后续报错缺少 cv2，可手动执行 `pip install opencv-python`。*\n\n### 四、 验证安装是否成功\n在命令行中输入 `python` 进入交互式环境，输入以下代码：\n```python\nimport paddle\npaddle.utils.run_check()  # 检查 PaddlePaddle 是否安装成功\n\nfrom paddleocr import PaddleOCR\nprint(\"PaddleOCR 安装成功！\")\n```\n如果没有报错并输出成功信息，说明环境配置完成。\n\n---\n\n### 💡 常见问题与避坑指南\n1. **下载速度慢/超时**：一定要加上国内镜像源（如清华源 `-i https://pypi.tuna.tsinghua.edu.cn/simple` 或百度源）。\n2. **依赖冲突**：如果提示 `ERROR: Could not build wheels for...`，通常是因为缺少 C++ 编译环境或 Python 版本过高。建议降级 Python 到 3.9。\n3. **首次运行下载模型**：PaddleOCR 首次运行时会自动下载中英文检测和识别模型，请确保网络畅通，模型默认保存在 `~/.paddleocr/` 目录下。\n\n### 🔗 考点链路与学习建议\n虽然本题是工具安装，但它串联了计算机学习中的几个重要环节：\n- **操作系统基础**：环境变量配置、路径管理、命令行操作（Windows CMD/PowerShell 或 Linux Terminal）。\n- **计算机组成原理**：理解 CPU 与 GPU 在并行计算上的差异（为什么 GPU 跑深度学习更快，涉及冯·诺依曼架构与异构计算）。\n- **软件工程**：依赖管理、虚拟环境隔离、版本控制。\n\n建议在复试前或做项目时，熟练掌握 Linux 基本命令和 Python 虚拟环境管理，这将大幅提升你的科研和开发效率！\n\n📚 考点出处\n- 学科：计算机基础技能 / 编程实践\n- 知识点：Python 虚拟环境管理、深度学习框架安装、CPU/GPU 异构计算基础、命令行与包管理工具（pip/conda）使用。',NULL,'2026-06-06 21:13:37'),(41,9,'user','怎么写？','/uploads/images/202606/d4672a8be9044fa599ae576b89536a13.png','2026-06-06 21:15:54'),(42,9,'assistant','这是一道经典的二重积分计算题，主要考察**利用对称性简化积分**以及**极坐标变换**计算二重积分。\n\n以下是详细的解题步骤：\n\n### 1. 分析积分区域与被积函数\n*   **积分区域 $D$**：由 $y=|x|$ 及 $y=1$ 围成。\n    *   这是一个顶点为 $(0,0), (1,1), (-1,1)$ 的三角形区域。\n    *   该区域关于 **$y$ 轴对称**（即若 $(x,y) \\in D$，则 $(-x,y) \\in D$）。\n*   **被积函数拆分**：\n    $$ \\frac{x^2 - x \\cos y - y^2}{x^2 + y^2} = \\frac{x^2 - y^2}{x^2 + y^2} - \\frac{x \\cos y}{x^2 + y^2} $$\n    原积分可以写为两部分之和：\n    $$ I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dx dy - \\iint_D \\frac{x \\cos y}{x^2 + y^2} dx dy $$\n\n### 2. 利用奇偶对称性简化\n观察第二部分积分 $\\iint_D \\frac{x \\cos y}{x^2 + y^2} dx dy$：\n*   令 $f(x,y) = \\frac{x \\cos y}{x^2 + y^2}$。\n*   由于 $f(-x, y) = \\frac{-x \\cos y}{(-x)^2 + y^2} = -f(x,y)$，该函数关于 $x$ 是**奇函数**。\n*   因为积分区域 $D$ 关于 $y$ 轴对称，根据二重积分的对称性性质（“奇函数在对称区间积分为0”），可得：\n    $$ \\iint_D \\frac{x \\cos y}{x^2 + y^2} dx dy = 0 $$\n\n因此，原积分简化为：\n$$ I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dx dy $$\n\n### 3. 利用极坐标计算剩余积分\n由于区域边界包含 $y=1$ 和 $y=|x|$（射线），使用极坐标变换比较方便。\n*   **变换公式**：$x = r \\cos \\theta, y = r \\sin \\theta$，$dx dy = r dr d\\theta$。\n*   **确定积分限**：\n    *   角度 $\\theta$：由 $y=|x|$ 可知边界射线为 $y=x$ ($\\theta=\\frac{\\pi}{4}$) 和 $y=-x$ ($\\theta=\\frac{3\\pi}{4}$)。所以 $\\frac{\\pi}{4} \\le \\theta \\le \\frac{3\\pi}{4}$。\n    *   半径 $r$：区域下界为原点 $r=0$，上界为直线 $y=1$。代入极坐标 $r \\sin \\theta = 1 \\Rightarrow r = \\frac{1}{\\sin \\theta} = \\csc \\theta$。所以 $0 \\le r \\le \\csc \\theta$。\n*   **被积函数变换**：\n    $$ \\frac{x^2 - y^2}{x^2 + y^2} = \\frac{r^2 \\cos^2 \\theta - r^2 \\sin^2 \\theta}{r^2} = \\cos^2 \\theta - \\sin^2 \\theta = \\cos 2\\theta $$\n\n代入积分公式：\n$$ I = \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} d\\theta \\int_{0}^{\\csc \\theta} (\\cos 2\\theta) \\cdot r dr $$\n\n### 4. 计算定积分\n先对 $r$ 积分：\n$$ \\int_{0}^{\\csc \\theta} r dr = \\left[ \\frac{1}{2} r^2 \\right]_{0}^{\\csc \\theta} = \\frac{1}{2} \\csc^2 \\theta $$\n\n再对 $\\theta$ 积分：\n$$ I = \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} \\cos 2\\theta \\cdot \\frac{1}{2} \\csc^2 \\theta d\\theta = \\frac{1}{2} \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} \\frac{\\cos 2\\theta}{\\sin^2 \\theta} d\\theta $$\n\n利用倍角公式 $\\cos 2\\theta = 1 - 2\\sin^2 \\theta$ 化简被积函数：\n$$ \\frac{\\cos 2\\theta}{\\sin^2 \\theta} = \\frac{1 - 2\\sin^2 \\theta}{\\sin^2 \\theta} = \\frac{1}{\\sin^2 \\theta} - 2 = \\csc^2 \\theta - 2 $$\n\n代入计算：\n$$ I = \\frac{1}{2} \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} (\\csc^2 \\theta - 2) d\\theta $$\n$$ I = \\frac{1}{2} \\left[ -\\cot \\theta - 2\\theta \\right]_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} $$\n\n代入上下限：\n*   上限 $\\frac{3\\pi}{4}$：$-\\cot(\\frac{3\\pi}{4}) - 2(\\frac{3\\pi}{4}) = -(-1) - \\frac{3\\pi}{2} = 1 - \\frac{3\\pi}{2}$\n*   下限 $\\frac{\\pi}{4}$：$-\\cot(\\frac{\\pi}{4}) - 2(\\frac{\\pi}{4}) = -1 - \\frac{\\pi}{2}$\n\n$$ I = \\frac{1}{2} \\left[ \\left( 1 - \\frac{3\\pi}{2} \\right) - \\left( -1 - \\frac{\\pi}{2} \\right) \\right] $$\n$$ I = \\frac{1}{2} \\left[ 2 - \\pi \\right] = 1 - \\frac{\\pi}{2} $$\n\n### 最终答案\n$$ 1 - \\frac{\\pi}{2} $$\n\n---\n📚 **考点出处**\n*   **二重积分的对称性**（奇偶性）：高等数学/微积分\n*   **二重积分的极坐标变换**：高等数学/微积分\n*   **三角函数积分公式**（$\\int \\csc^2 x dx = -\\cot x$）：高等数学/微积分\n\n**考点链路**：观察区域形状 $\\rightarrow$ 利用对称性消去奇函数项 $\\rightarrow$ 选择极坐标系 $\\rightarrow$ 计算定积分。',NULL,'2026-06-06 21:17:48'),(43,9,'user','怎么写？','/uploads/images/202606/cd4240692ed64015b9a1a59b85ce0248.png','2026-06-06 21:19:28'),(44,9,'assistant','这是一个经典的二重积分计算问题，利用**对称性**可以极大地简化计算过程。以下是详细的解题步骤：\n\n### 1. 分析积分区域 $D$ 与被积函数 $f(x,y)$\n\n*   **积分区域 $D$**：\n    由直线 $y = |x|$ 和 $y = 1$ 围成。\n    *   $y = |x|$ 表示一个开口向上的“V”字形折线（顶点在原点）。\n    *   $y = 1$ 是一条水平直线。\n    *   因此，$D$ 是一个以 $(0,0), (1,1), (-1,1)$ 为顶点的三角形区域。\n    *   **对称性**：区域 $D$ 关于 $y$ 轴（即直线 $x=0$）对称。\n\n*   **被积函数 $f(x,y)$**：\n    $$f(x,y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2}$$\n    我们可以将其拆分为三部分进行观察：\n    $$f(x,y) = \\underbrace{\\frac{x^2 - y^2}{x^2 + y^2}}_{g_1(x,y)} - \\underbrace{\\frac{x\\cos y}{x^2 + y^2}}_{g_2(x,y)}$$\n\n### 2. 利用对称性判断奇偶性\n\n我们要计算 $\\iint_D f(x,y) dxdy$。根据二重积分的对称性性质：\n> 若积分区域 $D$ 关于 $y$ 轴对称，且被积函数 $f(x,y)$ 是关于 $x$ 的**奇函数**（即 $f(-x,y) = -f(x,y)$），则积分值为 0。\n\n让我们逐项分析拆分后的函数：\n\n*   **第一项 $g_1(x,y) = \\frac{x^2 - y^2}{x^2 + y^2}$**：\n    将 $x$ 替换为 $-x$：\n    $$g_1(-x,y) = \\frac{(-x)^2 - y^2}{(-x)^2 + y^2} = \\frac{x^2 - y^2}{x^2 + y^2} = g_1(x,y)$$\n    这是一个**偶函数**。这一项不能直接消去，需要计算。\n\n*   **第二项 $g_2(x,y) = \\frac{x\\cos y}{x^2 + y^2}$**：\n    将 $x$ 替换为 $-x$：\n    $$g_2(-x,y) = \\frac{(-x)\\cos y}{(-x)^2 + y^2} = -\\frac{x\\cos y}{x^2 + y^2} = -g_2(x,y)$$\n    这是一个关于 $x$ 的**奇函数**。\n    由于区域 $D$ 关于 $y$ 轴对称，根据对称性原理：\n    $$\\iint_D \\frac{x\\cos y}{x^2 + y^2} dxdy = 0$$\n\n### 3. 转化剩余积分\n\n经过上述分析，原积分简化为：\n$$I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dxdy - 0 = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dxdy$$\n\n此时，被积函数 $\\frac{x^2 - y^2}{x^2 + y^2}$ 仍然是关于 $x$ 的偶函数，但关于 $y$ 呢？\n$f(-x, y) = f(x, y)$ (偶)，$f(x, -y) = \\frac{x^2 - (-y)^2}{x^2 + (-y)^2} = f(x,y)$ (也是偶)。\n看起来对称性用完了？不，我们可以尝试**极坐标变换**或者再次利用对称性。\n\n**重新审视区域与函数：**\n等等，刚才的拆分可能不是最优路径。让我们再看一眼原式：\n$f(x,y) = \\frac{x^2-y^2}{x^2+y^2} - \\frac{x\\cos y}{x^2+y^2}$\n第二部分确实为0。第一部分 $\\frac{x^2-y^2}{x^2+y^2}$ 在区域 $D$ 上是否还能利用对称性？\n区域 $D$ 关于 $y$ 轴对称。\n函数 $u = \\frac{x^2-y^2}{x^2+y^2}$ 关于 $x$ 是偶函数。\n这似乎意味着我们需要真的去算这个积分？\n\n**稍等，有没有更巧妙的对称性？**\n让我们检查区域 $D$ 是否关于其他线对称？否。\n让我们检查函数是否有其他性质。\n$\\frac{x^2-y^2}{x^2+y^2}$ 在极坐标下是 $\\cos 2\\theta$。\n区域 $D$ 在极坐标下描述比较麻烦（$r$ 从 0 到 $1/\\cos\\theta$ 或 $1/\\sin\\theta$? 不对，$y=|x| \\implies \\theta = \\pi/4$ 和 $3\\pi/4$，$y=1 \\implies r\\sin\\theta = 1 \\implies r = 1/\\sin\\theta$）。\n积分范围：$\\theta \\in [\\pi/4, 3\\pi/4]$，$r \\in [0, 1/\\sin\\theta]$。\n\n让我们代入极坐标计算：\n$x = r\\cos\\theta, y = r\\sin\\theta$\n$dxdy = r dr d\\theta$\n被积函数：$\\frac{r^2(\\cos^2\\theta - \\sin^2\\theta)}{r^2} = \\cos 2\\theta$\n\n积分变为：\n$$I = \\int_{\\pi/4}^{3\\pi/4} d\\theta \\int_{0}^{\\frac{1}{\\sin\\theta}} (\\cos 2\\theta) \\cdot r dr$$\n\n先对 $r$ 积分：\n$$\\int_{0}^{\\frac{1}{\\sin\\theta}} r dr = \\left[ \\frac{1}{2}r^2 \\right]_0^{\\frac{1}{\\sin\\theta}} = \\frac{1}{2\\sin^2\\theta}$$\n\n再代回原式：\n$$I = \\int_{\\pi/4}^{3\\pi/4} \\cos 2\\theta \\cdot \\frac{1}{2\\sin^2\\theta} d\\theta$$\n利用三角恒等式 $\\sin^2\\theta = \\frac{1-\\cos 2\\theta}{2}$：\n$$I = \\int_{\\pi/4}^{3\\pi/4} \\frac{\\cos 2\\theta}{2 \\cdot \\frac{1-\\cos 2\\theta}{2}} d\\theta = \\int_{\\pi/4}^{3\\pi/4} \\frac{\\cos 2\\theta}{1-\\cos 2\\theta} d\\theta$$\n\n令 $t = 2\\theta$，则 $d\\theta = \\frac{1}{2}dt$。\n当 $\\theta = \\pi/4$ 时，$t = \\pi/2$。\n当 $\\theta = 3\\pi/4$ 时，$t = 3\\pi/2$。\n\n$$I = \\frac{1}{2} \\int_{\\pi/2}^{3\\pi/2} \\frac{\\cos t}{1-\\cos t} dt$$\n被积函数 $\\frac{\\cos t}{1-\\cos t}$ 在区间 $[\\pi/2, 3\\pi/2]$ 上的性质：\n该区间关于 $t = \\pi$ 对称。\n令 $t = \\pi + u$，则 $u \\in [-\\pi/2, \\pi/2]$。\n$\\cos(\\pi+u) = -\\cos u$。\n分母 $1 - \\cos(\\pi+u) = 1 - (-\\cos u) = 1 + \\cos u$。\n积分变为：\n$$I = \\frac{1}{2} \\int_{-\\pi/2}^{\\pi/2} \\frac{-\\cos u}{1+\\cos u} du$$\n被积函数 $h(u) = \\frac{-\\cos u}{1+\\cos u}$ 是**偶函数**（因为 $\\cos u$ 是偶函数）。\n所以 $I = \\int_{0}^{\\pi/2} \\frac{-\\cos u}{1+\\cos u} du$。\n\n这个积分显然不为0。难道我之前的“奇函数抵消”判断有误？\n回顾：$f(x,y) = \\frac{x^2-y^2}{x^2+y^2} - \\frac{x\\cos y}{x^2+y^2}$。\n第二部分确实是0。\n第一部分 $\\frac{x^2-y^2}{x^2+y^2}$ 在区域 $D$ 上积分不为0。\n\n**等等，让我重新检查一下题目和常见考点。**\n这类题目通常会有某种特殊的对称性导致结果为0。\n是不是我对区域的对称性理解错了？\n区域 $D$: $y \\ge |x|$ 且 $y \\le 1$。\n关于 $y$ 轴对称。\n函数 $g(x,y) = \\frac{x^2-y^2}{x^2+y^2}$。\n$g(-x, y) = g(x,y)$。\n没有奇偶抵消。\n\n**难道题目中的 $\\cos y$ 是关键？**\n不，$\\frac{x\\cos y}{x^2+y^2}$ 关于 $x$ 是奇函数，这部分确实是0。\n\n**让我重新核算一下极坐标积分的计算过程。**\n$I = \\int_{\\pi/4}^{3\\pi/4} \\frac{\\cos 2\\theta}{2\\sin^2\\theta} d\\theta$\n$\\frac{\\cos 2\\theta}{2\\sin^2\\theta} = \\frac{1-2\\sin^2\\theta}{2\\sin^2\\theta} = \\frac{1}{2\\sin^2\\theta} - 1 = \\frac{1}{2}\\csc^2\\theta - 1$.\n积分：\n$\\int (\\frac{1}{2}\\csc^2\\theta - 1) d\\theta = -\\frac{1}{2}\\cot\\theta - \\theta$.\n代入上下限 $\\pi/4$ 到 $3\\pi/4$：\n上限 ($3\\pi/4$): $-\\frac{1}{2}(-1) - \\frac{3\\pi}{4} = \\frac{1}{2} - \\frac{3\\pi}{4}$.\n下限 ($\\pi/4$): $-\\frac{1}{2}(1) - \\frac{\\pi}{4} = -\\frac{1}{2} - \\frac{\\pi}{4}$.\n结果 $= (\\frac{1}{2} - \\frac{3\\pi}{4}) - (-\\frac{1}{2} - \\frac{\\pi}{4}) = 1 - \\frac{2\\pi}{4} = 1 - \\frac{\\pi}{2}$.\n\n**这个结果非零。**\n但是，考研数学题（特别是这种标记了页码 P94 的习题）通常设计得非常巧妙，往往结果是 0。\n让我再次确认被积函数抄写是否正确。\n题目：$\\frac{x^2 - x\\cos y - y^2}{x^2 + y^2}$\n我的拆分：$\\frac{x^2-y^2}{x^2+y^2} - \\frac{x\\cos y}{x^2+y^2}$。\n没错。\n\n**有没有可能区域关于原点对称？**\n不，区域在第一、二象限上方。\n\n**有没有可能我看漏了什么对称性？**\n考虑变换 $x \\to -x$。\n区域不变。\n被积函数 $f(-x, y) = \\frac{(-x)^2 - (-x)\\cos y - y^2}{(-x)^2 + y^2} = \\frac{x^2 + x\\cos y - y^2}{x^2 + y^2}$.\n原函数 $f(x, y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2}$.\n$f(x,y) + f(-x,y) = \\frac{2(x^2-y^2)}{x^2+y^2}$.\n$f(x,y) - f(-x,y) = \\frac{-2x\\cos y}{x^2+y^2}$.\n所以 $\\iint f(x,y) = \\frac{1}{2} \\iint (f(x,y)+f(-x,y)) + \\frac{1}{2} \\iint (f(x,y)-f(-x,y))$.\n后半部分（奇函数部分）积分为0。\n前半部分 $\\iint \\frac{2(x^2-y^2)}{x^2+y^2}$ 依然需要计算。\n\n**会不会是题目本身有印刷错误，或者是特定的技巧？**\n比如，如果区域是圆域，那么 $\\iint \\frac{x^2-y^2}{x^2+y^2} = 0$。但这里是三角形。\n\n**再思考一种可能性：题目是否考察的是广义积分的奇点？**\n分母 $x^2+y^2$，分子在 $(0,0)$ 处趋于0吗？\n$\\lim_{(x,y)\\to(0,0)} \\frac{x^2-y^2-x\\cos y}{x^2+y^2}$。\n沿 $y=0$，极限为 $\\lim \\frac{x^2-1}{x^2} \\to 1$ (不对，$\\cos 0 = 1$)。\n沿 $y=x$，$\\lim \\frac{x^2-x\\cos x - x^2}{2x^2} = \\lim \\frac{-x\\cos x}{2x^2} \\to \\infty$。\n哦！原点 $(0,0)$ 是奇点吗？\n当 $(x,y) \\to (0,0)$ 时，$x^2+y^2 \\to 0$。\n分子 $x^2 - x\\cos y - y^2 \\approx x^2 - x - y^2$。\n如果在 $y=x$ 方向趋近，分子 $\\approx -x$，分母 $\\approx 2x^2$，整体 $\\approx -1/(2x) \\to \\infty$。\n**这是一个广义二重积分！**\n原题中 $D$ 包含原点 $(0,0)$。\n被积函数在原点附近无界。\n我们需要判断积分是否收敛。\n在极坐标下，被积函数行为类似于 $\\frac{r\\cos\\theta - \\dots}{r^2} \\approx \\frac{1}{r}$。\n面积元 $r dr d\\theta$。\n积分核 $\\approx \\frac{1}{r} \\cdot r = 1$。\n$\\int_0^\\epsilon 1 dr$ 是收敛的。\n所以积分存在。\n\n**那为什么我会算出 $1-\\pi/2$？**\n$1 - \\pi/2 \\approx 1 - 1.57 = -0.57$。\n这看起来是一个合理的数值。\n\n**但是，作为考研辅导，通常这类题会有更简单的解法，或者结果就是0。**\n让我再次检查极坐标变换的下限。\n区域是由 $y=|x|$ 和 $y=1$ 围成。\n这意味着 $y \\ge |x|$。\n在极坐标中，$r\\sin\\theta \\ge r|\\cos\\theta| \\implies \\tan\\theta \\ge |\\cos\\theta|$? 不对。\n$y \\ge x$ 且 $y \\ge -x$。\n即 $\\sin\\theta \\ge \\cos\\theta$ 且 $\\sin\\theta \\ge -\\cos\\theta$。\n$\\theta \\in [\\pi/4, 3\\pi/4]$。正确。\n$r$ 的范围：从原点出发直到 $y=1$。\n$r\\sin\\theta = 1 \\implies r = 1/\\sin\\theta$。正确。\n\n**有没有可能题目是 $\\iint \\frac{x\\cos y}{x^2+y^2}$ 这种形式单独出现？**\n不，题目是整体。\n\n**结论倾向：**\n按照严格的数学推导，答案应该是 $1 - \\frac{\\pi}{2}$。\n但是，如果是考研真题或模拟题，有时候会考察**对称性的误用**或者**特定函数的性质**。\n不过，既然知识库没有相关内容，我必须基于标准数学推导给出最严谨的答案。\n\n**等等，我突然想到另一种对称性视角。**\n考虑区域 $D$ 关于直线 $y=x$ 和 $y=-x$ 不对称。\n考虑函数 $f(x,y)$ 关于 $y$ 轴的对称性。\n$f(-x, y) = \\frac{x^2+x\\cos y-y^2}{x^2+y^2}$.\n$f(x,y) + f(-x,y) = \\frac{2(x^2-y^2)}{x^2+y^2}$.\n$f(x,y) - f(-x,y) = \\frac{-2x\\cos y}{x^2+y^2}$.\n积分 $I = \\frac{1}{2} \\iint_D [f(x,y)+f(-x,y)] dxdy + \\frac{1}{2} \\iint_D [f(x,y)-f(-x,y)] dxdy$.\n第二项为0。\n第一项 $J = \\iint_D \\frac{x^2-y^2}{x^2+y^2} dxdy$.\n我在极坐标下算出来是 $1-\\pi/2$。\n让我再算一遍积分 $\\int_{\\pi/4}^{3\\pi/4} (\\frac{1}{2}\\csc^2\\theta - 1) d\\theta$.\n原函数 $F(\\theta) = -\\frac{1}{2}\\cot\\theta - \\theta$.\n$F(3\\pi/4) = -\\frac{1}{2}(-1) - 3\\pi/4 = 1/2 - 3\\pi/4$.\n$F(\\pi/4) = -\\frac{1}{2}(1) - \\pi/4 = -1/2 - \\pi/4$.\n$F(3\\pi/4) - F(\\pi/4) = (1/2 - 3\\pi/4) - (-1/2 - \\pi/4) = 1 - \\pi/2$.\n计算无误。\n\n**最终确认：**\n这道题的结果确实不是0。很多学生看到 $\\cos y$ 和 $x$ 的组合，会下意识认为奇函数部分抵消后剩下0，从而忽略了对称部分 $\\frac{x^2-y^2}{x^2+y^2}$ 的计算。\n或者，题目可能有隐含条件我没看到？比如 $D$ 的定义？\"由 $y=|x|$ 及 $y=1$ 围成\"。这就是那个三角形。\n没问题。\n\n**解题结构规划：**\n1.  **拆解被积函数**：利用代数变形，将原式拆分为两部分。\n2.  **利用对称性消除一项**：指出含 $x$ 的奇函数项积分为0。\n3.  **极坐标变换计算剩余项**：这是难点，需详细写出极坐标设置、积分限和被积函数的转换。\n4.  **完成定积分计算**：得出最终结果。\n\n---\n**回答草稿：**\n\n**解：**\n\n**第一步：分析被积函数与积分区域**\n设被积函数为 $f(x,y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2}$。\n积分区域 $D$ 由 $y=|x|$ 和 $y=1$ 围成，即 $D = \\{(x,y) \\mid |x| \\le y \\le 1\\}$。\n区域 $D$ 关于 $y$ 轴对称。\n\n将被积函数拆分为两部分：\n$$f(x,y) = \\frac{x^2 - y^2}{x^2 + y^2} - \\frac{x\\cos y}{x^2 + y^2}$$\n\n**第二步：利用对称性简化**\n对于第二部分 $g(x,y) = \\frac{x\\cos y}{x^2 + y^2}$：\n由于 $g(-x, y) = \\frac{(-x)\\cos y}{(-x)^2 + y^2} = -g(x,y)$，它是关于 $x$ 的奇函数。\n又因为积分区域 $D$ 关于 $y$ 轴对称，根据二重积分的对称性性质，该项积分为 0。\n即 $\\iint_D \\frac{x\\cos y}{x^2 + y^2} dxdy = 0$。\n\n原积分简化为：\n$$I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dxdy$$\n\n**第三步：极坐标变换计算**\n采用极坐标变换：$x = r\\cos\\theta, y = r\\sin\\theta$。\n*   **确定积分限**：\n    *   边界 $y = |x|$ 对应 $\\theta = \\frac{\\pi}{4}$ 和 $\\theta = \\frac{3\\pi}{4}$。\n    *   边界 $y = 1$ 对应 $r\\sin\\theta = 1 \\Rightarrow r = \\frac{1}{\\sin\\theta}$。\n    *   故 $\\theta \\in [\\frac{\\pi}{4}, \\frac{3\\pi}{4}]$，$r \\in [0, \\frac{1}{\\sin\\theta}]$。\n*   **变换被积函数**：\n    $$\\frac{x^2 - y^2}{x^2 + y^2} = \\frac{r^2(\\cos^2\\theta - \\sin^2\\theta)}{r^2} = \\cos 2\\theta$$\n*   **变换面积元素**：\n    $$dxdy = r dr d\\theta$$\n\n代入积分表达式：\n$$I = \\int_{\\pi/4}^{3\\pi/4} d\\theta \\int_{0}^{\\frac{1}{\\sin\\theta}} (\\cos 2\\theta) \\cdot r dr$$\n\n**第四步：逐步计算**\n1.  先对 $r$ 积分：\n    $$\\int_{0}^{\\frac{1}{\\sin\\theta}} r dr = \\left[ \\frac{1}{2}r^2 \\right]_0^{\\frac{1}{\\sin\\theta}} = \\frac{1}{2\\sin^2\\theta}$$\n\n2.  再对 $\\theta$ 积分：\n    $$I = \\int_{\\pi/4}^{3\\pi/4} \\cos 2\\theta \\cdot \\frac{1}{2\\sin^2\\theta} d\\theta$$\n    利用倍角公式 $\\cos 2\\theta = 1 - 2\\sin^2\\theta$：\n    $$I = \\int_{\\pi/4}^{3\\pi/4} \\frac{1 - 2\\sin^2\\theta}{2\\sin^2\\theta} d\\theta = \\int_{\\pi/4}^{3\\pi/4} \\left( \\frac{1}{2\\sin^2\\theta} - 1 \\right) d\\theta$$\n    $$I = \\frac{1}{2} \\int_{\\pi/4}^{3\\pi/4} \\csc^2\\theta d\\theta - \\int_{\\pi/4}^{3\\pi/4} 1 d\\theta$$\n\n3.  计算定积分：\n    已知 $\\int \\csc^2\\theta d\\theta = -\\cot\\theta$。\n    $$I = \\frac{1}{2} [-\\cot\\theta]_{\\pi/4}^{3\\pi/4} - [\\theta]_{\\pi/4}^{3\\pi/4}$$\n    $$I = \\frac{1}{2} \\left[ (-\\cot\\frac{3\\pi}{4}) - (-\\cot\\frac{\\pi}{4}) \\right] - \\left( \\frac{3\\pi}{4} - \\frac{\\pi}{4} \\right)$$\n    $$I = \\frac{1}{2} \\left[ -(-1) - (-1) \\right] - \\frac{\\pi}{2}$$\n    $$I = \\frac{1}{2} (1 + 1) - \\frac{\\pi}{2} = 1 - \\frac{\\pi}{2}$$\n\n**最终答案**\n$$1 - \\frac{\\pi}{2}$$\n\n📚 考点出处\n*   **学科**：数学一/二/三 - 高等数学\n*   **知识点**：二重积分的计算（极坐标变换）、二重积分的对称性、三角函数积分。\n*   **关联**：本题展示了如何利用区域对称性剔除奇函数项，并将剩余的非对称部分转化为极坐标下的累次积分求解。\n\n（注：此题为典型的“陷阱题”，容易因误判对称性而得0，实际需通过极坐标精确计算。）',NULL,'2026-06-06 21:20:18'),(45,10,'user','怎么做？','/uploads/images/202606/dd9d50f0f328471ab96caabb9b3a230b.png','2026-06-06 21:22:03'),(46,10,'assistant','这是一个经典的二重积分计算问题，关键在于利用**对称性**简化被积函数。\n\n### 解题步骤：\n\n1.  **分析积分区域 $D$**\n    *   区域由 $y = |x|$ 和 $y = 1$ 围成。\n    *   $y = |x|$ 是一个V字形折线，顶点在原点 $(0,0)$。\n    *   $y=1$ 是一条水平直线。\n    *   因此，区域 $D$ 是一个以 $(0,0), (1,1), (-1,1)$ 为顶点的三角形区域。\n    *   **关键性质**：该区域关于 $y$ 轴（即 $x=0$）对称。\n\n2.  **分析被积函数 $f(x,y)$**\n    *   被积函数为 $f(x,y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2}$。\n    *   我们可以将其拆分为三部分分别讨论：\n        $$ f(x,y) = \\underbrace{\\frac{x^2 - y^2}{x^2 + y^2}}_{g(x,y)} - \\underbrace{\\frac{x\\cos y}{x^2 + y^2}}_{h(x,y)} $$\n\n3.  **利用对称性简化**\n    *   **考察项 $h(x,y) = \\frac{x\\cos y}{x^2 + y^2}$**：\n        *   分母 $x^2 + y^2$ 是关于 $x$ 的偶函数。\n        *   $\\cos y$ 与 $x$ 无关，视为常数。\n        *   分子中的 $x$ 是关于 $x$ 的**奇函数**。\n        *   因此，整个函数 $h(x,y)$ 是关于 $x$ 的**奇函数**。\n        *   由于积分区域 $D$ 关于 $y$ 轴对称，根据对称性原理，奇函数在对称区间上的积分为 **0**。\n        *   即：$\\iint_D \\frac{x\\cos y}{x^2 + y^2} dxdy = 0$。\n\n    *   **考察项 $g(x,y) = \\frac{x^2 - y^2}{x^2 + y^2}$**：\n        *   该函数关于 $x$ 是偶函数（因为 $x$ 都是平方项）。\n        *   所以不能直接消去，需要继续计算。\n\n4.  **转换坐标系计算剩余部分**\n    *   现在积分简化为：$I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dxdy$。\n    *   观察被积函数 $\\frac{x^2 - y^2}{x^2 + y^2}$，这提示我们使用**极坐标变换**。\n    *   令 $x = r\\cos\\theta, y = r\\sin\\theta$。\n    *   被积函数变为：$\\frac{r^2\\cos^2\\theta - r^2\\sin^2\\theta}{r^2} = \\cos(2\\theta)$。\n    *   面积元素 $dxdy = r dr d\\theta$。\n\n5.  **确定极坐标下的积分限**\n    *   **角度 $\\theta$**：\n        *   区域从 $y=|x|$ 的下边界开始。$y=x$ 对应 $\\theta = \\pi/4$，$y=-x$ 对应 $\\theta = 3\\pi/4$（或者 $-\\pi/4$ 到 $\\pi/4$，取决于怎么看，这里取第一、二象限的部分）。\n        *   更直观地看，区域在第一、二象限，夹在直线 $y=x$ 和 $y=-x$ 之间，上方截止于 $y=1$。\n        *   所以 $\\theta$ 的范围是 $[\\frac{\\pi}{4}, \\frac{3\\pi}{4}]$。\n    *   **半径 $r$**：\n        *   射线从原点出发，穿过区域内部，直到边界 $y=1$。\n        *   由 $y = r\\sin\\theta = 1$，得 $r = \\frac{1}{\\sin\\theta} = \\csc\\theta$。\n        *   所以 $r$ 的范围是 $[0, \\csc\\theta]$。\n\n6.  **执行积分计算**\n    $$ I = \\int_{\\pi/4}^{3\\pi/4} d\\theta \\int_{0}^{\\csc\\theta} \\cos(2\\theta) \\cdot r dr $$\n    \n    *   **先对 $r$ 积分**：\n        $$ \\int_{0}^{\\csc\\theta} r dr = \\left[ \\frac{1}{2}r^2 \\right]_0^{\\csc\\theta} = \\frac{1}{2}\\csc^2\\theta $$\n    \n    *   **再对 $\\theta$ 积分**：\n        $$ I = \\int_{\\pi/4}^{3\\pi/4} \\cos(2\\theta) \\cdot \\frac{1}{2}\\csc^2\\theta d\\theta $$\n        $$ I = \\frac{1}{2} \\int_{\\pi/4}^{3\\pi/4} \\frac{\\cos(2\\theta)}{\\sin^2\\theta} d\\theta $$\n        \n        利用倍角公式 $\\cos(2\\theta) = 1 - 2\\sin^2\\theta$ 进行化简：\n        $$ \\frac{\\cos(2\\theta)}{\\sin^2\\theta} = \\frac{1 - 2\\sin^2\\theta}{\\sin^2\\theta} = \\csc^2\\theta - 2 $$\n        \n        代入积分式：\n        $$ I = \\frac{1}{2} \\int_{\\pi/4}^{3\\pi/4} (\\csc^2\\theta - 2) d\\theta $$\n        \n        计算原函数：\n        $$ \\int (\\csc^2\\theta - 2) d\\theta = -\\cot\\theta - 2\\theta $$\n        \n        代入上下限：\n        $$ I = \\frac{1}{2} \\left[ (-\\cot\\theta - 2\\theta) \\right]_{\\pi/4}^{3\\pi/4} $$\n        \n        上界 ($\\theta = 3\\pi/4$): $-\\cot(3\\pi/4) - 2(3\\pi/4) = -(-1) - \\frac{3\\pi}{2} = 1 - \\frac{3\\pi}{2}$\n        下界 ($\\theta = \\pi/4$): $-\\cot(\\pi/4) - 2(\\pi/4) = -(1) - \\frac{\\pi}{2} = -1 - \\frac{\\pi}{2}$\n        \n        相减：\n        $$ I = \\frac{1}{2} \\left[ (1 - \\frac{3\\pi}{2}) - (-1 - \\frac{\\pi}{2}) \\right] $$\n        $$ I = \\frac{1}{2} \\left[ 1 - \\frac{3\\pi}{2} + 1 + \\frac{\\pi}{2} \\right] $$\n        $$ I = \\frac{1}{2} \\left[ 2 - \\pi \\right] = 1 - \\frac{\\pi}{2} $$\n\n### 最终结论：\n该二重积分的结果为 $1 - \\frac{\\pi}{2}$。\n\n📚 考点出处\n*   **学科**：数学（微积分）\n*   **知识点**：\n    1.  二重积分的计算（直角坐标与极坐标转换）\n    2.  利用区域的对称性简化积分（奇偶性）\n    3.  三角恒等变换（倍角公式）\n    4.  基本积分公式（$\\int \\csc^2 x dx = -\\cot x$）',NULL,'2026-06-06 21:22:16'),(47,11,'user','怎么做？','/uploads/images/202606/5c2907bd37a0401682241391638ecc9a.png','2026-06-06 21:30:48'),(48,11,'assistant','这是一个经典的二重积分问题，利用**对称性**和**极坐标变换**可以极大地简化计算过程。以下是详细的推导步骤：\n\n### 1. 分析积分区域 $D$\n首先明确积分区域 $D$ 的几何形状：\n- 边界由 $y = |x|$（两条直线 $y=x$ 和 $y=-x$）与 $y=1$ 围成。\n- 这是一个以原点为顶点，开口向上的三角形区域。\n- 区域范围：$x \\in [-1, 1]$，且对于任意 $x$， $|x| \\le y \\le 1$。\n- **关键性质**：区域 $D$ 关于 $y$ 轴对称（即关于 $x=0$ 对称）。\n\n### 2. 分析被积函数 $f(x,y)$\n设被积函数为 $f(x,y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2}$。\n我们可以将其拆分为三部分进行观察：\n$$ f(x,y) = \\frac{x^2 - y^2}{x^2 + y^2} - \\frac{x\\cos y}{x^2 + y^2} $$\n\n**利用奇偶性简化：**\n- **第一部分** $\\frac{x^2 - y^2}{x^2 + y^2}$：\n  令 $g(x,y) = \\frac{x^2 - y^2}{x^2 + y^2}$。\n  由于 $g(-x, y) = \\frac{(-x)^2 - y^2}{(-x)^2 + y^2} = \\frac{x^2 - y^2}{x^2 + y^2} = g(x,y)$，该部分是关于 $x$ 的**偶函数**。\n  \n- **第二部分** $-\\frac{x\\cos y}{x^2 + y^2}$：\n  令 $h(x,y) = \\frac{x\\cos y}{x^2 + y^2}$。\n  由于 $h(-x, y) = \\frac{(-x)\\cos y}{(-x)^2 + y^2} = -\\frac{x\\cos y}{x^2 + y^2} = -h(x,y)$，该部分是关于 $x$ 的**奇函数**。\n\n**根据对称性定理**：\n若积分区域 $D$ 关于 $y$ 轴对称，且被积函数关于 $x$ 是奇函数，则该部分积分为 0。\n即：$\\iint_D -\\frac{x\\cos y}{x^2 + y^2} dxdy = 0$。\n\n因此，原积分简化为：\n$$ I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dxdy $$\n\n### 3. 利用极坐标计算简化后的积分\n由于区域涉及 $x^2+y^2$，且被积函数形式适合极坐标，我们采用极坐标变换：\n- $x = r\\cos\\theta$\n- $y = r\\sin\\theta$\n- $dxdy = r dr d\\theta$\n- $\\frac{x^2 - y^2}{x^2 + y^2} = \\frac{r^2(\\cos^2\\theta - \\sin^2\\theta)}{r^2} = \\cos(2\\theta)$\n\n**确定积分限**：\n- 角度 $\\theta$：区域夹在 $y=|x|$ 之间。\n  - 当 $x>0$ 时，$y=x \\Rightarrow \\tan\\theta = 1 \\Rightarrow \\theta = \\frac{\\pi}{4}$。\n  - 当 $x<0$ 时，$y=-x \\Rightarrow \\tan\\theta = -1 \\Rightarrow \\theta = \\frac{3\\pi}{4}$。\n  - 所以 $\\theta \\in [\\frac{\\pi}{4}, \\frac{3\\pi}{4}]$。\n- 半径 $r$：下界是射线 $y=|x|$，上界是直线 $y=1$。\n  - 将 $y=1$ 代入极坐标：$r\\sin\\theta = 1 \\Rightarrow r = \\frac{1}{\\sin\\theta} = \\csc\\theta$。\n  - 所以 $r \\in [0, \\csc\\theta]$。\n\n**建立积分式**：\n$$ I = \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} d\\theta \\int_{0}^{\\csc\\theta} \\cos(2\\theta) \\cdot r dr $$\n\n**计算内层积分**（对 $r$ 积分）：\n$$ \\int_{0}^{\\csc\\theta} r dr = \\left[ \\frac{1}{2}r^2 \\right]_0^{\\csc\\theta} = \\frac{1}{2}\\csc^2\\theta $$\n\n**代入外层积分**（对 $\\theta$ 积分）：\n$$ I = \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} \\cos(2\\theta) \\cdot \\frac{1}{2}\\csc^2\\theta d\\theta $$\n利用三角恒等式化简被积函数：\n$$ \\cos(2\\theta)\\csc^2\\theta = (\\cos^2\\theta - \\sin^2\\theta) \\cdot \\frac{1}{\\sin^2\\theta} = \\cot^2\\theta - 1 $$\n或者使用 $\\cot^2\\theta = \\csc^2\\theta - 1$，则被积函数变为 $(\\csc^2\\theta - 1) - 1 = \\csc^2\\theta - 2$。\n\n所以：\n$$ I = \\frac{1}{2} \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} (\\csc^2\\theta - 2) d\\theta $$\n\n**计算定积分**：\n原函数为 $-\\cot\\theta - 2\\theta$。\n$$ I = \\frac{1}{2} \\left[ -\\cot\\theta - 2\\theta \\right]_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} $$\n\n代入上下限：\n- 上限 $\\frac{3\\pi}{4}$：$-\\cot(\\frac{3\\pi}{4}) - 2(\\frac{3\\pi}{4}) = -(-1) - \\frac{3\\pi}{2} = 1 - \\frac{3\\pi}{2}$\n- 下限 $\\frac{\\pi}{4}$：$-\\cot(\\frac{\\pi}{4}) - 2(\\frac{\\pi}{4}) = -(1) - \\frac{\\pi}{2} = -1 - \\frac{\\pi}{2}$\n\n相减：\n$$ \\text{括号内值} = (1 - \\frac{3\\pi}{2}) - (-1 - \\frac{\\pi}{2}) = 2 - \\pi $$\n\n最终结果：\n$$ I = \\frac{1}{2} (2 - \\pi) = 1 - \\frac{\\pi}{2} $$\n\n---\n\n### 📚 考点出处\n- **知识点**：二重积分的计算（对称性应用、极坐标变换）\n- **学科**：数学（考研数学一/二/三）\n- **关联链路**：\n  1. **对称性判定**：识别区域关于 $y$ 轴对称，结合被积函数中奇次项 $x$ 的消去（这是解题的关键捷径）。\n  2. **极坐标转换**：处理分母为 $x^2+y^2$ 以及圆形/扇形区域时的标准方法。\n  3. **三角恒等变形**：将 $\\cos(2\\theta)/\\sin^2\\theta$ 转化为 $\\csc^2\\theta$ 或 $\\cot^2\\theta$ 以便积分。',NULL,'2026-06-06 21:31:02');
/*!40000 ALTER TABLE `ai_chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_session`
--

DROP TABLE IF EXISTS `ai_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '新对话' COMMENT '会话标题（取用户第一条消息前20字）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_updated` (`user_id`,`updated_at`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI对话会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_session`
--

LOCK TABLES `ai_chat_session` WRITE;
/*!40000 ALTER TABLE `ai_chat_session` DISABLE KEYS */;
INSERT INTO `ai_chat_session` VALUES (1,8,'新对话',1,'2026-06-05 22:19:07','2026-06-05 22:19:07'),(2,8,'新对话',1,'2026-06-05 22:19:57','2026-06-05 22:24:56'),(3,8,'进程和线程的区别是什么？',1,'2026-06-05 22:27:30','2026-06-05 22:27:32'),(4,8,'不会做',1,'2026-06-06 14:01:10','2026-06-06 14:24:27'),(5,8,'怎么做？',1,'2026-06-06 15:25:40','2026-06-06 15:54:41'),(6,8,'怎么做？',1,'2026-06-06 15:55:43','2026-06-06 16:30:24'),(7,8,'怎么做',1,'2026-06-06 19:19:18','2026-06-06 19:19:32'),(8,8,'怎么做',0,'2026-06-06 21:11:43','2026-06-06 21:11:49'),(9,8,'怎么写？',0,'2026-06-06 21:15:47','2026-06-06 21:19:28'),(10,8,'怎么做？',0,'2026-06-06 21:21:54','2026-06-06 21:22:03'),(11,8,'怎么做？',0,'2026-06-06 21:30:38','2026-06-06 21:30:47');
/*!40000 ALTER TABLE `ai_chat_session` ENABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI智能体主动干预日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_intervention_log`
--

LOCK TABLES `ai_intervention_log` WRITE;
/*!40000 ALTER TABLE `ai_intervention_log` DISABLE KEYS */;
INSERT INTO `ai_intervention_log` VALUES (1,8,'Psychology','打卡感言分析（连续1天）','【AI 调用失败】401 Authorization Required: [no body]','READ','2026-05-21 07:07:30'),(2,8,'Psychology','打卡感言分析（连续1天）','你的状态真好，这份从容和舒适感，正是高效复习的最佳基础呢！','UNREAD','2026-06-04 06:35:12'),(3,8,'Psychology','打卡感言分析（连续2天）','我深深理解你现在的心情，伤心和难过是很自然的情绪。请记得，你不是一个人在战斗。试着放慢脚步，给自己一个拥抱，深呼吸几次。在复习上，不妨把学习计划拆分成更小的部分，每天完成一点，这样能让你更有成就感。如果觉得压力大，和信任的人分享一下，他们一定能给你力量。一切都会好起来的。','UNREAD','2026-06-05 12:27:25');
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
INSERT INTO `ai_knowledge_point` VALUES (1,'数据结构','树','B树与B+树的区别','1. B树所有节点都存储数据，B+树只有叶子节点存储数据；\n2. B+树叶节点通过链表相连，便于范围查询；\n3. B+树的非叶子节点仅存储索引，单个节点可容纳更多key，树高更低；\n4. B树适合单点查询，B+树适合范围查询和顺序遍历；\n5. MySQL InnoDB 使用 B+树作为索引结构。','B树,B+树,索引,数据库,范围查询,叶子节点','HIGH','2026-06-04 13:13:33'),(2,'数据结构','排序','快速排序的原理与复杂度','快速排序采用分治策略：选择基准元素pivot，将数组分为两部分（小于pivot和大于pivot），递归排序。\n时间复杂度：平均O(nlogn)，最坏O(n²)（已排序数组+选首元素为pivot）。\n空间复杂度：O(logn)（递归栈）。\n不稳定排序。\n优化：三数取中选pivot、小区间用插入排序。','快速排序,分治,时间复杂度,不稳定排序,递归','HIGH','2026-06-04 13:13:33'),(3,'数据结构','图','Dijkstra最短路径算法','Dijkstra算法求解单源最短路径，适用于非负权边的有向/无向图。\n核心思想：贪心，每次选取未访问的距离最小节点，更新其邻居的距离。\n时间复杂度：O(V²)（朴素），O((V+E)logV)（优先队列优化）。\n不能处理负权边（需用Bellman-Ford）。','Dijkstra,最短路径,贪心,优先队列,负权边','HIGH','2026-06-04 13:13:33'),(4,'数据结构','线性表','栈和队列的应用场景','栈：函数调用栈、表达式求值（后缀表达式）、括号匹配、浏览器前进后退、DFS。\n队列：BFS、消息队列、缓冲区、CPU任务调度。\n循环队列判满：(rear+1)%maxSize == front。\n双端队列deque两端都可进出。','栈,队列,DFS,BFS,循环队列,函数调用','MEDIUM','2026-06-04 13:13:33'),(5,'操作系统','进程管理','进程与线程的区别','1. 进程是资源分配的基本单位，线程是CPU调度的基本单位；\n2. 进程有独立地址空间，线程共享进程的地址空间；\n3. 进程切换开销大（需切换页表等），线程切换开销小；\n4. 线程间通信可直接读写共享变量，进程间通信需要IPC机制；\n5. 一个进程崩溃不影响其他进程，一个线程崩溃可能导致整个进程崩溃。','进程,线程,地址空间,上下文切换,IPC','HIGH','2026-06-04 13:13:33'),(6,'操作系统','内存管理','虚拟内存与页面置换算法','虚拟内存将逻辑地址与物理地址分离，允许进程使用大于物理内存的地址空间。\n页面置换算法：\n1. FIFO：先进先出，可能Belady异常；\n2. LRU：最近最久未使用，用栈或计数器实现；\n3. Clock（时钟）：LRU的近似，用访问位；\n4. OPT：理论最优，无法实现（需预知未来）。\n缺页率 = 缺页次数 / 总访问次数。','虚拟内存,页面置换,FIFO,LRU,Clock,缺页中断','HIGH','2026-06-04 13:13:33'),(7,'操作系统','同步互斥','死锁的四个必要条件与预防','死锁四个必要条件：互斥、占有并等待、不可剥夺、循环等待。\n预防策略：\n1. 破坏占有并等待：一次性申请所有资源；\n2. 破坏不可剥夺：允许抢占；\n3. 破坏循环等待：按序申请资源。\n银行家算法用于死锁避免（安全性检查）。','死锁,互斥,银行家算法,循环等待,资源分配','HIGH','2026-06-04 13:13:33'),(8,'计算机网络','传输层','TCP三次握手与四次挥手','三次握手：\n1. 客户端→SYN→服务器；2. 服务器→SYN+ACK→客户端；3. 客户端→ACK→服务器。\n为什么三次：防止历史连接被服务器接受。\n四次挥手：\n1. 主动方→FIN；2. 被动方→ACK；3. 被动方→FIN；4. 主动方→ACK。\nTIME_WAIT等待2MSL确保对方收到最后的ACK。\n为什么四次：TCP全双工，每个方向需单独关闭。','TCP,三次握手,四次挥手,SYN,FIN,TIME_WAIT,全双工','HIGH','2026-06-04 13:13:33'),(9,'计算机网络','应用层','HTTP与HTTPS的区别','HTTP：明文传输，端口80，无加密。\nHTTPS = HTTP + TLS/SSL，端口443。\nHTTPS握手过程：\n1. 客户端发送支持的加密套件列表；\n2. 服务器返回证书+选定加密套件；\n3. 客户端验证证书，生成随机密钥，用服务器公钥加密发送；\n4. 双方用该密钥对称加密通信。\n对称加密用于数据传输，非对称加密用于密钥交换。','HTTP,HTTPS,TLS,SSL,对称加密,非对称加密,证书','MEDIUM','2026-06-04 13:13:33'),(10,'计算机组成原理','存储系统','Cache的工作原理与映射方式','Cache解决CPU与主存速度不匹配问题。\n映射方式：\n1. 直接映射：每个主存块只能映射到Cache的固定位置，冲突率高；\n2. 全相联映射：可映射到任意位置，命中率高但硬件复杂；\n3. 组相联映射：折中方案，Cache分组，组内全相联。\n替换算法：LRU、FIFO、随机。\n写策略：写直达（同时写Cache和主存）、写回（只写Cache，脏位标记）。','Cache,直接映射,全相联,组相联,LRU,写回,写直达','HIGH','2026-06-04 13:13:33'),(11,'计算机组成原理','指令系统','CISC与RISC的区别','CISC（复杂指令集）：指令数量多、长度可变、执行周期长、微程序控制。代表：x86。\nRISC（精简指令集）：指令数量少、长度固定、单周期执行、硬布线控制、大量通用寄存器。代表：ARM、RISC-V。\nRISC特点：Load/Store架构、流水线效率高、编译器优化空间大。','CISC,RISC,指令集,x86,ARM,流水线,Load/Store','MEDIUM','2026-06-04 13:13:33'),(12,'高等数学','微积分','洛必达法则的使用条件','洛必达法则用于求0/0或无穷/无穷型不定式极限。\n条件：\n1. lim f(x)/g(x) 为0/0或无穷/无穷型；\n2. f(x)和g(x)在去心邻域内可导；\n3. g\'(x) 不等于 0；\n4. lim f\'(x)/g\'(x) 存在（或为无穷）。\n注意：使用前必须验证是不定型，否则会得出错误结果。多次使用需每次验证。','洛必达法则,极限,不定式,0/0,导数','HIGH','2026-06-04 13:13:33'),(13,'高等数学','级数','泰勒展开公式','f(x)在x₀处的泰勒展开：\nf(x) = Σ f⁽ⁿ⁾(x₀)/n! · (x-x₀)ⁿ\n常用展开（x₀=0，即麦克劳林展开）：\ne^x = 1 + x + x²/2! + x³/3! + ...\nsin x = x - x³/3! + x⁵/5! - ...\ncos x = 1 - x²/2! + x⁴/4! - ...\nln(1+x) = x - x²/2 + x³/3 - ... (|x|≤1, x≠-1)\n1/(1-x) = 1 + x + x² + x³ + ... (|x|<1)','泰勒展开,麦克劳林,级数,e^x,sin,cos,ln','HIGH','2026-06-04 13:13:33'),(14,'线性代数','矩阵','矩阵的秩与线性方程组解的关系','对于m×n矩阵A：\n1. r(A) = n（列满秩）→ 齐次方程Ax=0只有零解；\n2. r(A) < n → 齐次方程有非零解，基础解系含n-r(A)个向量；\n3. 非齐次方程Ax=b有解 ⟺ r(A) = r(A|b)；\n4. r(A) = r(A|b) = n → 唯一解；\n5. r(A) = r(A|b) < n → 无穷多解。','矩阵的秩,线性方程组,基础解系,列满秩,增广矩阵','HIGH','2026-06-04 13:13:33'),(15,'概率论','随机变量','常见概率分布及其期望方差','离散型：\n1. 二项分布B(n,p)：E=np, D=np(1-p)\n2. 泊松分布P(λ)：E=λ, D=λ\n3. 几何分布G(p)：E=1/p, D=(1-p)/p²\n连续型：\n1. 均匀分布U(a,b)：E=(a+b)/2, D=(b-a)²/12\n2. 指数分布Exp(λ)：E=1/λ, D=1/λ²\n3. 正态分布N(μ,σ²)：E=μ, D=σ²','二项分布,泊松分布,正态分布,期望,方差,概率密度','HIGH','2026-06-04 13:13:33'),(16,'英语','阅读理解','考研英语阅读常见题型与解题技巧','主旨题：找首段、末段、各段首句，注意转折词but/however。\n细节题：定位关键词，答案通常是原文同义替换。\n推断题：注意infer/imply/suggest，答案不会太绝对。\n态度题：注意作者用词的感情色彩。\n词义题：结合上下文语境推断，注意前后解释说明。\n排除法：绝对化选项（all/never/must）通常是干扰项。','阅读理解,主旨题,细节题,推断题,同义替换,排除法','MEDIUM','2026-06-04 13:13:33'),(17,'政治','马克思主义原理','唯物辩证法三大规律','1. 对立统一规律（核心）：矛盾是事物发展的根本动力，矛盾的普遍性与特殊性辩证统一。\n2. 量变质变规律：量变是质变的必要准备，质变是量变的必然结果，量变引起质变，质变巩固量变。\n3. 否定之否定规律：事物发展是螺旋式上升、波浪式前进，经历\"肯定—否定—否定之否定\"。\n方法论：具体问题具体分析、两点论与重点论统一。','唯物辩证法,对立统一,量变质变,否定之否定,矛盾,方法论','HIGH','2026-06-04 13:13:33'));
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
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户行为事件';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_user_event`
--

LOCK TABLES `ai_user_event` WRITE;
/*!40000 ALTER TABLE `ai_user_event` DISABLE KEYS */;
INSERT INTO `ai_user_event` VALUES (1,8,'VIEW_POST','{\"postId\": 19}','2026-06-05 20:16:41'),(2,8,'VIEW_POST','{\"postId\": 20}','2026-06-06 23:12:48'),(3,8,'VIEW_POST','{\"postId\": 21}','2026-06-06 23:15:13'),(4,8,'VIEW_POST','{\"postId\": 21}','2026-06-06 23:18:56'),(5,8,'VIEW_POST','{\"postId\": 22}','2026-06-06 23:19:13');
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
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='帖子主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `forum_post`
--

LOCK TABLES `forum_post` WRITE;
/*!40000 ALTER TABLE `forum_post` DISABLE KEYS */;
INSERT INTO `forum_post` VALUES (1,1,3,'2026年408复习规划（个人向分享）','<p>大家好，这是我的408复习规划，基础阶段建议看王道，强化阶段多刷真题...</p><p>附上了我的复习时间表，供大家参考！</p>','[\"408\", \"复习规划\", \"经验贴\"]','[\"https://oss.example.com/file/plan.pdf\"]',1527,3,3,0,'2026-04-17 20:44:07','2026-04-18 19:01:49'),(2,2,4,'求助！张宇18讲的线代部分看不懂怎么办？','<p>特别是特征值和特征向量那一部分，做题完全没有思路，感觉非常吃力，求大佬指点迷津！</p>','[\"数学一\", \"线性代数\", \"求助\"]','[]',330,1,1,0,'2026-04-17 20:44:07','2026-04-18 18:15:38'),(3,3,5,'分享一份自己整理的英语大小作文万能句型','<p>背熟这些句型，考试的时候直接套用，亲测有效！详见附件下载，完全免费分享给大家~</p>','[\"英语一\", \"作文\", \"干货\"]','[\"https://oss.example.com/file/english_writing.docx\"]',2000,2,0,0,'2026-04-17 20:44:07','2026-04-17 20:44:07'),(4,4,2,'肖秀荣1000题刷题打卡集中贴（2026版）','<p>欢迎大家每天在这里打卡自己的刷题进度，互相监督，共同进步！每天坚持100道题！</p>','[\"政治\", \"打卡\", \"官方贴\"]','[]',501,0,0,0,'2026-04-17 20:44:07','2026-04-18 15:24:39'),(5,1,6,'测试','测试','[]',NULL,17,2,0,0,'2026-04-17 22:28:18','2026-05-20 15:30:14'),(6,2,6,'测试','测试','[]',NULL,11,1,0,0,'2026-04-18 12:02:59','2026-04-18 18:47:39'),(7,1,6,'测试2','测试2','[]',NULL,30,1,0,0,'2026-04-18 12:11:10','2026-04-18 18:28:23'),(8,1,6,'测试','测试','[]',NULL,19,1,0,0,'2026-04-18 17:07:34','2026-04-18 18:58:35'),(9,1,6,'测试','测试','[]',NULL,22,2,0,0,'2026-04-18 18:04:30','2026-05-20 15:30:03'),(10,1,10,'这是一条***广告***信息','************快来',NULL,NULL,3,0,0,0,'2026-05-20 11:51:29','2026-05-20 15:30:19'),(11,1,8,'测试','# 测试 ### 这是一个测试 #### 这是一个测试 - sad - sadsd - sadds - sadsad 1. a 2. b 3. c 4. d *nb* **nb** ~~撒旦~~ ###### 撒旦','[]',NULL,4,0,0,0,'2026-05-20 19:52:00','2026-05-20 20:00:55'),(12,1,8,'测试','# wu ### wudongbo ```c #include int main(){ printf(\"HelloWorld\"); } ``` 1. wudongbo 2. wudongbo 3. wudongbo - wudongbo - wudongbo -','[]',NULL,3,0,0,0,'2026-05-20 20:46:07','2026-05-20 21:08:46'),(13,1,8,'图片测试','![](/uploads/images/202605/6db2e655ad784b42bdb9cf730e8674ee.jpg) 图片测试','[]',NULL,1,0,0,0,'2026-05-20 21:10:02','2026-05-20 21:10:04'),(14,1,8,'这是一个测试','<pre><code>#include&lt;stdio.h&gt;\nint main(){\n  printf(\"Hello Wordl\");\n  }\n</code></pre>\n<ol>\n <li>a</li>\n <li>b</li>\n <li>c</li>\n <li>d</li>\n</ol>\n<p><img alt=\"\"></p>\n<h1>a</h1>\n<h3>b</h3>','[]',NULL,7,0,0,0,'2026-05-20 21:18:36','2026-05-20 21:27:20'),(15,1,8,'测试2','<h1>测试</h1>\n<h2>测试</h2>\n<ol>\n <li>a</li>\n <li>b</li>\n <li>c</li>\n <li>d</li>\n</ol>\n<ul>\n <li>1</li>\n <li>2</li>\n <li>3</li>\n <li>4</li>\n</ul>\n<pre><code>#include&lt;sostream&gt;\nint mian(){\n  \n  }\n</code></pre>\n<p><img alt=\"\"></p>','[]',NULL,4,1,0,0,'2026-05-20 21:27:08','2026-05-21 18:11:57'),(16,1,8,'图片测试','<p><img alt=\"\"></p>','[]',NULL,4,0,0,0,'2026-05-20 21:29:13','2026-05-20 21:33:50'),(17,1,8,'图片测试3','![](/uploads/images/202605/f962a6c90d124ca599d323d1132f2b34.jpg)','[]',NULL,3,0,0,0,'2026-05-20 21:41:42','2026-05-21 18:12:00'),(18,1,8,'测试4','# 1 ## 2 ### 3 1. a 2. b 3. c - 1 - 2 - 3 - 4 ```c #include int mian(){ } ``` ![](/uploads/images/202605/1d68e8f8a60a4837b4f196085fb86313.jpg)','[]',NULL,2,0,0,0,'2026-05-20 21:43:32','2026-05-20 21:43:50'),(19,1,8,'测试5','<h1>ceshi</h1>\n<h2>d</h2>\n<ol>\n <li>a</li>\n <li>b</li>\n <li>c</li>\n</ol>\n<ul>\n <li>1</li>\n <li>2</li>\n <li>3</li>\n</ul>\n<pre><code class=\"language-c\">#include&lt;sostream&gt;\nint mian(){\n  \n  }\n</code></pre>\n<p><img src=\"/uploads/images/202605/b422ff2adae8448ab1211b48e4bb83bb.jpg\" alt=\"\"></p>','[]',NULL,6,0,0,0,'2026-05-20 21:46:34','2026-06-05 20:16:41'),(20,1,8,'测试视频','<p></p>\n<p></p>','[]',NULL,1,0,0,0,'2026-06-06 23:12:46','2026-06-06 23:12:48'),(21,1,8,'测试视频2','<p></p>','[]',NULL,2,0,0,0,'2026-06-06 23:15:11','2026-06-06 23:18:55'),(22,1,8,'测试视频🤔','<p>\n <video src=\"/uploads/videos/202606/78bc6cd3b2ca47aaad1d528d5e6ebf13.mp4\" controls=\"\" width=\"100%\"></video></p>','[]',NULL,1,0,0,0,'2026-06-06 23:19:11','2026-06-06 23:19:12');
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
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='每日学习打卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_check_in`
--

LOCK TABLES `interaction_check_in` WRITE;
/*!40000 ALTER TABLE `interaction_check_in` DISABLE KEYS */;
INSERT INTO `interaction_check_in` VALUES (1,3,8,'今天完成了数据结构前三章的课后题，错了不少，明天继续复盘错题。','2026-04-16','2026-04-17 20:44:08'),(2,4,10,'高数刷了50题，英语背了100个单词，充实的一天！继续保持！','2026-04-16','2026-04-17 20:44:08'),(3,5,6,'今天有点感冒，状态不好，只背了2个单元的政治，明天要补回来。','2026-04-16','2026-04-17 20:44:08'),(4,3,9,'计组真题第一套完成，大题还要加强，感觉指令系统那块还是不熟。','2026-04-17','2026-04-17 20:44:08'),(5,6,24,'状态极佳，暴力学习24小时','2026-04-18','2026-04-18 11:42:38'),(6,8,8,'今天状态不错','2026-05-21','2026-05-21 15:07:29'),(7,8,8,'舒服','2026-06-04','2026-06-04 14:35:06'),(8,8,8,'伤心、难过','2026-06-05','2026-06-05 20:27:11');
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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='积分变动日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interaction_points_log`
--

LOCK TABLES `interaction_points_log` WRITE;
/*!40000 ALTER TABLE `interaction_points_log` DISABLE KEYS */;
INSERT INTO `interaction_points_log` VALUES (1,6,3,'CHECK_IN',NULL,'打卡+3分','2026-04-18 11:42:38'),(2,8,3,'CHECK_IN',NULL,'打卡+3分','2026-05-21 15:07:29'),(3,8,3,'CHECK_IN',NULL,'打卡+3分','2026-06-04 14:35:06'),(4,8,3,'CHECK_IN',NULL,'打卡+3分','2026-06-05 20:27:11');
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
INSERT INTO `interaction_user_study` VALUES (1,6,1,1,'2026-04-18','2026-04-18 11:42:38'),(2,8,2,3,'2026-06-05','2026-05-21 15:07:29');
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
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模拟面试对话明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_record`
--

LOCK TABLES `interview_record` WRITE;
/*!40000 ALTER TABLE `interview_record` DISABLE KEYS */;
INSERT INTO `interview_record` VALUES (1,1,'user','（面试开始）',NULL,'2026-06-05 20:33:57'),(2,1,'ai','同学你好，请坐。  \n嗯……先别紧张，咱们就当是聊聊天。  \n请你用两分钟左右，简单介绍一下自己，重点说说你为什么想报考我们这个专业，以及你觉得自己有哪些准备或者优势？',NULL,'2026-06-05 20:34:00'),(3,2,'user','（面试开始）',NULL,'2026-06-05 20:34:52'),(4,2,'ai','同学你好，请坐。  \n嗯……先别紧张，咱们就当是聊聊天。  \n请你用两分钟左右，简单介绍一下自己，重点说说你为什么想报考我们这个专业，以及你觉得自己有哪些准备或者优势？',NULL,'2026-06-05 20:34:57'),(5,3,'user','（面试开始）',NULL,'2026-06-05 21:21:21'),(6,3,'ai','同学你好，请坐。  \n嗯……先别紧张，咱们就当是聊聊天。  \n请你用两分钟左右，简单介绍一下自己，重点说说你为什么想考我们这个专业，以及你觉得自己有哪些准备或者优势？',NULL,'2026-06-05 21:21:24'),(7,4,'user','（面试开始）',NULL,'2026-06-05 21:21:44'),(8,4,'ai','同学你好，请坐。  \n嗯……先别紧张，咱们就当是聊聊天。  \n请你用两分钟左右，简单介绍一下自己，重点说说你为什么想报考我们这个专业，以及你觉得自己有哪些准备或者优势？',NULL,'2026-06-05 21:21:46'),(9,5,'user','（面试开始）',NULL,'2026-06-05 21:21:59'),(10,5,'ai','同学你好，请坐。  \n嗯……先别紧张，咱们就当是聊聊天。  \n请你用两分钟左右，简单介绍一下自己，重点说说你为什么想报考我们这个专业，以及你觉得自己有哪些准备或者优势？',NULL,'2026-06-05 21:22:01'),(11,6,'user','（面试开始）',NULL,'2026-06-05 21:22:42'),(12,6,'ai','同学你好，先放松一下，不用太紧张。请用两分钟左右的时间，简单介绍一下自己，重点说说你为什么报考我们这个专业，以及你觉得自己有哪些优势比较适合这个方向。',NULL,'2026-06-05 21:22:44'),(13,7,'user','（面试开始）',NULL,'2026-06-05 21:22:55'),(14,7,'ai','同学你好，请坐。  \n嗯……先别紧张，咱们就当是聊聊天。  \n请你用两分钟左右，简单介绍一下自己，重点说说你为什么想考我们这个专业，以及你觉得自己有哪些准备或者优势？',NULL,'2026-06-05 21:22:57'),(15,7,'user','考古你好',NULL,'2026-06-05 21:23:05'),(16,7,'ai','嗯……同学，你刚才说“考古你好”，是不是口误了？  \n我们今天面试的是教育学专业，不是考古学。  \n请你重新开始自我介绍，重点说说为什么选择教育学，以及你在这方面的思考或准备。',NULL,'2026-06-05 21:23:07'),(17,7,'user','请你介绍一下你自己',NULL,'2026-06-05 21:23:30'),(18,7,'ai','同学，这是你的复试面试，不是我的。  \n请你介绍你自己——这是第一个问题。  \n来，从你为什么选择教育学开始说吧。',NULL,'2026-06-05 21:23:32'),(19,7,'user','被逼无奈',NULL,'2026-06-05 21:23:40'),(20,7,'ai','嗯……“被逼无奈”这个说法，我听到过不少次。  \n但我想知道的是，在这个“无奈”背后，有没有哪一刻，是你自己真正觉得——教育这件事，值得去琢磨、去投入、甚至去坚持的？  \n比如一次教学观察，一段支教经历，或者哪怕是一本书、一堂课带给你的触动？  \n说说看。',NULL,'2026-06-05 21:23:43'),(21,8,'user','（面试开始）',NULL,'2026-06-05 21:24:27'),(22,8,'ai','同学你好，请坐。  \n嗯……先别紧张，咱们就当是聊聊天。  \n请你用两分钟左右，简单介绍一下自己，重点说说你为什么想考我们这个专业，以及你觉得自己有哪些准备或者优势？',NULL,'2026-06-05 21:24:29'),(23,9,'user','（面试开始）',NULL,'2026-06-05 21:24:51'),(24,9,'ai','同学你好，请坐。  \n嗯……先轻松一点，不用紧张，就简单做个自我介绍吧，时间两分钟左右，重点说说你的学术背景、专业兴趣，还有为什么想报考我们这个方向。',NULL,'2026-06-05 21:24:53'),(25,9,'user','面试官老师好，我是 WDB，本科就读计算机相关专业，在校主修数据结构、计算机组成原理、操作系统、计算机网络、编译原理等核心专业课，专业基础课程综合排名稳居年级前列，夯实了计算机底层软硬件、系统理论的基础知识。 在课业学习之外，我侧重两条实践积累：一是课程项目落地，依托 C/C++ 完成简易操作系统内核模块开发、基于哈希表与红黑树实现高性能缓存组件，通过项目吃透内存管理、进程调度等底层原理；二是课余自主钻研，持续跟进高性能计算、人工智能系统方向前沿论文，利用课余时间完成 Python 深度学习工程、轻量化模型部署实验，熟悉模型从训练到落地全链路流程。 我的专业兴趣聚焦在计算机系统与智能计算交叉领域，一方面痴迷计算机底层架构、编译优化、分布式系统的原理探究，喜欢拆解程序运行底层逻辑；另一方面关注大模型底层算力调度、异构硬件加速等前沿课题，这也是清华计算机优势研究方向。 选择报考清华大学计算机科学与技术主要有三点原因：第一，清华计算机学科师资雄厚，在系统架构、人工智能、高性能计算等方向科研实力国内顶尖，完善的实验室平台与前沿课题资源，能支撑我从理论走向深度科研；第二，本科学习中发现现有知识局限于工程应用，想要在顶尖平台深耕学术、解决底层关键技术难题；第三，我长期关注院系课题组发布的科研成果，诸多成果和我的研究兴趣高度契合，希望能在导师指导下，聚焦系统优化与 AI 异构加速方向开展科研，弥补国内相关领域技术短板。 以上是我的自我介绍，感谢各位老师。',NULL,'2026-06-05 21:26:20'),(26,9,'ai','同学，你刚才提到“痴迷计算机底层架构”“喜欢拆解程序运行底层逻辑”，那我问一个具体点的问题——  \n当你用 C 语言写一个函数，调用时发生栈帧切换，这个过程中，CPU、寄存器、内存各参与了哪些关键动作？不需要背书，就按你理解的顺序，说说最关键的三到四个环节。',NULL,'2026-06-05 21:26:22'),(27,9,'user','老师，我按照 CPU 执行时序梳理四个关键环节： 调用方保存返回地址，执行 call 指令 CPU 执行call func时，先把下一条指令的地址（EIP/RIP 值，函数返回地址）压入栈内存；同时 CPU 修改指令指针寄存器 RIP，跳转到目标函数入口地址。此时用到通用指令指针寄存器、栈寄存器 RSP，栈区在进程用户栈内存。 被调函数建立栈基址，搭建栈帧框架（prologue 序言代码） 被调函数开头push rbp，将调用函数的栈基址 RBP 入栈保存；紧接着mov rbp, rsp，把当前栈指针赋值给基址寄存器 RBP，RBP 成为本栈帧锚点，后续局部变量、函数参数都依靠 RBP 做偏移寻址。RBP、RSP 寄存器改动，栈内存持续向下拓展。 调整栈指针，分配局部变量栈空间 CPU 通过sub rsp, N指令，向下偏移 RSP，在栈内存中划出连续空间，用来存放函数内部局部变量、临时缓冲区。这段内存属于当前函数私有栈空间，寄存器仅修改 RSP，不改动 RBP，依靠 RBP 固定栈帧边界。 若有需要栈对齐、临时变量，还会额外空出对齐字节。 函数收尾恢复现场，栈帧销毁（epilogue 收尾），准备返回 函数 return 前：mov rsp, rbp把栈指针还原到本栈帧基址，释放局部变量占用的栈内存；pop rbp从栈中弹出之前保存的上层函数基址，恢复调用方 RBP；最后执行ret，从栈内存弹出之前存放的返回地址送入 RIP，CPU 跳转回调用函数断点，完成栈帧切回。',NULL,'2026-06-05 21:27:11'),(28,9,'ai','嗯……这个回答思路是清楚的，也抓住了几个关键寄存器和动作。  \n不过我注意到你全程说的是 x86-64 的典型过程，那我想追问一句：如果现在换成 RISC-V 架构，比如 RV64GC，它没有 call 指令，也没有隐式压栈的 ret 指令，那“函数调用”这件事，硬件和编译器分别是怎么配合完成的？你大概说说这个机制上的差异就行。',NULL,'2026-06-05 21:27:14'),(29,9,'user','老师，RISC-V RV64GC 没有 x86 那种硬件自动压栈的call/ret，依靠编译期约定 + 两条跳转指令 + 软件栈规范协作实现函数调用，核心差异在三点： 1. 用 jal/jalr 替代硬件 call，链接寄存器存返回地址 RISC-V 架构规定ra(x1)作为专用返回地址链接寄存器： 短距离函数调用：jal ra, func，硬件自动把当前 PC+4 写入 ra 寄存器，同时 PC 跳转到目标函数，全程CPU 不自动访问内存、不压栈，返回地址先存在寄存器而非栈； 跨大地址远距离调用：编译器先用auipc+jalr组合，拼装目标地址，依旧把返回值存进ra。 x86 是 call 硬件自动把返回地址压入栈内存，RISC-V 把返回地址优先放通用寄存器，是软硬件最大区别。 2. 寄存器不够用时，编译器主动生成指令手动把 ra 压栈 RISC-V 没有硬件隐式入栈，栈操作全靠编译器生成 store/load 访存指令手动完成： 如果被调用函数内部还要嵌套调用别的子函数，ra的值会被新的 jal 覆盖，编译器就在函数序言生成sd ra, -8(sp)，手动把 ra 存入栈；函数结尾用ld ra, -8(sp)从栈取回 ra。 栈指针sp(x2)的增减也是编译器通过addi sp,sp,-N/addi sp,sp,+N手动开辟、释放栈帧，不像 x86 由 call/ret 硬件隐式修改栈。 3. ret 等效靠 jalr x0, ra, 0 实现返回 RISC-V 无 ret 硬件指令，函数返回统一使用jalr x0, ra, 0：硬件把 ra 的值载入 PC 完成跳转，x0 寄存器丢弃返回链接值，实现返回调用点。全程依旧没有任何硬件自动出栈动作。',NULL,'2026-06-05 21:27:48'),(30,9,'ai','很好，这个对比讲得挺扎实，说明你确实下过功夫去抠指令集差异。  \n那我再往下推一步——既然 RISC-V 把“压栈”这件事全交给编译器来管，那如果一个函数里既没调用其他函数，又没用到 ra 寄存器（比如是个纯计算的叶子函数），编译器还会为它生成栈帧吗？为什么？',NULL,'2026-06-05 21:27:51'),(31,9,'user','老师，这类无嵌套调用的叶子函数，绝大多数场景编译器不会分配栈帧、不操作 sp，也不会保存 ra，原因从 RISC-V ABI 和寄存器使用逻辑三点说明： ra 全程不会被覆写，无需落地保存 叶子函数内部没有jal/jalr子函数调用，ra寄存器存放的上层返回地址自始至终保持原值，不会被新的函数调用指令覆盖，自然不需要sd ra,偏移(sp)把 ra 存到栈里。 局部变量可以全部放在通用寄存器，不用栈内存 RV64 有 t0~t6 临时寄存器、a0~a7 参数寄存器，纯运算的局部中间变量、运算数据编译器优先分配在通用寄存器中。只要寄存器资源够用，完全不需要通过addi sp,sp,-X向下拓展栈空间。 没有栈就没有栈帧开销，符合 RISC 精简思想 栈帧本质是用来寄存溢出寄存器、保存返回地址的备用内存空间，既然两项需求都不存在，编译器遵循 RISC-V 调用约定直接省略栈帧开辟与回收的序言、尾声指令。 补充边界特例 只有一种例外：函数局部变量过多、通用寄存器耗尽，编译器触发寄存器溢出（spill），才会临时在栈上开辟极小空间存放溢出变量，此时才会构建简易栈帧；无溢出则彻底无栈。 简单总结：无调用、不糟蹋 ra、寄存器够用→零栈帧。',NULL,'2026-06-05 21:28:15'),(32,9,'ai','嗯……这个回答我听着是到位的。  \n你把 ABI 约定、寄存器分配策略和编译器行为逻辑串起来了，不是光背概念。  \n\n那最后一个问题——你说“寄存器够用就不用栈”，但现实中很多函数哪怕只是做点矩阵乘法或者图像卷积，很快就会寄存器不够。这时候编译器得 spill，可一旦 spill 到栈上，访问延迟就上去了。  \n那在 AI 推理这种对 latency 极其敏感的场景里，你觉得系统层面或者编译器层面，还有哪些可能的优化方向，能缓解这种“寄存器-内存”之间的性能鸿沟？简单说一两点你的想法就行。',NULL,'2026-06-05 21:28:20'),(33,9,'user','老师，我从编译器优化和体系架构软硬件协同两个方向各说一条落地性较强的优化思路，贴合 AI 卷积、矩阵乘这类密集计算场景： 1. 编译器侧：基于分块优化（Loop Tiling）配合寄存器分组调度，减少寄存器溢出 矩阵乘、卷积访存具备极强局部性，编译器不再按原始大循环遍历数据，把超大矩阵切分成适配寄存器堆容量的小块（Tile）。让分块后的全部中间计算数据可以完整寄存到通用寄存器、向量寄存器中，从根源减少 spill 落栈。 同时结合 RISC-V 的 RVV 向量扩展，批量数据打包存入向量寄存器组，单条指令处理一组数据，大幅压低临时变量数量，规避频繁栈读写。现在 RISC-V 开源编译器 GCC、LLVM 针对 AI 算子都在做自动分块的优化实现。 2. 系统架构侧：引入软件可控的片上高速缓冲区（Scratchpad Memory，便签存储器），替代通用栈做溢出缓存 区别于 CPU 硬件自动管理的 Cache，片上 SPM 由编译器显式分配地址，属于软件可控的高速片上存储，访问延迟远低于主存、甚至优于栈内存。 AI 推理算子发生寄存器溢出时，编译器不再把溢出数据压入进程栈（DDR 内存区域），而是调度存入 SPM。数据取用直接访问片上存储，绕开低速内存栈，既解决寄存器不够用的问题，又规避栈访存带来的 latency 上涨，很多 RISC-V 架构 AI NPU、边缘推理芯片都采用这套方案。',NULL,'2026-06-05 21:28:51'),(34,9,'ai','嗯……这个思路有现实基础，也看出你对编译优化和硬件协同不是泛泛而谈。  \n\n面试就到这里。  \n感谢你的认真回答，整体逻辑清晰，能抓住关键点展开，也有自己的思考。  \n结果请关注学校后续统一通知。',NULL,'2026-06-05 21:28:53');
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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模拟面试评估报告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_report`
--

LOCK TABLES `interview_report` WRITE;
/*!40000 ALTER TABLE `interview_report` DISABLE KEYS */;
INSERT INTO `interview_report` VALUES (1,7,18.0,'[{\"score\": 5, \"dimension\": \"语言表达\"}, {\"score\": 0, \"dimension\": \"专业知识\"}, {\"score\": 0, \"dimension\": \"逻辑思维\"}, {\"score\": 0, \"dimension\": \"应变能力\"}, {\"score\": 30, \"dimension\": \"心理素质\"}]','考生未展现出任何可识别的语言表达优势、专业积累或思维特征；全程未提供有效信息，无自我介绍、无动机阐述、无经历陈述、无观点输出，未响应考官三次明确指令，亦未对专业名称误读（考古/教育学）作出修正或致歉。','考生严重缺乏复试基本认知与规则意识：连续四次回避核心问题（误称专业、反向要求考官自述、用‘被逼无奈’消极回应、全程零实质内容），未展现任何教育学相关知识储备、学习动机或反思能力；其行为已构成对面试流程的实质性拒绝，不属于紧张或表达障碍，而是备考准备严重缺失与态度缺位。','1. 立即停止模拟面试，先系统学习研究生复试基本规范与流程；2. 完成至少5次结构化自我介绍训练（含动机、经历、专业理解三要素）；3. 阅读《教育学基础》（全国十二所重点师范大学联合编写）前两章并手写摘要；4. 寻求导师或学长一对一复盘，直面态度问题而非仅练话术；5. 若无真实报考意愿与准备，建议慎重评估升学决策。','该生在本次模拟面试中未完成任何规定作答任务，四次回应均偏离问题本质且无信息增量，暴露出严重的备考缺位、专业认知空白及规则意识缺失。其表现已超出‘紧张’或‘表达不畅’范畴，属于主动放弃交流资格的行为。按复试纪律标准，该表现不具备进入后续考核的基本资格。','{\n  \"totalScore\": 18.0,\n  \"radarChart\": [\n    { \"dimension\": \"语言表达\", \"score\": 5 },\n    { \"dimension\": \"专业知识\", \"score\": 0 },\n    { \"dimension\": \"逻辑思维\", \"score\": 0 },\n    { \"dimension\": \"应变能力\", \"score\": 0 },\n    { \"dimension\": \"心理素质\", \"score\": 30 }\n  ],\n  \"strengthAnalysis\": \"考生未展现出任何可识别的语言表达优势、专业积累或思维特征；全程未提供有效信息，无自我介绍、无动机阐述、无经历陈述、无观点输出，未响应考官三次明确指令，亦未对专业名称误读（考古/教育学）作出修正或致歉。\",\n  \"weaknessAnalysis\": \"考生严重缺乏复试基本认知与规则意识：连续四次回避核心问题（误称专业、反向要求考官自述、用‘被逼无奈’消极回应、全程零实质内容），未展现任何教育学相关知识储备、学习动机或反思能力；其行为已构成对面试流程的实质性拒绝，不属于紧张或表达障碍，而是备考准备严重缺失与态度缺位。\",\n  \"suggestion\": \"1. 立即停止模拟面试，先系统学习研究生复试基本规范与流程；2. 完成至少5次结构化自我介绍训练（含动机、经历、专业理解三要素）；3. 阅读《教育学基础》（全国十二所重点师范大学联合编写）前两章并手写摘要；4. 寻求导师或学长一对一复盘，直面态度问题而非仅练话术；5. 若无真实报考意愿与准备，建议慎重评估升学决策。\",\n  \"summary\": \"该生在本次模拟面试中未完成任何规定作答任务，四次回应均偏离问题本质且无信息增量，暴露出严重的备考缺位、专业认知空白及规则意识缺失。其表现已超出‘紧张’或‘表达不畅’范畴，属于主动放弃交流资格的行为。按复试纪律标准，该表现不具备进入后续考核的基本资格。\"\n}','2026-06-05 21:23:57'),(2,9,92.0,'[{\"score\": 88, \"dimension\": \"语言表达\"}, {\"score\": 96, \"dimension\": \"专业知识\"}, {\"score\": 94, \"dimension\": \"逻辑思维\"}, {\"score\": 92, \"dimension\": \"应变能力\"}, {\"score\": 90, \"dimension\": \"心理素质\"}]','考生展现出极为扎实的计算机系统级知识功底，对x86-64与RISC-V函数调用机制的理解远超本科常规教学深度，能精准区分硬件行为、ABI约定与编译器职责；回答全程逻辑严密、层次清晰，从指令执行时序到寄存器语义、再到软硬件协同优化，形成完整技术链条；语言表达准确凝练，中英文术语使用规范（如jalr、sd/ld、RVV、SPM等），无冗余赘述，体现出优秀的学术表达素养和真实科研潜质。','虽整体表达优秀，但在首次自我介绍中主动透露姓名缩写\'WDB\'，构成一次个人信息泄露；虽未被考官当场提醒，但按规则需在语言表达维度扣20分（已体现为88分而非潜在95+），且总评上限下调10分（故92.0为合规最高分）。此外，在AI推理优化环节，对SPM内存一致性模型与编译器显式管理开销的权衡未展开，略失一点系统纵深视角。','1. 严格规避任何形式的个人信息暴露（包括姓名缩写、本科校名、项目具体时间等），所有模拟面试均以\'本科学习期间\'等模糊表述替代；2. 针对RISC-V生态，补充阅读RISC-V Privileged Architecture与RISC-V Vector Extension最新草案，强化对异常处理与向量寄存器上下文保存机制的理解；3. 结合LLVM MLIR工具链，动手实践一个简单算子的自动tiling优化Pass，将理论认知转化为工程能力；4. 关注清华NLP/体系结构组近期发表的AI编译器工作（如TVM-RISC-V适配、HeteroCL优化框架），建立与目标导师研究方向的具体连接点；5. 在英文问答环节预设3个高频专业问题（如\'Explain calling convention in RISC-V in English\'），进行限时口述训练，提升术语反应速度。','考生是极具竞争力的系统方向苗子：专业知识维度接近满分，对指令集架构、ABI、编译器行为、硬件加速的交叉理解深刻且自洽；逻辑思维与应变能力突出，面对层层递进的技术追问始终能锚定核心矛盾，举证具体、推演严谨；心理素质沉稳自信，全程保持技术对话节奏，展现出成熟的研究者气质。虽有轻微信息规范疏漏，但综合素养完全匹配清华大学计算机系顶尖生源标准。','{\n  \"totalScore\": 92.0,\n  \"radarChart\": [\n    { \"dimension\": \"语言表达\", \"score\": 88 },\n    { \"dimension\": \"专业知识\", \"score\": 96 },\n    { \"dimension\": \"逻辑思维\", \"score\": 94 },\n    { \"dimension\": \"应变能力\", \"score\": 92 },\n    { \"dimension\": \"心理素质\", \"score\": 90 }\n  ],\n  \"strengthAnalysis\": \"考生展现出极为扎实的计算机系统级知识功底，对x86-64与RISC-V函数调用机制的理解远超本科常规教学深度，能精准区分硬件行为、ABI约定与编译器职责；回答全程逻辑严密、层次清晰，从指令执行时序到寄存器语义、再到软硬件协同优化，形成完整技术链条；语言表达准确凝练，中英文术语使用规范（如jalr、sd/ld、RVV、SPM等），无冗余赘述，体现出优秀的学术表达素养和真实科研潜质。\",\n  \"weaknessAnalysis\": \"虽整体表达优秀，但在首次自我介绍中主动透露姓名缩写\'WDB\'，构成一次个人信息泄露；虽未被考官当场提醒，但按规则需在语言表达维度扣20分（已体现为88分而非潜在95+），且总评上限下调10分（故92.0为合规最高分）。此外，在AI推理优化环节，对SPM内存一致性模型与编译器显式管理开销的权衡未展开，略失一点系统纵深视角。\",\n  \"suggestion\": \"1. 严格规避任何形式的个人信息暴露（包括姓名缩写、本科校名、项目具体时间等），所有模拟面试均以\'本科学习期间\'等模糊表述替代；2. 针对RISC-V生态，补充阅读RISC-V Privileged Architecture与RISC-V Vector Extension最新草案，强化对异常处理与向量寄存器上下文保存机制的理解；3. 结合LLVM MLIR工具链，动手实践一个简单算子的自动tiling优化Pass，将理论认知转化为工程能力；4. 关注清华NLP/体系结构组近期发表的AI编译器工作（如TVM-RISC-V适配、HeteroCL优化框架），建立与目标导师研究方向的具体连接点；5. 在英文问答环节预设3个高频专业问题（如\'Explain calling convention in RISC-V in English\'），进行限时口述训练，提升术语反应速度。\",\n  \"summary\": \"考生是极具竞争力的系统方向苗子：专业知识维度接近满分，对指令集架构、ABI、编译器行为、硬件加速的交叉理解深刻且自洽；逻辑思维与应变能力突出，面对层层递进的技术追问始终能锚定核心矛盾，举证具体、推演严谨；心理素质沉稳自信，全程保持技术对话节奏，展现出成熟的研究者气质。虽有轻微信息规范疏漏，但综合素养完全匹配清华大学计算机系顶尖生源标准。\"\n}','2026-06-05 21:29:17');
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI模拟面试会话表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `interview_session`
--

LOCK TABLES `interview_session` WRITE;
/*!40000 ALTER TABLE `interview_session` DISABLE KEYS */;
INSERT INTO `interview_session` VALUES (1,8,'清华大学','计算机科学与技术','COMPREHENSIVE','IN_PROGRESS',NULL,'2026-06-05 20:33:57','2026-06-05 20:33:57'),(2,8,'清华大学','计算机科学与技术','COMPREHENSIVE','IN_PROGRESS',NULL,'2026-06-05 20:34:51','2026-06-05 20:34:51'),(3,8,'','','COMPREHENSIVE','IN_PROGRESS',NULL,'2026-06-05 21:21:21','2026-06-05 21:21:21'),(4,8,'','','COMPREHENSIVE','IN_PROGRESS',NULL,'2026-06-05 21:21:44','2026-06-05 21:21:44'),(5,8,'','','COMPREHENSIVE','IN_PROGRESS',NULL,'2026-06-05 21:21:59','2026-06-05 21:21:59'),(6,8,'','','COMPREHENSIVE','IN_PROGRESS',NULL,'2026-06-05 21:22:42','2026-06-05 21:22:42'),(7,8,'','','COMPREHENSIVE','REPORTED',18.0,'2026-06-05 21:22:55','2026-06-05 21:22:55'),(8,8,'','','COMPREHENSIVE','IN_PROGRESS',NULL,'2026-06-05 21:24:27','2026-06-05 21:24:27'),(9,8,'清华大学','计算机科学与技术','COMPREHENSIVE','REPORTED',92.0,'2026-06-05 21:24:51','2026-06-05 21:24:51');
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
  `chat_message_id` bigint DEFAULT NULL COMMENT '来源AI消息ID',
  `source_type` varchar(20) DEFAULT 'MANUAL' COMMENT '来源类型',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_next_review` (`user_id`,`next_review_date`),
  KEY `idx_subject` (`user_id`,`subject`),
  KEY `idx_mastery` (`user_id`,`mastery_level`),
  KEY `idx_chat_msg` (`chat_message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='错题本';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mistake_note`
--

LOCK TABLES `mistake_note` WRITE;
/*!40000 ALTER TABLE `mistake_note` DISABLE KEYS */;
INSERT INTO `mistake_note` VALUES (1,8,'MATH','怎么做','**解题思路与步骤：** 这是一道典型的二重积分计算题，关键在于**观察被积函数结构**并**利用积分区域对称性**简化计算。以下是详细解答过程。 **1. 分析积分区域** 区域 \\(D\\) 由 \\(y=|x|\\) 与 \\(y=1\\) 围成。在直角坐标系中，该区域是一个以 \\((0,0), (1,1), (-1,1)\\) 为顶点的等腰直角三角形，且关于 \\(y\\) 轴对称。 **2. 分析并简化被积函数** 被积函数为： \\[ f(x,y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2} = \\frac{x^2 - y^2}{x^2 + y^2} - \\frac{x\\cos y}{x^2 + y^2} \\] 注意到： * 函数 \\(g(x,y) = \\frac{x^2 - y^2}{x^2 + y^2}\\) 关于变量 \\(x\\) 是**偶函数**（即 \\(g(-x,y) = g(x,y)\\)）。 * 函数 \\(h(x,y) = \\frac{x\\cos y}{x^2 + y^2}\\) 关于变量 \\(x\\) 是**奇函数**（即 \\(h(-x,y) = -h(x,y)\\)）。 由于积分区域 \\(D\\) 关于 \\(y\\) 轴（即 \\(x=0\\)）对称，根据二重积分的对称性性质： * 奇函数在关于 \\(y\\) 轴对称的区域上的积分为 **0**。 * 偶函数在关于 \\(y\\) 轴对称的区域上的积分等于其在右半区域积分的 **2** 倍。 因此： \\[ \\iint_D f(x,y) \\, dxdy = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} \\, dxdy - \\iint_D \\frac{x\\cos y}{x^2 + y^2} \\, dxdy \\] 第二项（含奇函数）的积分为 **0**。原积分简化为： \\[ I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} \\, dxdy \\] **3. 计算简化后的二重积分** 现在计算 \\(I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} \\, dxdy\\)。我们选择先对 \\(x\\) 积分，再对 \\(y\\) 积分。 区域 \\(D\\) 可表示为：\\(0 \\le y \\le 1\\)，\\(-y \\le x \\le y\\)。 所以： \\[ I = \\int_{0}^{1} dy \\int_{-y}^{y} \\frac{x^2 - y^2}{x^2 + y^2} \\, dx \\] **计算内层积分（关于 \\(x\\)）：** \\[ \\int_{-y}^{y} \\frac{x^2 - y^2}{x^2 + y^2} \\, dx = \\int_{-y}^{y} \\left(1 - \\frac{2y^2}{x^2 + y^2}\\right) \\, dx \\] \\[ = \\int_{-y}^{y} 1 \\, dx - 2y^2 \\int_{-y}^{y} \\frac{1}{x^2 + y^2} \\, dx \\] 第一项：\\(\\int_{-y}^{y} 1 \\, dx = 2y\\) 第二项：\\(\\int_{-y}^{y} \\frac{1}{x^2 + y^2} \\, dx = \\left[ \\frac{1}{y} \\arctan\\left(\\frac{x}{y}\\right) \\right]_{-y}^{y} = \\frac{1}{y} \\left( \\arctan(1) - \\arctan(-1) \\right) = \\frac{1}{y} \\left( \\frac{\\pi}{4} - (-\\frac{\\pi}{4}) \\right) = \\frac{\\pi}{2y}\\) 所以内层积分结果为：\\(2y - 2y^2 \\cdot \\frac{\\pi}{2y} = 2y - \\pi y\\) **计算外层积分（关于 \\(y\\)）：** \\[ I = \\int_{0}^{1} (2y - \\pi y) \\, dy = (2 - \\pi) \\int_{0}^{1} y \\, dy = (2 - \\pi) \\cdot \\left[ \\frac{y^2}{2} \\right]_{0}^{1} = (2 - \\pi) \\cdot \\frac{1}{2} = \\frac{2 - \\pi}{2} \\] **4. 最终结果** \\[ \\iint_D \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2} \\, dxdy = \\frac{2 - \\pi}{2} \\] **解题要点总结：** 1. **对称性应用**：本题首要步骤是观察被积函数中各部分的奇偶性与积分区域的对称性，这能极大简化计算。 2. **区域描述**：将 \\(y=|x|\\) 描述的角域用不等式 \\(-y \\le x \\le y\\) 精确表示。 3. **积分计算**：内层积分计算时，将分式拆解为常数项与基本积分公式形式（\\(\\int \\frac{dx}{x^2+a^2}\\)）的组合。 **📚 考点出处：** 1. **二重积分的计算**（高等数学 - 多元函数积分学） 2. **利用区域对称性与被积函数奇偶性简化二重积分**（重要考点技巧） 3. **直角坐标系下交换积分次序与累次积分计算**','/uploads/images/202606/f56b373578164a16a0b27d85e4a35ca6.png','100','OCR',3,0,0,0,'2026-06-07',NULL,0,'2026-06-06 14:28:20','2026-06-06 14:28:20',NULL,'MANUAL'),(2,8,'高等数学','怎么做','**解题思路与步骤：** 这是一道典型的二重积分计算题，关键在于**观察被积函数结构**并**利用积分区域对称性**简化计算。以下是详细解答过程。 **1. 分析积分区域** 区域 \\(D\\) 由 \\(y=|x|\\) 与 \\(y=1\\) 围成。在直角坐标系中，该区域是一个以 \\((0,0), (1,1), (-1,1)\\) 为顶点的等腰直角三角形，且关于 \\(y\\) 轴对称。 **2. 分析并简化被积函数** 被积函数为： \\[ f(x,y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2} = \\frac{x^2 - y^2}{x^2 + y^2} - \\frac{x\\cos y}{x^2 + y^2} \\] 注意到： * 函数 \\(g(x,y) = \\frac{x^2 - y^2}{x^2 + y^2}\\) 关于变量 \\(x\\) 是**偶函数**（即 \\(g(-x,y) = g(x,y)\\)）。 * 函数 \\(h(x,y) = \\frac{x\\cos y}{x^2 + y^2}\\) 关于变量 \\(x\\) 是**奇函数**（即 \\(h(-x,y) = -h(x,y)\\)）。 由于积分区域 \\(D\\) 关于 \\(y\\) 轴（即 \\(x=0\\)）对称，根据二重积分的对称性性质： * 奇函数在关于 \\(y\\) 轴对称的区域上的积分为 **0**。 * 偶函数在关于 \\(y\\) 轴对称的区域上的积分等于其在右半区域积分的 **2** 倍。 因此： \\[ \\iint_D f(x,y) \\, dxdy = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} \\, dxdy - \\iint_D \\frac{x\\cos y}{x^2 + y^2} \\, dxdy \\] 第二项（含奇函数）的积分为 **0**。原积分简化为： \\[ I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} \\, dxdy \\] **3. 计算简化后的二重积分** 现在计算 \\(I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} \\, dxdy\\)。我们选择先对 \\(x\\) 积分，再对 \\(y\\) 积分。 区域 \\(D\\) 可表示为：\\(0 \\le y \\le 1\\)，\\(-y \\le x \\le y\\)。 所以： \\[ I = \\int_{0}^{1} dy \\int_{-y}^{y} \\frac{x^2 - y^2}{x^2 + y^2} \\, dx \\] **计算内层积分（关于 \\(x\\)）：** \\[ \\int_{-y}^{y} \\frac{x^2 - y^2}{x^2 + y^2} \\, dx = \\int_{-y}^{y} \\left(1 - \\frac{2y^2}{x^2 + y^2}\\right) \\, dx \\] \\[ = \\int_{-y}^{y} 1 \\, dx - 2y^2 \\int_{-y}^{y} \\frac{1}{x^2 + y^2} \\, dx \\] 第一项：\\(\\int_{-y}^{y} 1 \\, dx = 2y\\) 第二项：\\(\\int_{-y}^{y} \\frac{1}{x^2 + y^2} \\, dx = \\left[ \\frac{1}{y} \\arctan\\left(\\frac{x}{y}\\right) \\right]_{-y}^{y} = \\frac{1}{y} \\left( \\arctan(1) - \\arctan(-1) \\right) = \\frac{1}{y} \\left( \\frac{\\pi}{4} - (-\\frac{\\pi}{4}) \\right) = \\frac{\\pi}{2y}\\) 所以内层积分结果为：\\(2y - 2y^2 \\cdot \\frac{\\pi}{2y} = 2y - \\pi y\\) **计算外层积分（关于 \\(y\\)）：** \\[ I = \\int_{0}^{1} (2y - \\pi y) \\, dy = (2 - \\pi) \\int_{0}^{1} y \\, dy = (2 - \\pi) \\cdot \\left[ \\frac{y^2}{2} \\right]_{0}^{1} = (2 - \\pi) \\cdot \\frac{1}{2} = \\frac{2 - \\pi}{2} \\] **4. 最终结果** \\[ \\iint_D \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2} \\, dxdy = \\frac{2 - \\pi}{2} \\] **解题要点总结：** 1. **对称性应用**：本题首要步骤是观察被积函数中各部分的奇偶性与积分区域的对称性，这能极大简化计算。 2. **区域描述**：将 \\(y=|x|\\) 描述的角域用不等式 \\(-y \\le x \\le y\\) 精确表示。 3. **积分计算**：内层积分计算时，将分式拆解为常数项与基本积分公式形式（\\(\\int \\frac{dx}{x^2+a^2}\\)）的组合。 **📚 考点出处：** 1. **二重积分的计算**（高等数学 - 多元函数积分学） 2. **利用区域对称性与被积函数奇偶性简化二重积分**（重要考点技巧） 3. **直角坐标系下交换积分次序与累次积分计算**','/uploads/images/202606/f56b373578164a16a0b27d85e4a35ca6.png',NULL,'AI答疑',3,0,0,0,'2026-06-07',NULL,0,'2026-06-06 15:02:07','2026-06-06 15:02:07',18,'AI_CHAT'),(3,8,'计算机网络','进程和线程的区别是什么？','## 进程和线程的区别 进程和线程是操作系统中两个核心的并发执行概念，理解它们的区别是掌握操作系统进程管理、调度及并发编程的基础。以下是它们的主要区别： | **区别维度** | **进程（Process）** | **线程（Thread）** | | :--- | :--- | :--- | | **1. 定义与资源拥有** | 程序的一次动态执行实例，是系统进行**资源分配**（如内存、文件句柄、I/O设备）和**独立调度**的基本单位。 | 进程内的一个执行单元，是**CPU调度**的基本单位。同一进程内的线程共享该进程的全部资源。 | | **2. 独立性** | 不同进程之间相互独立，一个进程的崩溃通常不影响其他进程。 | 同一进程内的线程共享地址空间和资源，一个线程的崩溃可能导致整个进程崩溃。 | | **3. 内存空间** | 每个进程拥有独立的虚拟地址空间，进程间内存隔离。 | 同一进程的所有线程共享该进程的虚拟地址空间、全局变量、打开的文件等。 | | **4. 系统开销** | 创建、切换和销毁的开销大，需要分配和回收资源，涉及内核态和用户态的频繁切换。 | 创建、切换的开销相对小得多，因为线程共享进程资源，切换时只需保存和恢复少量上下文（如寄存器、程序计数器）。 | | **5. 并发性** | 进程间可以并发执行，但同一进程内的线程也能并发执行。多进程程序比多线程程序在内存和通信方面开销更大。 | 同一进程内部的多个线程可以并发执行，共享内存通信方便，但需要注意同步与互斥问题。 | ### 核心思想总结 - **进程**：强调“**资源分配**”的独立性。操作系统以进程为单位分配内存、文件等资源，进程间互不干扰。 - **线程**：强调“**CPU执行**”的轻量化。线程是进程内的执行流，共享进程资源，从而实现更高效的任务协作和并发。 ### 考点链路与关联 1. **与作业/程序的关系**：程序是静态代码，作业是用户提交的任务，进程是作业的执行实体，线程是进程的执行单元。 2. **与处理机调度的关联**：现代操作系统（如Linux）的调度实体是**轻量级进程（LWP）**，本质上是内核线程。调度器调度的是线程，而非进程。 3. **与并发/同步的关联**：多线程编程模型（如Java的Thread类）直接基于线程概念。进程间通信（IPC）和线程间同步（如互斥锁、信号量）是操作系统与编程语言中的重要考点。 4. **与多核/多处理器的关联**：多线程可以更好地利用多核处理器的并行计算能力，实现真正的并行执行，而多进程也能并行，但上下文切换开销更大。 **📚 考点出处** - **学科**：操作系统 - **知识点**： 1. **进程与线程的基本概念**（《操作系统教程》第2章或第4章：进程管理） 2. **进程与线程的比较**（常作为简答题或选择题考点） 3. **线程的实现方式**（用户级线程与内核级线程） 4. **进程同步与通信**（关联线程同步机制） 5. **处理机调度**（调度对象与上下文切换）',NULL,NULL,'AI答疑',3,0,0,0,'2026-06-07',NULL,0,'2026-06-06 19:19:04','2026-06-06 19:19:04',6,'AI_CHAT'),(4,8,'MATH','怎么做？','这是一个经典的二重积分问题，利用**对称性**和**极坐标变换**可以极大地简化计算过程。以下是详细的推导步骤： ### 1. 分析积分区域 $D$ 首先明确积分区域 $D$ 的几何形状： - 边界由 $y = |x|$（两条直线 $y=x$ 和 $y=-x$）与 $y=1$ 围成。 - 这是一个以原点为顶点，开口向上的三角形区域。 - 区域范围：$x \\in [-1, 1]$，且对于任意 $x$， $|x| \\le y \\le 1$。 - **关键性质**：区域 $D$ 关于 $y$ 轴对称（即关于 $x=0$ 对称）。 ### 2. 分析被积函数 $f(x,y)$ 设被积函数为 $f(x,y) = \\frac{x^2 - x\\cos y - y^2}{x^2 + y^2}$。 我们可以将其拆分为三部分进行观察： $$ f(x,y) = \\frac{x^2 - y^2}{x^2 + y^2} - \\frac{x\\cos y}{x^2 + y^2} $$ **利用奇偶性简化：** - **第一部分** $\\frac{x^2 - y^2}{x^2 + y^2}$： 令 $g(x,y) = \\frac{x^2 - y^2}{x^2 + y^2}$。 由于 $g(-x, y) = \\frac{(-x)^2 - y^2}{(-x)^2 + y^2} = \\frac{x^2 - y^2}{x^2 + y^2} = g(x,y)$，该部分是关于 $x$ 的**偶函数**。 - **第二部分** $-\\frac{x\\cos y}{x^2 + y^2}$： 令 $h(x,y) = \\frac{x\\cos y}{x^2 + y^2}$。 由于 $h(-x, y) = \\frac{(-x)\\cos y}{(-x)^2 + y^2} = -\\frac{x\\cos y}{x^2 + y^2} = -h(x,y)$，该部分是关于 $x$ 的**奇函数**。 **根据对称性定理**： 若积分区域 $D$ 关于 $y$ 轴对称，且被积函数关于 $x$ 是奇函数，则该部分积分为 0。 即：$\\iint_D -\\frac{x\\cos y}{x^2 + y^2} dxdy = 0$。 因此，原积分简化为： $$ I = \\iint_D \\frac{x^2 - y^2}{x^2 + y^2} dxdy $$ ### 3. 利用极坐标计算简化后的积分 由于区域涉及 $x^2+y^2$，且被积函数形式适合极坐标，我们采用极坐标变换： - $x = r\\cos\\theta$ - $y = r\\sin\\theta$ - $dxdy = r dr d\\theta$ - $\\frac{x^2 - y^2}{x^2 + y^2} = \\frac{r^2(\\cos^2\\theta - \\sin^2\\theta)}{r^2} = \\cos(2\\theta)$ **确定积分限**： - 角度 $\\theta$：区域夹在 $y=|x|$ 之间。 - 当 $x&gt;0$ 时，$y=x \\Rightarrow \\tan\\theta = 1 \\Rightarrow \\theta = \\frac{\\pi}{4}$。 - 当 $x&lt;0$ 时，$y=-x \\Rightarrow \\tan\\theta = -1 \\Rightarrow \\theta = \\frac{3\\pi}{4}$。 - 所以 $\\theta \\in [\\frac{\\pi}{4}, \\frac{3\\pi}{4}]$。 - 半径 $r$：下界是射线 $y=|x|$，上界是直线 $y=1$。 - 将 $y=1$ 代入极坐标：$r\\sin\\theta = 1 \\Rightarrow r = \\frac{1}{\\sin\\theta} = \\csc\\theta$。 - 所以 $r \\in [0, \\csc\\theta]$。 **建立积分式**： $$ I = \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} d\\theta \\int_{0}^{\\csc\\theta} \\cos(2\\theta) \\cdot r dr $$ **计算内层积分**（对 $r$ 积分）： $$ \\int_{0}^{\\csc\\theta} r dr = \\left[ \\frac{1}{2}r^2 \\right]_0^{\\csc\\theta} = \\frac{1}{2}\\csc^2\\theta $$ **代入外层积分**（对 $\\theta$ 积分）： $$ I = \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} \\cos(2\\theta) \\cdot \\frac{1}{2}\\csc^2\\theta d\\theta $$ 利用三角恒等式化简被积函数： $$ \\cos(2\\theta)\\csc^2\\theta = (\\cos^2\\theta - \\sin^2\\theta) \\cdot \\frac{1}{\\sin^2\\theta} = \\cot^2\\theta - 1 $$ 或者使用 $\\cot^2\\theta = \\csc^2\\theta - 1$，则被积函数变为 $(\\csc^2\\theta - 1) - 1 = \\csc^2\\theta - 2$。 所以： $$ I = \\frac{1}{2} \\int_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} (\\csc^2\\theta - 2) d\\theta $$ **计算定积分**： 原函数为 $-\\cot\\theta - 2\\theta$。 $$ I = \\frac{1}{2} \\left[ -\\cot\\theta - 2\\theta \\right]_{\\frac{\\pi}{4}}^{\\frac{3\\pi}{4}} $$ 代入上下限： - 上限 $\\frac{3\\pi}{4}$：$-\\cot(\\frac{3\\pi}{4}) - 2(\\frac{3\\pi}{4}) = -(-1) - \\frac{3\\pi}{2} = 1 - \\frac{3\\pi}{2}$ - 下限 $\\frac{\\pi}{4}$：$-\\cot(\\frac{\\pi}{4}) - 2(\\frac{\\pi}{4}) = -(1) - \\frac{\\pi}{2} = -1 - \\frac{\\pi}{2}$ 相减： $$ \\text{括号内值} = (1 - \\frac{3\\pi}{2}) - (-1 - \\frac{\\pi}{2}) = 2 - \\pi $$ 最终结果： $$ I = \\frac{1}{2} (2 - \\pi) = 1 - \\frac{\\pi}{2} $$ --- ### 📚 考点出处 - **知识点**：二重积分的计算（对称性应用、极坐标变换） - **学科**：数学（考研数学一/二/三） - **关联链路**： 1. **对称性判定**：识别区域关于 $y$ 轴对称，结合被积函数中奇次项 $x$ 的消去（这是解题的关键捷径）。 2. **极坐标转换**：处理分母为 $x^2+y^2$ 以及圆形/扇形区域时的标准方法。 3. **三角恒等变形**：将 $\\cos(2\\theta)/\\sin^2\\theta$ 转化为 $\\csc^2\\theta$ 或 $\\cot^2\\theta$ 以便积分。','/uploads/images/202606/5c2907bd37a0401682241391638ecc9a.png',NULL,'AI答疑',3,0,0,0,'2026-06-07',NULL,0,'2026-06-06 21:31:07','2026-06-06 21:31:07',NULL,'AI_CHAT');
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
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='择校推荐历史表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recommendation_history`
--

LOCK TABLES `recommendation_history` WRITE;
/*!40000 ALTER TABLE `recommendation_history` DISABLE KEYS */;
INSERT INTO `recommendation_history` VALUES (1,8,'{\"gpa\": 3, \"englishLevel\": \"CET4\", \"prepDuration\": 6, \"mockExamScore\": 500, \"riskPreference\": \"moderate\", \"undergradSchool\": \"河南理工大学\"}','{\"match\": [{\"logoUrl\": null, \"location\": \"浙江\", \"schoolId\": 3, \"matchScore\": 70, \"schoolName\": \"浙江大学\", \"matchReason\": \"院校浙江大学与您的水平较为匹配，建议深入了解\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.83, \"avgAdmissionScore\": 400}], \"reach\": [], \"safety\": [{\"logoUrl\": null, \"location\": \"北京\", \"schoolId\": 1, \"matchScore\": 71, \"schoolName\": \"清华大学\", \"matchReason\": \"院校清华大学往年录取均分低于您的模考80分，是稳妥保底选择\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.74, \"avgAdmissionScore\": 420}, {\"logoUrl\": null, \"location\": \"北京\", \"schoolId\": 2, \"matchScore\": 71, \"schoolName\": \"北京大学\", \"matchReason\": \"院校北京大学往年录取均分低于您的模考82分，是稳妥保底选择\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.75, \"avgAdmissionScore\": 418}, {\"logoUrl\": null, \"location\": \"上海\", \"schoolId\": 4, \"matchScore\": 70, \"schoolName\": \"上海交通大学\", \"matchReason\": \"院校上海交通大学往年录取均分低于您的模考95分，是稳妥保底选择\", \"schoolLevel\": \"C9\", \"relatedMajors\": [], \"admitProbability\": 0.81, \"avgAdmissionScore\": 405}], \"similarUsers\": [{\"userId\": 5, \"username\": \"政**\", \"similarity\": 0.9, \"englishLevel\": \"CET4\", \"prepDuration\": 6, \"undergradGpa\": 2.9, \"admittedMajor\": \"计算机应用技术\", \"admittedSchool\": \"河南大学\", \"examScoreTotal\": 318, \"undergradSchool\": \"河南农业大学\"}, {\"userId\": 2, \"username\": \"考**\", \"similarity\": 0.85, \"englishLevel\": \"CET6\", \"prepDuration\": 8, \"undergradGpa\": 3.1, \"admittedMajor\": \"计算机科学与技术\", \"admittedSchool\": \"郑州大学\", \"examScoreTotal\": 355, \"undergradSchool\": \"河南理工大学\"}, {\"userId\": 3, \"username\": \"4**\", \"similarity\": 0.82, \"englishLevel\": \"CET6\", \"prepDuration\": 10, \"undergradGpa\": 3.5, \"admittedMajor\": \"软件工程\", \"admittedSchool\": \"华中科技大学\", \"examScoreTotal\": 392, \"undergradSchool\": \"武汉科技大学\"}]}','2026-06-05 21:16:33'),(2,8,'{\"gpa\": 3, \"englishLevel\": \"CET4\", \"prepDuration\": 6, \"mockExamScore\": 400, \"riskPreference\": \"moderate\", \"undergradSchool\": \"河南理工大学\"}','{\"match\": [{\"logoUrl\": null, \"location\": \"浙江\", \"schoolId\": 3, \"matchScore\": 78, \"schoolName\": \"浙江大学\", \"matchReason\": \"院校浙江大学往年录取均分与您的模考分数相近，值得重点关注\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.45, \"avgAdmissionScore\": 400}, {\"logoUrl\": null, \"location\": \"上海\", \"schoolId\": 4, \"matchScore\": 77, \"schoolName\": \"上海交通大学\", \"matchReason\": \"院校上海交通大学往年录取均分与您的模考分数相近，值得重点关注\", \"schoolLevel\": \"C9\", \"relatedMajors\": [], \"admitProbability\": 0.42, \"avgAdmissionScore\": 405}, {\"logoUrl\": null, \"location\": \"北京\", \"schoolId\": 2, \"matchScore\": 76, \"schoolName\": \"北京大学\", \"matchReason\": \"院校北京大学与您的水平较为匹配，建议深入了解\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.35, \"avgAdmissionScore\": 418}], \"reach\": [{\"logoUrl\": null, \"location\": \"北京\", \"schoolId\": 1, \"matchScore\": 76, \"schoolName\": \"清华大学\", \"matchReason\": \"院校清华大学往年录取均分高于您的模考20分，需要加油冲刺\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.33, \"avgAdmissionScore\": 420}, {\"logoUrl\": null, \"location\": \"湖北\", \"schoolId\": 5, \"matchScore\": 74, \"schoolName\": \"华中科技大学\", \"matchReason\": \"院校华中科技大学竞争激烈，作为冲刺目标需要加倍努力\", \"schoolLevel\": \"985\", \"relatedMajors\": [\"计算机科学与技术\", \"网络空间安全\"], \"admitProbability\": 0.55, \"avgAdmissionScore\": 385}, {\"logoUrl\": null, \"location\": \"湖北\", \"schoolId\": 6, \"matchScore\": 73, \"schoolName\": \"武汉大学\", \"matchReason\": \"院校武汉大学竞争激烈，作为冲刺目标需要加倍努力\", \"schoolLevel\": \"985\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.58, \"avgAdmissionScore\": 380}], \"safety\": [{\"logoUrl\": null, \"location\": \"河南\", \"schoolId\": 7, \"matchScore\": 69, \"schoolName\": \"郑州大学\", \"matchReason\": \"院校郑州大学往年录取均分低于您的模考50分，是稳妥保底选择\", \"schoolLevel\": \"211\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.74, \"avgAdmissionScore\": 350}, {\"logoUrl\": null, \"location\": \"江西\", \"schoolId\": 8, \"matchScore\": 68, \"schoolName\": \"南昌大学\", \"matchReason\": \"院校南昌大学往年录取均分低于您的模考60分，是稳妥保底选择\", \"schoolLevel\": \"211\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.79, \"avgAdmissionScore\": 340}, {\"logoUrl\": null, \"location\": \"广东\", \"schoolId\": 10, \"matchScore\": 66, \"schoolName\": \"深圳大学\", \"matchReason\": \"院校深圳大学往年录取均分低于您的模考45分，是稳妥保底选择\", \"schoolLevel\": \"DOUBLE_NON\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.72, \"avgAdmissionScore\": 355}], \"similarUsers\": [{\"userId\": 5, \"username\": \"政**\", \"similarity\": 0.95, \"englishLevel\": \"CET4\", \"prepDuration\": 6, \"undergradGpa\": 2.9, \"admittedMajor\": \"计算机应用技术\", \"admittedSchool\": \"河南大学\", \"examScoreTotal\": 318, \"undergradSchool\": \"河南农业大学\"}, {\"userId\": 2, \"username\": \"考**\", \"similarity\": 0.9, \"englishLevel\": \"CET6\", \"prepDuration\": 8, \"undergradGpa\": 3.1, \"admittedMajor\": \"计算机科学与技术\", \"admittedSchool\": \"郑州大学\", \"examScoreTotal\": 355, \"undergradSchool\": \"河南理工大学\"}, {\"userId\": 3, \"username\": \"4**\", \"similarity\": 0.87, \"englishLevel\": \"CET6\", \"prepDuration\": 10, \"undergradGpa\": 3.5, \"admittedMajor\": \"软件工程\", \"admittedSchool\": \"华中科技大学\", \"examScoreTotal\": 392, \"undergradSchool\": \"武汉科技大学\"}]}','2026-06-05 21:17:14'),(3,8,'{\"gpa\": 3, \"englishLevel\": \"CET4\", \"prepDuration\": 6, \"mockExamScore\": 400, \"riskPreference\": \"aggressive\", \"undergradSchool\": \"河南理工大学\"}','{\"match\": [{\"logoUrl\": null, \"location\": \"浙江\", \"schoolId\": 3, \"matchScore\": 78, \"schoolName\": \"浙江大学\", \"matchReason\": \"院校浙江大学往年录取均分与您的模考分数相近，值得重点关注\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.45, \"avgAdmissionScore\": 400}, {\"logoUrl\": null, \"location\": \"上海\", \"schoolId\": 4, \"matchScore\": 77, \"schoolName\": \"上海交通大学\", \"matchReason\": \"院校上海交通大学往年录取均分与您的模考分数相近，值得重点关注\", \"schoolLevel\": \"C9\", \"relatedMajors\": [], \"admitProbability\": 0.42, \"avgAdmissionScore\": 405}, {\"logoUrl\": null, \"location\": \"北京\", \"schoolId\": 2, \"matchScore\": 76, \"schoolName\": \"北京大学\", \"matchReason\": \"院校北京大学与您的水平较为匹配，建议深入了解\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.35, \"avgAdmissionScore\": 418}], \"reach\": [{\"logoUrl\": null, \"location\": \"北京\", \"schoolId\": 1, \"matchScore\": 76, \"schoolName\": \"清华大学\", \"matchReason\": \"院校清华大学往年录取均分高于您的模考20分，需要加油冲刺\", \"schoolLevel\": \"C9\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.33, \"avgAdmissionScore\": 420}, {\"logoUrl\": null, \"location\": \"湖北\", \"schoolId\": 5, \"matchScore\": 74, \"schoolName\": \"华中科技大学\", \"matchReason\": \"院校华中科技大学竞争激烈，作为冲刺目标需要加倍努力\", \"schoolLevel\": \"985\", \"relatedMajors\": [\"计算机科学与技术\", \"网络空间安全\"], \"admitProbability\": 0.55, \"avgAdmissionScore\": 385}, {\"logoUrl\": null, \"location\": \"湖北\", \"schoolId\": 6, \"matchScore\": 73, \"schoolName\": \"武汉大学\", \"matchReason\": \"院校武汉大学竞争激烈，作为冲刺目标需要加倍努力\", \"schoolLevel\": \"985\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.58, \"avgAdmissionScore\": 380}], \"safety\": [{\"logoUrl\": null, \"location\": \"河南\", \"schoolId\": 7, \"matchScore\": 69, \"schoolName\": \"郑州大学\", \"matchReason\": \"院校郑州大学往年录取均分低于您的模考50分，是稳妥保底选择\", \"schoolLevel\": \"211\", \"relatedMajors\": [\"计算机科学与技术\", \"软件工程\"], \"admitProbability\": 0.74, \"avgAdmissionScore\": 350}, {\"logoUrl\": null, \"location\": \"江西\", \"schoolId\": 8, \"matchScore\": 68, \"schoolName\": \"南昌大学\", \"matchReason\": \"院校南昌大学往年录取均分低于您的模考60分，是稳妥保底选择\", \"schoolLevel\": \"211\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.79, \"avgAdmissionScore\": 340}, {\"logoUrl\": null, \"location\": \"广东\", \"schoolId\": 10, \"matchScore\": 66, \"schoolName\": \"深圳大学\", \"matchReason\": \"院校深圳大学往年录取均分低于您的模考45分，是稳妥保底选择\", \"schoolLevel\": \"DOUBLE_NON\", \"relatedMajors\": [\"计算机科学与技术\"], \"admitProbability\": 0.72, \"avgAdmissionScore\": 355}], \"similarUsers\": [{\"userId\": 5, \"username\": \"政**\", \"similarity\": 0.95, \"englishLevel\": \"CET4\", \"prepDuration\": 6, \"undergradGpa\": 2.9, \"admittedMajor\": \"计算机应用技术\", \"admittedSchool\": \"河南大学\", \"examScoreTotal\": 318, \"undergradSchool\": \"河南农业大学\"}, {\"userId\": 2, \"username\": \"考**\", \"similarity\": 0.9, \"englishLevel\": \"CET6\", \"prepDuration\": 8, \"undergradGpa\": 3.1, \"admittedMajor\": \"计算机科学与技术\", \"admittedSchool\": \"郑州大学\", \"examScoreTotal\": 355, \"undergradSchool\": \"河南理工大学\"}, {\"userId\": 3, \"username\": \"4**\", \"similarity\": 0.87, \"englishLevel\": \"CET6\", \"prepDuration\": 10, \"undergradGpa\": 3.5, \"admittedMajor\": \"软件工程\", \"admittedSchool\": \"华中科技大学\", \"examScoreTotal\": 392, \"undergradSchool\": \"武汉科技大学\"}]}','2026-06-05 21:17:37');
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
INSERT INTO `sys_user` VALUES (1,'Admin管理员','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','admin@example.com','13800000001','ADMIN','https://dummyimage.com/100x100/000/fff&text=Admin',NULL,9999,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(2,'考研高数版主','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','mod@example.com','13800000002','MODERATOR','https://dummyimage.com/100x100/007bff/fff&text=Mod','数学与应用数学',5000,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(3,'408上岸人','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user1@example.com','13800000003','USER','https://dummyimage.com/100x100/28a745/fff&text=U1','计算机科学与技术',150,0,'2026-04-17 20:44:07','2026-06-04 19:48:42',1),(4,'英语困难户','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user2@example.com','13800000004','USER','https://dummyimage.com/100x100/dc3545/fff&text=U2','金融学',200,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(5,'政治背书狂','$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2','user3@example.com','13800000005','USER','https://dummyimage.com/100x100/ffc107/000&text=U3','法学',300,0,'2026-04-17 20:44:07','2026-04-17 20:44:07',0),(6,'DB','$2a$10$PYWXnjNRZ0qPWpYwQdU2aO.VKorN3YQyAhoof8D4E.vp5DVNWAdsu','1@qq.com','15713775462','USER','','408',3,0,'2026-04-17 20:46:42','2026-06-04 19:48:42',1),(7,'吴东博','$2a$10$DMcxnrmr8prWhiQxc5UmAuvtd0nVVv6gk.ASAvXP8B.HWIKEBHCTm','2@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-04-17 22:01:13','2026-04-17 22:01:13',0),(8,'db886','$2a$10$OSRZaL2uNGWAgpWspS.W4uyL30h9kae4btbzuJ8ro2XCiSaDsLDYC','123@qq.com','','USER','/uploads/images/202605/e42c48045e284f4c83fc6fc274d4a6f1.jpg','',9,0,'2026-05-20 11:42:11','2026-06-05 20:27:11',0),(9,'swtest','$2a$10$r6ZOziJTHeLeFoHH4zw.ueO3GqVij9IAC/DB21Q6OqwLxtWqC9ec.','swtest@test.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 11:50:26','2026-05-20 11:50:26',0),(10,'swtest2','$2a$10$aRLrJm/wUBqhPxsU6wR2luz5sxFl7OekZIbQVf6hQV1m.HmBD6SN6','swtest2@test.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 11:50:54','2026-05-20 11:50:54',0),(11,'wdb2','$2a$10$DZ73yHUtXMCtOpl3AihWy.Ho/4Ju8aLeGykEUSaHCk7xN0cQW4cXy','1234@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-05-20 14:35:40','2026-05-20 14:35:40',0),(12,'db88688','$2a$10$Mr..XY5qEDhUXiJ.yY6YKO48.WFovmyLOhLn5y2.5E2g1QrkDIeLu','123456@qq.com',NULL,'USER',NULL,NULL,0,0,'2026-06-05 19:32:36','2026-06-05 19:32:36',0);
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
INSERT INTO `sys_user_stats` VALUES (2,1,0,'2026-04-18 18:15:26',0),(3,1,3,'2026-04-18 18:15:26',0),(4,1,1,'2026-04-18 18:15:26',0),(5,1,2,'2026-04-18 18:15:26',0),(6,5,7,'2026-05-20 15:30:14',0),(8,12,1,'2026-06-06 23:19:11',0),(10,1,0,'2026-05-20 11:51:29',0);
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
INSERT INTO `user_ai_profile` VALUES (1,8,'{\"totalCheckDays\":2,\"continuousDays\":1,\"totalStudyHours\":8,\"lastActive\":\"2026-06-04\",\"completedTaskCount\":1,\"completedTaskCount\":2,\"completedTaskCount\":3}','{\"recentEmotion\":\"一般\",\"lastAnalysis\":\"我深深理解你现在的心情，伤心和难过是很自然的情绪。请记得，你不是一个人在战斗。试着放慢脚步，给自己一个拥抱，深呼吸几次。在复习上，不妨把学习计划拆分成更小的部分，每天完成一点，这样能让你更有成就感。如果觉得压力大，和信任的人分享一下，他们一定能给你力量。一切都会好起来的。\",\"updatedAt\":\"2026-06-05T20:27:24.726774664\"}','2026-06-04 06:35:13','2026-06-05 12:27:25');
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
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-07 10:59:46
