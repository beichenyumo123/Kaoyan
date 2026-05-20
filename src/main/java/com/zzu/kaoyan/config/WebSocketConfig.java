package com.zzu.kaoyan.config;

import cn.dev33.satoken.stp.StpUtil;
import com.zzu.kaoyan.common.websocket.ChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler) {
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat/{groupId}")
                .addInterceptors(new SaTokenHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

    /**
     * WebSocket 握手拦截器：校验 Sa-Token 并检查群成员身份。
     */
    private static class SaTokenHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Map<String, Object> attributes) {
            String token = extractToken(request);
            if (token == null) return false;

            try {
                Object loginId = StpUtil.getLoginIdByToken(token);
                if (loginId == null) return false;
                attributes.put("userId", Long.parseLong(loginId.toString()));
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Exception exception) {
        }

        private String extractToken(ServerHttpRequest request) {
            String query = request.getURI().getQuery();
            if (query == null) return null;
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length == 2 && "satoken".equals(pair[0])) {
                    return pair[1];
                }
            }
            return null;
        }
    }
}
