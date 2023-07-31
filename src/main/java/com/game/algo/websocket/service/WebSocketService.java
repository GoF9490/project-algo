package com.game.algo.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.entity.Player;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
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

    public void sendGameStatusDataToPlayers(List<Player> playerList, GameStatusData gameStatusData) {
        String json = "";
        try {
            json = objectMapper.writeValueAsString(gameStatusData);
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        MessageDataResponse message = MessageDataResponse.create(MessageType.GameStatusData, json);

        playerList.forEach(player -> {
            try {
                sendMessageData(player.getWebSocketSessionId(), message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void sendMessageData(String recipientSessionId, MessageDataResponse messageDataResponse) throws IOException {
        String json = objectMapper.writeValueAsString(messageDataResponse);
        CLIENTS.get(recipientSessionId).sendMessage(new TextMessage(json));
    }

    public void removeClient(String sessionId){
        CLIENTS.remove(sessionId);
    }
}
