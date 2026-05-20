<template>
  <div class="interview-room">
    <!-- ==================== 顶部信息栏 ==================== -->
    <header class="interview-header">
      <div class="header-info">
        <span class="school-name">{{ sessionInfo.targetSchool || '目标院校' }}</span>
        <span class="divider">|</span>
        <span class="major-name">{{ sessionInfo.targetMajor || '目标专业' }}</span>
        <el-tag
          :type="typeTagColor"
          size="small"
          class="type-tag"
        >
          {{ interviewTypeLabel }}
        </el-tag>
      </div>
      <div class="header-status">
        <span v-if="sessionInfo.status === 'IN_PROGRESS'" class="status-dot active" />
        <span v-else class="status-dot done" />
        {{ sessionInfo.status === 'IN_PROGRESS' ? '面试进行中' : '已生成报告' }}
      </div>
    </header>

    <!-- ==================== 中间对话列表区 ==================== -->
    <main class="chat-area" ref="chatContainerRef">
      <!-- 空状态占位 -->
      <div v-if="messages.length === 0 && !loading" class="empty-hint">
        <el-icon :size="48" color="#ccc"><ChatDotRound /></el-icon>
        <p>面试即将开始，AI 面试官正在准备第一个问题...</p>
      </div>

      <!-- 对话气泡列表 -->
      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-row', msg.role === 'user' ? 'row-right' : 'row-left']"
      >
        <!-- AI 头像 -->
        <div v-if="msg.role === 'ai'" class="avatar ai-avatar">
          <el-icon :size="20"><School /></el-icon>
        </div>

        <!-- 对话气泡 -->
        <div :class="['bubble', msg.role === 'user' ? 'bubble-user' : 'bubble-ai']">
          <div class="bubble-role">{{ msg.role === 'user' ? '我' : 'AI 面试官' }}</div>
          <div class="bubble-content">{{ msg.content }}</div>
          <!-- 用户消息可能包含的语音得分 -->
          <div v-if="msg.fluencyScore != null" class="fluency-badge">
            流利度: {{ msg.fluencyScore }} 分
          </div>
        </div>

        <!-- 用户头像 -->
        <div v-if="msg.role === 'user'" class="avatar user-avatar">
          <el-icon :size="20"><UserFilled /></el-icon>
        </div>
      </div>

      <!-- AI 正在思考的 loading 动画 -->
      <div v-if="aiThinking" class="message-row row-left">
        <div class="avatar ai-avatar">
          <el-icon :size="20"><School /></el-icon>
        </div>
        <div class="bubble bubble-ai thinking-bubble">
          <span class="dot-pulse" />
          <span class="dot-pulse delay-1" />
          <span class="dot-pulse delay-2" />
        </div>
      </div>
    </main>

    <!-- ==================== 底部操作区 ==================== -->
    <footer class="interview-footer">
      <!-- 按住说话 / 结束回答 按钮 -->
      <button
        class="record-btn"
        :class="{ recording: isRecording }"
        :disabled="sessionInfo.status !== 'IN_PROGRESS'"
        @pointerdown.prevent="startRecording"
        @pointerup.prevent="stopRecording"
        @pointerleave.prevent="stopRecording"
        @touchstart.prevent="startRecording"
        @touchend.prevent="stopRecording"
        @touchcancel.prevent="stopRecording"
      >
        <span v-if="isRecording" class="record-icon pulse" />
        <span v-else class="record-icon idle" />
        <span class="record-text">{{ isRecording ? '结束回答' : '按住说话' }}</span>
      </button>

      <!-- 结束面试按钮 -->
      <el-button
        type="danger"
        :icon="SwitchButton"
        :disabled="sessionInfo.status !== 'IN_PROGRESS'"
        :loading="endingInterview"
        @click="handleEndInterview"
      >
        结束面试并生成报告
      </el-button>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ChatDotRound, School, UserFilled, SwitchButton } from '@element-plus/icons-vue'

// ============================================================
// Props
// ============================================================

const props = defineProps<{
  sessionId: number
}>()

// ============================================================
// 类型定义
// ============================================================

interface MessageItem {
  role: 'user' | 'ai'
  content: string
  fluencyScore?: number | null
  createdAt?: string
}

interface SessionInfo {
  targetSchool: string
  targetMajor: string
  interviewType: string
  status: string
}

// ============================================================
// 响应式状态
// ============================================================

/** 面试会话信息 */
const sessionInfo = reactive<SessionInfo>({
  targetSchool: '',
  targetMajor: '',
  interviewType: '',
  status: 'IN_PROGRESS'
})

/** 对话消息列表 */
const messages = ref<MessageItem[]>([])

/** 是否正在录音 */
const isRecording = ref(false)

/** AI 是否正在思考生成追问 */
const aiThinking = ref(false)

/** 页面首次加载 */
const loading = ref(true)

/** 结束面试 loading */
const endingInterview = ref(false)

/** 对话列表容器 DOM 引用 */
const chatContainerRef = ref<HTMLElement | null>(null)

/**
 * 模拟的 MediaRecorder 实例（壳代码，后续对接真实录音）
 */
let mockMediaRecorder: { state: string; start: () => void; stop: () => void } | null = null

// ============================================================
// 计算属性
// ============================================================

/** 面试类型 → Element Plus Tag 颜色映射 */
const typeTagColor = computed(() => {
  const map: Record<string, string> = {
    ENGLISH: 'primary',
    MAJOR: 'success',
    COMPREHENSIVE: 'warning'
  }
  return map[sessionInfo.interviewType] ?? 'info'
})

/** 面试类型标签文字 */
const interviewTypeLabel = computed(() => {
  const map: Record<string, string> = {
    ENGLISH: '英文面试',
    MAJOR: '专业课面试',
    COMPREHENSIVE: '综合面试'
  }
  return map[sessionInfo.interviewType] ?? sessionInfo.interviewType
})

// ============================================================
// API 调用（后端接口实现后对接）
// ============================================================

/**
 * 获取会话基本信息
 * GET /api/interview/session/{sessionId}
 */
async function fetchSessionInfo(): Promise<void> {
  try {
    const res = await fetch(`/api/interview/session/${props.sessionId}`)
    const json = await res.json()
    if (json.code === 200) {
      Object.assign(sessionInfo, json.data)
    }
  } catch (e) {
    console.error('获取面试会话信息失败:', e)
  }
}

/**
 * 获取历史对话记录
 * GET /api/interview/session/{sessionId}/records
 */
async function fetchRecords(): Promise<void> {
  try {
    const res = await fetch(`/api/interview/session/${props.sessionId}/records`)
    const json = await res.json()
    if (json.code === 200) {
      // 将后端 InterviewRecord 列表映射为 MessageItem
      messages.value = (json.data ?? []).map((r: any) => ({
        role: r.role === 'ai' ? 'ai' : 'user',
        content: r.content,
        fluencyScore: r.fluencyScore ?? null
      }))
      await scrollToBottom()
    }
  } catch (e) {
    console.error('获取历史对话记录失败:', e)
  } finally {
    loading.value = false
  }
}

/**
 * 将用户回答发送给后端，获取 AI 追问
 * POST /api/interview/session/{sessionId}/next-question
 *
 * @param answer 用户回答文本
 */
async function sendAnswerToAI(answer: string): Promise<string | null> {
  const res = await fetch(`/api/interview/session/${props.sessionId}/next-question`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ answer })
  })
  const json = await res.json()
  if (json.code === 200) {
    // 后端返回保存后的 InterviewRecord 对象
    return json.data?.content ?? null
  }
  ElMessage.error(json.message || 'AI 响应失败')
  return null
}

/**
 * 结束面试并生成报告
 * POST /api/interview/session/{sessionId}/finish
 */
async function finishInterview(): Promise<void> {
  const res = await fetch(`/api/interview/session/${props.sessionId}/finish`, {
    method: 'POST'
  })
  const json = await res.json()
  if (json.code === 200) {
    sessionInfo.status = 'REPORTED'
  } else {
    throw new Error(json.message || '结束面试失败')
  }
}

// ============================================================
// 录音逻辑（MediaRecorder 壳代码）
// ============================================================

/**
 * 开始录音
 *
 * TODO：【重要】对接真实语音识别（ASR）时的改造步骤：
 * 1. 调用 navigator.mediaDevices.getUserMedia({ audio: true }) 获取麦克风权限
 * 2. 创建真实的 MediaRecorder 实例
 * 3. 监听 ondataavailable 事件收集音频 Blob
 * 4. 停止时将 Blob 发送给 ASR 服务，拿到识别文本后调用 sendAnswerToAI()
 */
function startRecording(): void {
  // 状态保护：非 IN_PROGRESS 状态不允许录音
  if (sessionInfo.status !== 'IN_PROGRESS') return

  isRecording.value = true
  console.log('[InterviewRoom] 开始录音...')

  // -------- 模拟实现（后续替换为真实 MediaRecorder）--------
  mockMediaRecorder = {
    state: 'recording',
    start() {
      console.log('[Mock MediaRecorder] 录音已启动')
    },
    stop() {
      console.log('[Mock MediaRecorder] 录音已停止')
    }
  }
  mockMediaRecorder.start()
  // --------------------------------------------------------
}

/**
 * 结束录音，获取识别文本并发送给 AI
 */
function stopRecording(): void {
  // 非录音状态不处理
  if (!isRecording.value) return

  isRecording.value = false
  console.log('[InterviewRoom] 结束录音...')

  // -------- 模拟实现：停止 MediaRecorder --------
  if (mockMediaRecorder && mockMediaRecorder.state !== 'inactive') {
    mockMediaRecorder.stop()
  }
  // -------------------------------------------

  // -------- 模拟实现：用 Mock 字符串代替 ASR 识别结果 --------
  // TODO：【重要】真实对接时，将音频 Blob 发送给 ASR 服务，
  //       用服务返回的识别文本替换此 Mock 字符串
  const mockTranscribedText = `[Mock] 这是我的第 ${messages.value.filter(m => m.role === 'user').length + 1} 轮回答，内容是模拟语音识别的文本。`
  // -------------------------------------------------------

  // 将用户消息追加到对话列表
  messages.value.push({
    role: 'user',
    content: mockTranscribedText,
    fluencyScore: null // TODO：对接语音分析服务后填充
  })
  scrollToBottom()

  // 调用后端接口获取 AI 回复
  handleSendAnswer(mockTranscribedText)
}

// ============================================================
// 交互逻辑
// ============================================================

/**
 * 将用户回答发送给后端 AI，并处理返回结果
 */
async function handleSendAnswer(answer: string): Promise<void> {
  aiThinking.value = true
  try {
    const aiContent = await sendAnswerToAI(answer)
    if (aiContent) {
      messages.value.push({
        role: 'ai',
        content: aiContent
      })
      await scrollToBottom()
    }
  } catch (e) {
    console.error('获取 AI 回复失败:', e)
    ElMessage.error('AI 面试官响应失败，请重试')
  } finally {
    aiThinking.value = false
  }
}

/**
 * 结束面试并生成报告
 */
async function handleEndInterview(): Promise<void> {
  try {
    await ElMessageBox.confirm(
      '确定要结束本次面试并生成评估报告吗？结束后将无法继续作答。',
      '结束面试',
      { confirmButtonText: '确定结束', cancelButtonText: '继续面试', type: 'warning' }
    )
  } catch {
    return // 用户取消操作
  }

  endingInterview.value = true
  try {
    await finishInterview()
    ElMessage.success('面试报告已生成！')
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    endingInterview.value = false
  }
}

// ============================================================
// 辅助工具
// ============================================================

/** 滚动对话列表到底部 */
async function scrollToBottom(): Promise<void> {
  await nextTick()
  const el = chatContainerRef.value
  if (el) {
    el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' })
  }
}

// ============================================================
// 生命周期
// ============================================================

onMounted(async () => {
  // 并行加载会话信息与历史记录
  await Promise.all([fetchSessionInfo(), fetchRecords()])

  // 如果尚无对话记录，AI 主动发起第一问
  if (messages.value.length === 0 && sessionInfo.status === 'IN_PROGRESS') {
    // 发送一个空内容或特殊标记让后端生成开场白
    handleSendAnswer('（面试开始）')
  }
})
</script>

<style scoped>
/* ============================================================
   整体布局：Header + Chat + Footer 三段式，占满视口
   ============================================================ */
.interview-room {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 800px;
  margin: 0 auto;
  background: #f5f7fa;
  overflow: hidden;
}

/* ---- 顶部信息栏 ---- */
.interview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}
.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}
.divider {
  color: #c0c4cc;
}
.type-tag {
  margin-left: 4px;
}
.header-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #909399;
}
.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.status-dot.active {
  background: #67c23a;
  animation: breathe 1.5s infinite;
}
.status-dot.done {
  background: #909399;
}

/* ---- 中间对话区 ---- */
.chat-area {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.empty-hint {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #c0c4cc;
  gap: 12px;
  font-size: 14px;
}

/* 消息行 */
.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  max-width: 85%;
}
.row-left {
  align-self: flex-start;
}
.row-right {
  align-self: flex-end;
  flex-direction: row-reverse;
}

/* 头像 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.ai-avatar {
  background: #e6f0ff;
  color: #409eff;
}
.user-avatar {
  background: #e8f5e9;
  color: #67c23a;
}

/* 气泡 */
.bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
}
.bubble-ai {
  background: #fff;
  border-top-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  color: #303133;
}
.bubble-user {
  background: #409eff;
  color: #fff;
  border-top-right-radius: 4px;
}
.bubble-role {
  font-size: 12px;
  margin-bottom: 4px;
  opacity: 0.7;
}
.bubble-content {
  white-space: pre-wrap;
}
.fluency-badge {
  margin-top: 6px;
  font-size: 12px;
  opacity: 0.8;
  border-top: 1px solid rgba(255, 255, 255, 0.3);
  padding-top: 4px;
}

/* AI 思考中的气泡 */
.thinking-bubble {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 16px 20px;
}
.dot-pulse {
  width: 8px;
  height: 8px;
  background: #c0c4cc;
  border-radius: 50%;
  animation: dotBounce 0.8s infinite;
}
.dot-pulse.delay-1 { animation-delay: 0.15s; }
.dot-pulse.delay-2 { animation-delay: 0.30s; }

/* ---- 底部操作区 ---- */
.interview-footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 16px 16px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}

/* 录音按钮 */
.record-btn {
  width: 100%;
  max-width: 320px;
  height: 56px;
  border: none;
  border-radius: 28px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.25s ease;
  background: #fff;
  color: #409eff;
  border: 2px solid #409eff;
  user-select: none;
  -webkit-user-select: none;
  /* 防止长按触发文本选择 */
  touch-action: manipulation;
}
.record-btn:active {
  transform: scale(0.97);
}
.record-btn.recording {
  background: #fef0f0;
  color: #f56c6c;
  border-color: #f56c6c;
  box-shadow: 0 0 16px rgba(245, 108, 108, 0.35);
}
.record-btn:disabled {
  background: #f5f7fa;
  color: #c0c4cc;
  border-color: #e4e7ed;
  cursor: not-allowed;
}

.record-icon {
  width: 14px;
  height: 14px;
  border-radius: 50%;
}
.record-icon.idle {
  background: #409eff;
}
.record-icon.pulse {
  background: #f56c6c;
  animation: breathe 0.8s infinite;
}

.record-text {
  letter-spacing: 2px;
}

/* ============================================================
   动画关键帧
   ============================================================ */
@keyframes breathe {
  0%, 100% { opacity: 1; transform: scale(1); }
  50%      { opacity: 0.5; transform: scale(1.25); }
}
@keyframes dotBounce {
  0%, 80%, 100% { transform: translateY(0); }
  40%           { transform: translateY(-8px); }
}
</style>
