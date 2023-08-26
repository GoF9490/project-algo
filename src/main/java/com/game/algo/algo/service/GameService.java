package com.game.algo.algo.service;

import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.dto.OwnerBlockData;
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

    void gameStart(Long gameRoomId);

    boolean endSettingPhase(Long gameRoomId, int progressPlayerNum);

    void drawBlockAtStart(Long gameRoomId, Long playerId, int whiteBlockCount, int BlackBlockCount);

    void autoDrawAtStart(Long gameRoomId);

    boolean endStartPhase(Long gameRoomId, int playerOrderNum);

    GameStatusData getGameStatusData(Long gameRoomId);

    List<String> getSessionIdListInGameRoom(Long gameRoomId);

    List<OwnerBlockData> getOwnerBlockDataList(Long gameRoomId);
}
