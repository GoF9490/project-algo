package com.game.algo.algo.service;

import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;

public interface GameService {

    Long createPlayer(String name, String webSocketSessionId);

    Player findPlayerById(Long id);

    Long createGameRoom();

    GameRoom findGameRoomById(Long id);

    boolean joinGameRoom(GameRoom gameRoom, Player player);

    void testLogging(String message);

    void choiceBlock(GameRoom gameRoom, Player player, ChoiceBlockInfo choiceBlock);

    void updatePlayerReady(Player player, boolean isReady);
}
