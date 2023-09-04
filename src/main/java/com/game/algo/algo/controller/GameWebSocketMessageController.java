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
        /**
         * GameStatus를 넘기면 Joker의 위치가 정의되기 전의 위치(ex 맨뒤)로 노출되어서 조커의 여부를 판단 가능하게 됨.
         * GameStatus를 넘기지 않고 다음 페이즈로 넘어가게끔 할 필요성이 있음.
         * 방법
         *  1. 클라이언트의 페이즈를 넘가는 신호를준다.
         *  2. 해당 페이즈에서 조커를 일괄 처리한다.
         *  3. GameStatus에서 페이즈를 분리해 따로 관리한다. (다른 기존의 메서드도 수정 필요)
         * 추가적으로 방금 뽑은 카드는 따로 나타낼 수 있도록 표기해야함.
         * 이것도 방법 생각해야할듯
         *  1. GameRoom 에서 관리한다. (어짜피 방금뽑은 카드는 무조건 하나니까. 대신 직관적이지 않을 수 있음) <- 이게 나을듯
         *  2. Player 가 소유한 Block 객체에서 관리한다. (DB에서 가져올 때 리소스가 더 많이들듯 하지만 직관적)
         */
    }
    
    public void autoDrawAtDrawPhase(NextPhase nextPhase) {
        gameService.autoDrawAtDrawPhase(nextPhase.getGameRoomId());
        endDrawPhase(nextPhase.getGameRoomId(), nextPhase.getProgressPlayerNum());
    }

    public void updateJoker(JokerUpdate jokerUpdate) {
        gameService.updatePlayerJoker(jokerUpdate.getPlayerId(), jokerUpdate.getIndex(), jokerUpdate.getBlockColor());
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

    /** send 시리즈 */

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
