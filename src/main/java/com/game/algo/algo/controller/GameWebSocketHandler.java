package com.game.algo.algo.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.dto.GameStart;
import com.game.algo.algo.dto.MultipleMessageSupporter;
import com.game.algo.algo.dto.PlayerBlockDraw;
import com.game.algo.algo.dto.PlayerReadyUpdate;
import com.game.algo.algo.dto.messagetype.GameRoomCreate;
import com.game.algo.algo.dto.messagetype.GameRoomJoin;
import com.game.algo.algo.dto.messagetype.PlayerCreate;
import com.game.algo.algo.dto.messagetype.PlayerSimple;
import com.game.algo.algo.service.GameService;
import com.game.algo.websocket.annotation.ResponseMessageData;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataResponse;
import com.game.algo.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameWebSocketHandler { // 사용여부 검토 필요

    private final GameService gameService;
    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;

    @ResponseMessageData
    public MessageDataResponse createPlayer(PlayerCreate playerCreate) {
        Long playerId = gameService.createPlayer(playerCreate.getName(), playerCreate.getSessionId());
        PlayerSimple playerSimple = PlayerSimple.create(gameService.findPlayerById(playerId));
        return MessageDataResponse.create(MessageType.PlayerSimple, playerSimple);
    }

    @ResponseMessageData
    public MessageDataResponse createGameRoom(GameRoomCreate gameRoomCreate) {
        gameService.findPlayerById(gameRoomCreate.getPlayerId());
        Long gameRoomId = gameService.createGameRoom();
        return MessageDataResponse.create(MessageType.CreateRoomSuccess, gameRoomId);
    }


    @ResponseMessageData
    public MessageDataResponse joinGameRoom(GameRoomJoin gameRoomJoin) {
        gameService.joinGameRoom(gameRoomJoin.getGameRoomId(), gameRoomJoin.getPlayerId());
        sendMessageByMultipleMessageSupporter(gameService.getGameStatusMessageSupporter(gameRoomJoin.getGameRoomId()));
        return MessageDataResponse.create(MessageType.JoinRoomSuccess, "");
    }

    public void updatePlayerReady(PlayerReadyUpdate playerReadyUpdate) {
        gameService.updatePlayerReady(playerReadyUpdate.getPlayerId(), playerReadyUpdate.getReady());
        sendMessageByMultipleMessageSupporter(gameService.getGameStatusMessageSupporter(playerReadyUpdate.getGameRoomId()));
//        gameService.sendGameStatusByWebSocket(playerReadyUpdate.getGameRoomId());
    }

    public void gameStart(GameStart gameStart) {
        gameService.gameStart(gameStart.getGameRoomId(), gameStart.getPlayerId());
        sendMessageByMultipleMessageSupporter(gameService.getGameStatusMessageSupporter(gameStart.getGameRoomId()));
//        gameService.sendGameStatusByWebSocket(gameStart.getGameRoomId());
    }

    public void drawBlock(PlayerBlockDraw playerBlockDraw) {
        gameService.drawBlockAtStart(playerBlockDraw.getGameRoomId(), playerBlockDraw.getPlayerId(),
                playerBlockDraw.getWhiteBlockCount(), playerBlockDraw.getBlackBlockCount());

        gameService.updatePlayerReady(playerBlockDraw.getPlayerId(), true);
    }

    public void disconnectWebSession(String sessionId){
        // 세션아이디에 따른 플레이어 객체를 삭제, 수정하거나 해서 게임아웃을 시키던지, 재접속의 여지를 남기던지 하면 될듯.
    }

    private void sendMessageByMultipleMessageSupporter(MultipleMessageSupporter messageSupporter) {
        try {
            webSocketService.sendMessageToSessionIdList(messageSupporter.getSessionIdList(),
                    convertMessageDataResponse(messageSupporter));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private MessageDataResponse convertMessageDataResponse(MultipleMessageSupporter messageSupporter) throws JsonProcessingException {
        return MessageDataResponse.create(messageSupporter.getMessageType(),
                objectMapper.writeValueAsString(messageSupporter.getData()));
    }
}
