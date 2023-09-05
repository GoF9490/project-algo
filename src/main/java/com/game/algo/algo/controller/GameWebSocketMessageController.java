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
        sendWaitForSec(gameStart.getGameRoomId());
    }

    public void endSettingPhase(NextPhase nextPhase) {
        gameService.updatePlayerReady(nextPhase.getPlayerId(), true);
        gameService.endSettingPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
        sendGameStatusData(nextPhase.getGameRoomId());
        sendWaitForSec(nextPhase.getGameRoomId());
    }

    public void drawBlockAtStart(StartBlockDraw startBlockDraw) {
        gameService.drawBlockAtStart(startBlockDraw.getGameRoomId(), startBlockDraw.getPlayerId(),
                startBlockDraw.getWhiteBlockCount(), startBlockDraw.getBlackBlockCount());

        int playerOrderNum = gameService.findPlayerById(startBlockDraw.getPlayerId()).getOrderNumber();

        endStartPhase(startBlockDraw.getGameRoomId(), playerOrderNum);
    }

    public void autoDrawAtStart(NextPhase nextPhase) {
        gameService.autoDrawAtStart(nextPhase.getGameRoomId());

        endStartPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
    }

    public void drawBlockAtDrawPhase(BlockDraw blockDraw) {
        gameService.drawBlockAtDrawPhase(blockDraw.getGameRoomId(), blockDraw.getPlayerId(), blockDraw.getBlockColor());

        int playerOrderNum = gameService.findPlayerById(blockDraw.getPlayerId()).getOrderNumber();

        endDrawPhase(blockDraw.getGameRoomId(), playerOrderNum);
    }
    
    public void autoDrawAtDrawPhase(NextPhase nextPhase) {
        gameService.autoDrawAtDrawPhase(nextPhase.getGameRoomId());
        endDrawPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
    }

    public void updateJoker(JokerUpdate jokerUpdate) {
        gameService.updatePlayerJoker(jokerUpdate.getPlayerId(), jokerUpdate.getIndex(), jokerUpdate.getBlockColor());
    }
    
    public void endSortPhase(NextPhase nextPhase) {
        gameService.endSortPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
        
        sendGameStatusData(nextPhase.getGameRoomId());
        sendWaitForSec(nextPhase.getGameRoomId());
    }

    public void disconnectWebSession(String sessionId){
        // 세션아이디에 따른 플레이어 객체를 삭제, 수정하거나 해서 게임아웃을 시키던지, 재접속의 여지를 남기던지 하면 될듯.
    }

    /** end 시리즈 */

    private void endStartPhase(Long gameRoomId, int playerOrderNum) {
        gameService.endStartPhase(gameRoomId, playerOrderNum);
        sendOwnerBlockData(gameRoomId);
        sendGameStatusData(gameRoomId);
        sendWaitForSec(gameRoomId);
    }

    private void endDrawPhase(Long gameRoomId, int progressPlayerNum) {
        gameService.endDrawPhase(gameRoomId, progressPlayerNum);
        sendOwnerBlockData(gameRoomId);
        sendGameStatusData(gameRoomId);
        sendWaitForSec(gameRoomId);
        sendDrawBlockData(gameRoomId);
    }

    /** send 시리즈 (JPA 쿼리 수정 또는 DB변경을 통해 파라미터가 GameRoom 오브젝트로 수정, 쿼리횟수 줄이는 효과 기대가능) */

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

    private void sendWaitForSec(Long gameRoomId) {
        MessageDataResponse messageData = MessageDataResponse.create(MessageType.WaitForSec,
                gameService.findGameRoomById(gameRoomId).getPhase().getWaitTime());

        gameService.getSessionIdListInGameRoom(gameRoomId).forEach(sid -> sendMessage(sid, messageData));
    }

    private void sendDrawBlockData(Long gameRoomId) {
        DrawBlockData drawBlockData = gameService.getDrawBlockData(gameRoomId);
        MessageDataResponse messageData = MessageDataResponse.create(MessageType.DrawBlockData, drawBlockData);

        sendMessage(drawBlockData.getSessionId(), messageData);
    }

    private void sendMessage(String sessionId, MessageDataResponse messageData) {
        try {
            webSocketService.sendMessage(sessionId, messageData);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
