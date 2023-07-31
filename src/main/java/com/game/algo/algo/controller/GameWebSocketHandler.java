package com.game.algo.algo.controller;

import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.dto.messagetype.GameRoomCreate;
import com.game.algo.algo.dto.messagetype.GameRoomJoin;
import com.game.algo.algo.dto.messagetype.PlayerCreate;
import com.game.algo.algo.dto.messagetype.PlayerSimple;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.service.GameService;
import com.game.algo.websocket.annotation.ResponseMessageData;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataResponse;
import com.game.algo.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameWebSocketHandler { // 사용여부 검토 필요

    private final GameService gameService;

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
        gameService.sendGameStatusByWebSocket(gameRoomJoin.getGameRoomId());
        return MessageDataResponse.create(MessageType.JoinRoomSuccess, "");
    }
}
