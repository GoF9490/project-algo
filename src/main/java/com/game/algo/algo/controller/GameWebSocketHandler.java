package com.game.algo.algo.controller;

import com.game.algo.algo.dto.messagetype.GameRoomCreate;
import com.game.algo.algo.dto.messagetype.GameRoomJoin;
import com.game.algo.algo.dto.messagetype.PlayerCreate;
import com.game.algo.algo.dto.messagetype.PlayerSimple;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GameWebSocketHandler { // 사용여부 검토 필요

    private final GameService gameService;
//    private final WebSocketService webSocketService;

    public PlayerSimple createPlayer(PlayerCreate playerCreate) {
        Long playerId = gameService.createPlayer(playerCreate.getName(), playerCreate.getSessionId());
        return PlayerSimple.create(gameService.findPlayerById(playerId));
    }

    public Long createGameRoom(GameRoomCreate gameRoomCreate) {
        Player findPlayer = gameService.findPlayerById(gameRoomCreate.getPlayerId());
        return gameService.createGameRoom();
    }

    @Transactional
    public boolean joinGameRoom(GameRoomJoin gameRoomJoin) {
        Player findPlayer = gameService.findPlayerById(gameRoomJoin.getPlayerId());
        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomJoin.getGameRoomId());
        return gameService.joinGameRoom(findGameRoom, findPlayer);
    }
}
