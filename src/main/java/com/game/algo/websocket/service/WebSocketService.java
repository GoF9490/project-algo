package com.game.algo.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.websocket.dto.MessageDataResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebSocketService {

    private static final Map<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public void addClient(String sessionId, WebSocketSession session){
        CLIENTS.put(sessionId, session);
    }

    public void sendMessage(@NonNull String sessionId, @NonNull MessageDataResponse messageData) throws IOException {
        MessageDataResponse convertMessageData = MessageDataResponse.create(
                messageData.getType(),
                objectMapper.writeValueAsString(messageData.getMessage()));

        String json = objectMapper.writeValueAsString(convertMessageData);
        CLIENTS.get(sessionId).sendMessage(new TextMessage(json));
    }

    public void removeClient(String sessionId){
        CLIENTS.remove(sessionId);
    }
}
