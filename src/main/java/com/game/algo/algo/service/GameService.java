package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.GameRoomFind;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;

public interface GameService {

    Long createPlayer(String name, String webSocketSessionId);

    Player findPlayerById(Long id);

    Long createGameRoom(String title);

    GameRoom findGameRoomById(Long id);

    GameRoomFind findGameRoomsNotGameStart(int page, int size);

    void updatePlayerReady(Long playerId, boolean isReady);

    void joinGameRoom(Long gameRoomId, Long playerId);

    void gameStart(Long gameRoomId);

    void endSettingPhase(Long gameRoomId, int progressPlayerNum);

    void drawBlockAtStart(Long gameRoomId, Long playerId, int whiteBlockCount, int BlackBlockCount);

    void autoDrawAtStart(Long gameRoomId);

    void endStartPhase(Long gameRoomId, int playerOrderNum);

    void drawBlockAtDrawPhase(Long gameRoomId, Long playerId, BlockColor blockColor);

    void autoDrawAtDrawPhase(Long gameRoomId);

    void endDrawPhase(Long gameRoomId, int progressPlayerNum);

    void updatePlayerJoker(Long playerId,int newJokerIndex, BlockColor blockColor);

    void endSortPhase(Long gameRoomId, int progressPlayerNum);

    boolean guessBlock(Long guessPlayerId, Long targetPlayerId, int index, int num);

    void endGuessPhase(Long gameRoomId, int progressPlayerNum);

    void endRepeatPhase(Long gameRoomId, int progressPlayerNum, boolean repeatGuess);

    void endEndPhase(Long gameRoomId, int progressPlayerNum);

    void endGameOverPhase(Long gameRoomId, int progressPlayerNum);
}
