package com.game.algo.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.controller.GameWebSocketHandler;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataRequest;
import com.game.algo.websocket.dto.MessageDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<>();
    private final GameWebSocketHandler gameWebSocketHandler;
    private final ObjectMapper objectMapper;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        CLIENTS.put(sessionId, session);

        MessageDataResponse messageDataResponse = new MessageDataResponse(MessageType.SessionId, sessionId);
        sendMessageData(sessionId, messageDataResponse);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        CLIENTS.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();

        String input = message.getPayload();

        MessageDataRequest messageDataRequest = objectMapper.readValue(input, MessageDataRequest.class);

        MessageType type = messageDataRequest.getType();
        String requestMessage = messageDataRequest.getMessage();

        log.info("MessageData : sessionId:{} / type:{} / message:{}", sessionId, type, requestMessage);


        // 메서드로 뺄 가능성 있음
        MessageDataResponse messageDataResponse = null;

        try {
            switch (type) {
//                case PlayerCreate:
//                    PlayerCreate.Request playerCreateRequest = objectMapper.readValue(requestMessage, PlayerCreate.Request.class);
//                    gameWebSocketHandler.createPlayer(playerCreateRequest, sessionId);
//                    break;

                case SessionId:
                    messageDataResponse = new MessageDataResponse(MessageType.SessionId, sessionId);
                    sendMessageData(sessionId, messageDataResponse);
                    break;
            }
        } catch (GameLogicException gameLogicException) {
            messageDataResponse = new MessageDataResponse(MessageType.Exception, gameLogicException.getMessage());
            sendMessageData(sessionId, messageDataResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private void sendMessageData(String sessionId, MessageDataResponse messageDataResponse) throws IOException {
        String json = objectMapper.writeValueAsString(messageDataResponse);
        CLIENTS.get(sessionId).sendMessage(new TextMessage(json));
    }
}
