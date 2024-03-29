package com.game.algo.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.controller.GameWebSocketMessageController;
import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.dto.request.*;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataRequest;
import com.game.algo.websocket.dto.MessageDataResponse;
import com.game.algo.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends BinaryWebSocketHandler {

    private final GameWebSocketMessageController gameMessageController;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();

        System.out.println("connect : " + sessionId);
        webSocketService.addClient(sessionId, session);
        sendSessionId(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            gameMessageController.disconnectWebSession(session.getId());
            webSocketService.removeClient(session.getId());
        } catch (GameLogicException ignored) {
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();

        String input = new String(message.getPayload().array(), StandardCharsets.UTF_8);

        MessageDataRequest messageDataRequest = objectMapper.readValue(input, MessageDataRequest.class);

        MessageType type = messageDataRequest.getType();
        String requestMessage = messageDataRequest.getMessage();

        log.info("MessageData : sessionId:{} / type:{} / message:{}", sessionId, type, requestMessage);


        // 메서드로 뺄 가능성 있음
        try {
            switch (type) {
                case SessionId:
                    sendSessionId(sessionId);
                    break;

                case NextPhase:
                    NextPhase nextPhase = objectMapper.readValue(requestMessage, NextPhase.class);

                    nextPhase(nextPhase);
                    break;

                case PlayerCreate:
                    PlayerCreate playerCreate = objectMapper.readValue(requestMessage, PlayerCreate.class);

                    gameMessageController.createPlayer(playerCreate);
                    break;

                case SetSessionId:
                    Long playerId = Long.parseLong(requestMessage);

                    gameMessageController.setSessionIdForPlayer(playerId, sessionId);
                    break;

                case GameRoomCreate:
                    GameRoomCreate gameRoomCreate = objectMapper.readValue(requestMessage, GameRoomCreate.class);

                    gameMessageController.createGameRoom(gameRoomCreate);
                    break;

                case GameRoomJoin:
                    GameRoomJoin gameRoomJoin = objectMapper.readValue(requestMessage, GameRoomJoin.class);

                    gameMessageController.joinGameRoom(gameRoomJoin);
                    break;

                case GameRoomFind:
                    Integer page = Integer.parseInt(requestMessage);

                    gameMessageController.findGameRoom(sessionId, page);
                    break;

                case GameRoomExit:
                    gameMessageController.exitGameRoom(sessionId);
                    break;

                case PlayerReadyUpdate:
                    PlayerReadyUpdate playerReadyUpdate = objectMapper.readValue(requestMessage, PlayerReadyUpdate.class);

                    gameMessageController.updatePlayerReady(playerReadyUpdate);
                    break;

                case GameStart:
                    GameStart gameStart = objectMapper.readValue(requestMessage, GameStart.class);

                    gameMessageController.gameStart(gameStart);
                    break;

                case StartBlockDraw:
                    StartBlockDraw startBlockDraw = objectMapper.readValue(requestMessage, StartBlockDraw.class);

                    gameMessageController.drawBlockAtStart(startBlockDraw);
                    break;

                case BlockDraw:
                    BlockDraw blockDraw = objectMapper.readValue(requestMessage, BlockDraw.class);

                    gameMessageController.drawBlockAtDrawPhase(blockDraw);
                    break;

                case JokerUpdate:
                    JokerUpdate jokerUpdate = objectMapper.readValue(requestMessage, JokerUpdate.class);

                    gameMessageController.updateJoker(jokerUpdate);
                    break;

                case BlockGuess:
                    BlockGuess blockGuess = objectMapper.readValue(requestMessage, BlockGuess.class);

                    gameMessageController.guessBlock(blockGuess);
                    break;

                case GuessRepeat:
                    GuessRepeat guessRepeat = objectMapper.readValue(requestMessage, GuessRepeat.class);

                    gameMessageController.choiceRepeatGuess(guessRepeat);
                    break;
            }
        } catch (GameLogicException gameLogicException) {
            log.error("game logic exception : " + gameLogicException.getMessage());
            webSocketService.sendMessage(sessionId,
                    MessageDataResponse.create(MessageType.Exception, gameLogicException.getMessage()));

        } catch (CannotAcquireLockException e) {
            log.info("CannotAcquireLockException by session id : " + sessionId);

        } catch (Exception e) {
            log.error(e.getMessage());
            webSocketService.sendMessage(sessionId, MessageDataResponse.create(MessageType.Exception, e.getMessage()));
        }

    }

    private void sendGameVersion(String sessionId) throws IOException {
        MessageDataResponse messageDataResponse = new MessageDataResponse(MessageType.Version, GameProperty.VERSION);
        webSocketService.sendMessage(sessionId, messageDataResponse);
    }

    private void sendSessionId(String sessionId) throws Exception {
        MessageDataResponse messageDataResponse = new MessageDataResponse(MessageType.SessionId, sessionId);
        webSocketService.sendMessage(sessionId, messageDataResponse);
    }

    private void nextPhase(NextPhase nextPhase) {
        switch (nextPhase.getPhase()) {
            case SETTING:
                gameMessageController.endSettingPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
                break;

            case START:
                gameMessageController.endStartPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
                break;

            case DRAW:
                gameMessageController.endDrawPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
                break;

            case SORT:
                gameMessageController.endSortPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
                break;

            case GUESS:
                gameMessageController.endGuessPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
                break;

            case REPEAT:
                gameMessageController.endRepeatPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum(), false);
                break;

            case END:
                gameMessageController.endEndPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
                break;

            case GAMEOVER:
                gameMessageController.endGameOverPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
                break;
        }
    }
}
