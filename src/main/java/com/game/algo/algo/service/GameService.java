package com.game.algo.algo.service;

import com.game.algo.algo.dto.MultipleMessageSupporter;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;

import java.util.List;

public interface GameService {

    Long createPlayer(String name, String webSocketSessionId);

    Player findPlayerById(Long id);

    Long createGameRoom();

    GameRoom findGameRoomById(Long id);

    void updatePlayerReady(Long playerId, boolean isReady);

    void joinGameRoom(Long gameRoomId, Long playerId);

    void gameStart(Long gameRoomId, Long playerId);

    MultipleMessageSupporter getGameStatusMessageSupporter(Long gameRoomId);

    void drawBlockAtStart(Long gameRoomId, Long playerId, int whiteBlockCount, int BlackBlockCount);
}
