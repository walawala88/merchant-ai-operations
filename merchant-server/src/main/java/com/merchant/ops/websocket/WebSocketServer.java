package com.merchant.ops.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSION_MAP.put(sid, session);
        log.info("WebSocket 已连接: sid={}", sid);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.debug("收到 WebSocket 消息: sid={}, message={}", sid, message);
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("WebSocket 已断开: sid={}", sid);
    }

    public void sendToAllClient(String message) {
        Collection<Session> sessions = SESSION_MAP.values();
        for (Session session : sessions) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (Exception ex) {
                log.warn("WebSocket 消息发送失败: sessionId={}", session.getId(), ex);
            }
        }
    }
}