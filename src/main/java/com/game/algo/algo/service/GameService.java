package com.game.algo.algo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.dto.messagetype.GameRoomJoin;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;

public interface GameService {

    Long createPlayer(String name, String webSocketSessionId);

    Player findPlayerById(Long id);

    void updatePlayerReady(Player player, boolean isReady);


    Long createGameRoom();

    GameRoom findGameRoomById(Long id);

    void sendGameStatusByWebSocket(Long gameRoomId);

    void joinGameRoom(Long gameRoomId, Long playerId);

    void choiceBlock(GameRoom gameRoom, Player player, ChoiceBlockInfo choiceBlock);
}
