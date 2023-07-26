package com.game.algo.websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.controller.GameWebSocketHandler;
import com.game.algo.algo.dto.messagetype.GameRoomCreate;
import com.game.algo.algo.dto.messagetype.GameRoomJoin;
import com.game.algo.algo.dto.messagetype.PlayerCreate;
import com.game.algo.algo.dto.messagetype.PlayerSimple;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataRequest;
import com.game.algo.websocket.dto.MessageDataResponse;
import com.game.algo.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

    private final GameWebSocketHandler gameWebSocketHandler;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        webSocketService.addClient(sessionId, session);

        MessageDataResponse messageDataResponse = new MessageDataResponse(MessageType.SessionId, sessionId);
        webSocketService.sendMessageData(sessionId, messageDataResponse);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketService.removeClient(session.getId());
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
        try {
            switch (type) {
                case PlayerCreate:
                    PlayerCreate playerCreate = objectMapper.readValue(requestMessage, PlayerCreate.class);

                    PlayerSimple playerSimple = gameWebSocketHandler.createPlayer(playerCreate);
                    webSocketService.sendMessageData(sessionId,
                            MessageDataResponse.create(MessageType.PlayerSimple, toJson(playerSimple)));
                    break;

                case GameRoomCreate:
                    GameRoomCreate gameRoomCreate = objectMapper.readValue(requestMessage, GameRoomCreate.class);

                    Long gameRoomId = gameWebSocketHandler.createGameRoom(gameRoomCreate);
                    webSocketService.sendMessageData(sessionId,
                            MessageDataResponse.create(MessageType.CreateRoomSuccess, gameRoomId.toString()));
                    break;

                case GameRoomJoin:
                    GameRoomJoin gameRoomJoin = objectMapper.readValue(requestMessage, GameRoomJoin.class);

                    MessageType messageType = gameWebSocketHandler.joinGameRoom(gameRoomJoin) ?
                            MessageType.JoinRoomSuccess : MessageType.JoinRoomFail;
                    webSocketService.sendMessageData(sessionId,
                            MessageDataResponse.create(messageType, ""));
                    break;


            }
        } catch (GameLogicException gameLogicException) {
            webSocketService.sendMessageData(sessionId,
                    MessageDataResponse.create(MessageType.Exception, gameLogicException.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());
        }

    }

    private String toJson(PlayerSimple playerSimple) throws JsonProcessingException {
        return objectMapper.writeValueAsString(playerSimple);
    }
}
