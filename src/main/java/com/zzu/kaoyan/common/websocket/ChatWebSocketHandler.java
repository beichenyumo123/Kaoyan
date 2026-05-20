package com.zzu.kaoyan.common.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zzu.kaoyan.common.entity.User;
import com.zzu.kaoyan.common.util.SensitiveWordUtil;
import com.zzu.kaoyan.mapper.UserMapper;
import com.zzu.kaoyan.module.chat.dto.ChatMessageDTO;
import com.zzu.kaoyan.module.chat.entity.GroupMessage;
import com.zzu.kaoyan.module.chat.mapper.GroupMemberMapper;
import com.zzu.kaoyan.module.chat.mapper.GroupMessageMapper;
import com.zzu.kaoyan.module.chat.service.ChatGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    private static final ConcurrentHashMap<Long, Set<WebSocketSession>> GROUP_SESSIONS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final GroupMessageMapper groupMessageMapper;
    private final GroupMemberMapper groupMemberMapper;
    private final UserMapper userMapper;
    private final ChatGroupService chatGroupService;

    public ChatWebSocketHandler(GroupMessageMapper groupMessageMapper,
                                GroupMemberMapper groupMemberMapper,
                                UserMapper userMapper,
                                ChatGroupService chatGroupService) {
        this.groupMessageMapper = groupMessageMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.userMapper = userMapper;
        this.chatGroupService = chatGroupService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long groupId = getGroupId(session);
        Long userId = getUserId(session);
        if (groupId == null || userId == null) {
            closeQuietly(session);
            return;
        }

        if (!chatGroupService.isMember(groupId, userId)) {
            closeQuietly(session);
            return;
        }

        GROUP_SESSIONS.computeIfAbsent(groupId, k -> ConcurrentHashMap.newKeySet()).add(session);

        User user = userMapper.selectById(userId);
        String username = user != null ? user.getUsername() : "未知用户";

        broadcastSystemMessage(groupId, username + " 加入了群聊", null);
        log.info("WebSocket 连接 — userId={}, groupId={}", userId, groupId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long groupId = getGroupId(session);
        Long userId = getUserId(session);
        if (groupId == null || userId == null) return;

        Set<WebSocketSession> sessions = GROUP_SESSIONS.get(groupId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                GROUP_SESSIONS.remove(groupId);
            }
        }

        User user = userMapper.selectById(userId);
        String username = user != null ? user.getUsername() : "未知用户";
        broadcastSystemMessage(groupId, username + " 离开了群聊", null);
        log.info("WebSocket 断开 — userId={}, groupId={}", userId, groupId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long groupId = getGroupId(session);
        Long userId = getUserId(session);
        if (groupId == null || userId == null) return;

        ChatMessageDTO dto;
        try {
            dto = objectMapper.readValue(message.getPayload(), ChatMessageDTO.class);
        } catch (Exception e) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"data\":{\"content\":\"消息格式错误\"}}"));
            return;
        }

        if (dto.getContent() == null || dto.getContent().isBlank()) return;

        SensitiveWordUtil.FilterResult filterResult = SensitiveWordUtil.filter(dto.getContent());
        if (filterResult.isHasSensitive()) {
            log.warn("群聊消息包含敏感词 — userId={}, matched={}", userId, filterResult.getMatched());
        }
        String cleanedContent = filterResult.getFilteredText();

        User user = userMapper.selectById(userId);

        GroupMessage msg = new GroupMessage();
        msg.setGroupId(groupId);
        msg.setUserId(userId);
        msg.setContent(cleanedContent);
        msg.setIsDeleted(0);
        msg.setCreatedAt(LocalDateTime.now());
        msg.setUpdatedAt(LocalDateTime.now());

        CompletableFuture.runAsync(() -> {
            try {
                groupMessageMapper.insert(msg);
            } catch (Exception e) {
                log.error("群聊消息入库失败 — userId={}, groupId={}", userId, groupId, e);
            }
        });

        broadcastMessage(groupId, msg, user, session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket 传输异常", exception);
        closeQuietly(session);
    }

    // ==================== 广播逻辑 ====================

    private void broadcastMessage(Long groupId, GroupMessage msg, User sender, WebSocketSession excludeSession) {
        String payload = buildMessageJson("message", Map.of(
                "id", msg.getId() != null ? msg.getId().toString() : "",
                "groupId", groupId.toString(),
                "userId", sender != null ? sender.getId().toString() : "",
                "username", sender != null ? sender.getUsername() : "未知",
                "avatarUrl", sender != null && sender.getAvatarUrl() != null ? sender.getAvatarUrl() : "",
                "content", msg.getContent(),
                "createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : ""
        ));
        broadcast(groupId, payload, excludeSession);
    }

    private void broadcastSystemMessage(Long groupId, String content, WebSocketSession excludeSession) {
        Set<WebSocketSession> sessions = GROUP_SESSIONS.get(groupId);
        int onlineCount = sessions != null ? sessions.size() : 0;
        String payload = buildMessageJson("system", Map.of(
                "content", content,
                "onlineCount", String.valueOf(onlineCount)
        ));
        broadcast(groupId, payload, excludeSession);
    }

    private void broadcast(Long groupId, String payload, WebSocketSession excludeSession) {
        Set<WebSocketSession> sessions = GROUP_SESSIONS.get(groupId);
        if (sessions == null) return;
        for (WebSocketSession s : sessions) {
            if (excludeSession != null && s.getId().equals(excludeSession.getId())) continue;
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(payload));
                } catch (IOException e) {
                    log.error("WebSocket 消息发送失败", e);
                }
            }
        }
    }

    // ==================== 工具方法 ====================

    private String buildMessageJson(String type, Map<String, String> data) {
        StringBuilder sb = new StringBuilder("{\"type\":\"").append(type).append("\",\"data\":{");
        boolean first = true;
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":\"").append(escapeJson(e.getValue())).append("\"");
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Long getGroupId(WebSocketSession session) {
        String path = session.getUri() != null ? session.getUri().getPath() : "";
        String[] parts = path.split("/");
        if (parts.length >= 4) {
            try {
                return Long.parseLong(parts[parts.length - 1]);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private Long getUserId(WebSocketSession session) {
        Object attr = session.getAttributes().get("userId");
        if (attr instanceof Long) return (Long) attr;
        return null;
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            session.close();
        } catch (IOException ignored) {
        }
    }
}
