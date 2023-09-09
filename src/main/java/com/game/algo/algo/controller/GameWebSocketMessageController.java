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

    public void drawBlockAtStart(StartBlockDraw startBlockDraw) {
        gameService.drawBlockAtStart(startBlockDraw.getGameRoomId(), startBlockDraw.getPlayerId(),
                startBlockDraw.getWhiteBlockCount(), startBlockDraw.getBlackBlockCount());

        int playerOrderNum = gameService.findPlayerById(startBlockDraw.getPlayerId()).getOrderNumber();

        endStartPhase(startBlockDraw.getGameRoomId(), playerOrderNum);
    }

    public void drawBlockAtDrawPhase(BlockDraw blockDraw) {
        gameService.drawBlockAtDrawPhase(blockDraw.getGameRoomId(), blockDraw.getPlayerId(), blockDraw.getBlockColor());

        int playerOrderNum = gameService.findPlayerById(blockDraw.getPlayerId()).getOrderNumber();

        endDrawPhase(blockDraw.getGameRoomId(), playerOrderNum);
    }

    public void updateJoker(JokerUpdate jokerUpdate) {
        gameService.updatePlayerJoker(jokerUpdate.getPlayerId(), jokerUpdate.getIndex(), jokerUpdate.getBlockColor());
    }

    public void guessBlock(BlockGuess blockGuess) {
        gameService.guessBlock(blockGuess.getPlayerId(), blockGuess.getTargetPlayerId(),
                blockGuess.getBlockIndex(), blockGuess.getBlockNum());

        int playerOrderNum = gameService.findPlayerById(blockGuess.getPlayerId()).getOrderNumber();

        endGuessPhase(blockGuess.getGameRoomId(), playerOrderNum);
    }

    public void disconnectWebSession(String sessionId){
        // 세션아이디에 따른 플레이어 객체를 삭제, 수정하거나 해서 게임아웃을 시키던지, 재접속의 여지를 남기던지 하면 될듯.
    }

    /** end 시리즈 */

    public void endSettingPhase(Long gameRoomId, int progressPlayerNum) {
//        gameService.updatePlayerReady(gameRoomId, true);
        gameService.endSettingPhase(gameRoomId, progressPlayerNum);

        sendGameStatusData(gameRoomId);
        sendWaitForSec(gameRoomId);
    }

    public void endStartPhase(Long gameRoomId, int progressPlayerNum) {
        gameService.autoDrawAtStart(gameRoomId);
        gameService.endStartPhase(gameRoomId, progressPlayerNum);

        sendOwnerBlockData(gameRoomId);
        sendGameStatusData(gameRoomId);
        sendWaitForSec(gameRoomId);
    }

    public void endDrawPhase(Long gameRoomId, int progressPlayerNum) {
        gameService.autoDrawAtDrawPhase(gameRoomId);
        gameService.endDrawPhase(gameRoomId, progressPlayerNum);

        sendOwnerBlockData(gameRoomId);
        sendGameStatusData(gameRoomId);
        sendWaitForSec(gameRoomId);
        sendDrawBlockData(gameRoomId);
    }

    public void endSortPhase(Long gameRoomId, int progressPlayerNum) {
        gameService.endSortPhase(gameRoomId, progressPlayerNum);

        sendOwnerBlockData(gameRoomId);
        sendGameStatusData(gameRoomId);
        sendWaitForSec(gameRoomId);
    }

    public void endGuessPhase(Long gameRoomId, int progressPlayerNum) {
        gameService.endGuessPhase(gameRoomId, progressPlayerNum);

        sendGameStatusData(gameRoomId);
        sendWaitForSec(gameRoomId);
    }

    /** send 시리즈 (JPA 쿼리 수정 또는 DB변경을 통해 파라미터가 GameRoom 오브젝트로 수정, 쿼리횟수 줄이는 효과 기대가능) */
    // 이거 시작해볼까?
    // 유니티 클라이언트 테스트 계속해서 하면서 버그찾기

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
