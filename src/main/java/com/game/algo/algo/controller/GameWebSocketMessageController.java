package com.game.algo.algo.controller;

import com.game.algo.algo.dto.*;
import com.game.algo.algo.dto.messagetype.GameRoomCreate;
import com.game.algo.algo.dto.messagetype.GameRoomJoin;
import com.game.algo.algo.dto.messagetype.PlayerCreate;
import com.game.algo.algo.dto.messagetype.PlayerSimple;
import com.game.algo.algo.service.GameService;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataResponse;
import com.game.algo.websocket.service.WebSocketService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class GameWebSocketMessageController {

    private final GameService gameService;
    private final WebSocketService webSocketService;


    public void createPlayer(@NonNull PlayerCreate playerCreate) {
        Long playerId = gameService.createPlayer(playerCreate.getName(), playerCreate.getSessionId());
        PlayerSimple playerSimple = PlayerSimple.create(gameService.findPlayerById(playerId));

        sendMessage(playerCreate.getSessionId(), MessageDataResponse.create(MessageType.PlayerSimple, playerSimple));
    }

    public void createGameRoom(@NonNull GameRoomCreate gameRoomCreate) {
        String sessionId = gameService.findPlayerById(gameRoomCreate.getPlayerId()).getWebSocketSessionId();
        Long gameRoomId = gameService.createGameRoom();

        sendMessage(sessionId, MessageDataResponse.create(MessageType.CreateRoomSuccess, gameRoomId));
    }



    public void joinGameRoom(@NonNull GameRoomJoin gameRoomJoin) {
        String sessionId = gameService.findPlayerById(gameRoomJoin.getPlayerId()).getWebSocketSessionId();
        gameService.joinGameRoom(gameRoomJoin.getGameRoomId(), gameRoomJoin.getPlayerId());

        sendGameStatusData(gameRoomJoin.getGameRoomId());
        sendMessage(sessionId, MessageDataResponse.create(MessageType.JoinRoomSuccess, ""));
    }

    public void updatePlayerReady(@NonNull PlayerReadyUpdate playerReadyUpdate) {
        gameService.updatePlayerReady(playerReadyUpdate.getPlayerId(), playerReadyUpdate.getReady());

        sendGameStatusData(playerReadyUpdate.getGameRoomId());
    }

    public void gameStart(@NonNull GameStart gameStart) {
        gameService.gameStart(gameStart.getGameRoomId());

        sendGameStatusData(gameStart.getGameRoomId());
        sendWaitForSec(gameStart.getGameRoomId(), 5);
    }

    public void endSettingPhase(NextPhase nextPhase) {
        gameService.updatePlayerReady(nextPhase.getPlayerId(), true);
        if (gameService.endSettingPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum())) {
            sendGameStatusData(nextPhase.getGameRoomId());
            sendWaitForSec(nextPhase.getGameRoomId(), 20);
        }
    }

    public void drawBlockAtStart(StartBlockDraw blockDraw) {
        gameService.drawBlockAtStart(blockDraw.getGameRoomId(), blockDraw.getPlayerId(),
                blockDraw.getWhiteBlockCount(), blockDraw.getBlackBlockCount());

        int playerOrderNum = gameService.findPlayerById(blockDraw.getPlayerId()).getOrderNumber();

        endStartPhase(blockDraw.getGameRoomId(), playerOrderNum);
    }

    public void autoDrawAtStart(NextPhase nextPhase) {
        gameService.autoDrawAtStart(nextPhase.getGameRoomId());

        endStartPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
    }

    private void endStartPhase(Long gameRoomId, int playerOrderNum) {
        if (gameService.endStartPhase(gameRoomId, playerOrderNum)) {
            sendWaitForSec(gameRoomId, 30);
        } else {
            sendWaitForSec(gameRoomId, 20);
        }

        sendOwnerBlockData(gameRoomId);
        sendGameStatusData(gameRoomId);
    }

    public void disconnectWebSession(String sessionId){
        // 세션아이디에 따른 플레이어 객체를 삭제, 수정하거나 해서 게임아웃을 시키던지, 재접속의 여지를 남기던지 하면 될듯.
    }

    private void sendGameStatusData(Long gameRoomId) {
        MessageDataResponse messageData = MessageDataResponse.create(MessageType.GameStatusData,
                gameService.getGameStatusData(gameRoomId));

        gameService.getSessionIdListInGameRoom(gameRoomId).forEach(sid -> sendMessage(sid, messageData));
    }

    private void sendOwnerBlockData(Long gameRoomId) {
        List<OwnerBlockData> ownerBlockDataList = gameService.getOwnerBlockDataList(gameRoomId);

        ownerBlockDataList.forEach(ownerBlockData -> sendMessage(ownerBlockData.getSessionId(),
                MessageDataResponse.create(MessageType.OwnerBlockData, ownerBlockData)));
    }

    private void sendWaitForSec(Long gameRoomId, int timeInSec) {
        MessageDataResponse messageData = MessageDataResponse.create(MessageType.WaitForSec, timeInSec);
        gameService.getSessionIdListInGameRoom(gameRoomId).forEach(sid -> sendMessage(sid, messageData));
    }

    private void sendMessage(String sessionId, MessageDataResponse messageData) {
        try {
            webSocketService.sendMessage(sessionId, messageData);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
