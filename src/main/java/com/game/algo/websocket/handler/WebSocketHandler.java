package com.game.algo.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.controller.GameWebSocketMessageController;
import com.game.algo.algo.dto.GameStart;
import com.game.algo.algo.dto.NextPhase;
import com.game.algo.algo.dto.PlayerBlockDraw;
import com.game.algo.algo.dto.PlayerReadyUpdate;
import com.game.algo.algo.dto.messagetype.GameRoomCreate;
import com.game.algo.algo.dto.messagetype.GameRoomJoin;
import com.game.algo.algo.dto.messagetype.PlayerCreate;
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

    private final GameWebSocketMessageController gameMessageController;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        webSocketService.addClient(sessionId, session);

        MessageDataResponse messageDataResponse = new MessageDataResponse(MessageType.SessionId, sessionId);
        webSocketService.sendMessage(sessionId, messageDataResponse);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        gameMessageController.disconnectWebSession(session.getId());
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

                    gameMessageController.createPlayer(playerCreate);
                    break;

                case GameRoomCreate:
                    GameRoomCreate gameRoomCreate = objectMapper.readValue(requestMessage, GameRoomCreate.class);

                    gameMessageController.createGameRoom(gameRoomCreate);
                    break;

                case GameRoomJoin:
                    GameRoomJoin gameRoomJoin = objectMapper.readValue(requestMessage, GameRoomJoin.class);

                    gameMessageController.joinGameRoom(gameRoomJoin);
                    break;

                case PlayerReadyUpdate:
                    PlayerReadyUpdate playerReadyUpdate = objectMapper.readValue(requestMessage, PlayerReadyUpdate.class);

                    gameMessageController.updatePlayerReady(playerReadyUpdate);
                    break;

                case GameStart:
                    GameStart gameStart = objectMapper.readValue(requestMessage, GameStart.class);

                    gameMessageController.gameStart(gameStart);
                    break;

                case PlayerBlockDraw:
                    PlayerBlockDraw playerBlockDraw = objectMapper.readValue(requestMessage, PlayerBlockDraw.class);

                    gameMessageController.drawBlock(playerBlockDraw);
                    break;

                case NextPhase:
                    NextPhase nextPhase = objectMapper.readValue(requestMessage, NextPhase.class);

                    nextPhase(nextPhase);
                    break;
            }
        } catch (GameLogicException gameLogicException) {
            log.error("game logic exception : " + gameLogicException.getMessage());
            webSocketService.sendMessage(sessionId,
                    MessageDataResponse.create(MessageType.Exception, gameLogicException.getMessage()));
        } catch (Exception e) {
            log.error(e.getMessage());
            webSocketService.sendMessage(sessionId, MessageDataResponse.create(MessageType.Exception, e.getMessage()));
        }

    }

    private void nextPhase(NextPhase nextPhase) {
        switch (nextPhase.getPhase()) {
            case SETTING:
                gameMessageController.endSettingPhase(nextPhase);
                break;

            case START:
                break;

            case CONTROL:
                break;

            case DRAW:
                break;

            case SORT:
                break;

            case GUESS:
                break;

            case REPEAT:
                break;

            case END:
                break;
        }
    }
}
