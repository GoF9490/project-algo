package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.entity.Player;
import org.springframework.transaction.annotation.Transactional;

public interface PlayerService {
    @Transactional
    Long create(String name, String webSocketSessionId);

    @Transactional(readOnly = true)
    Player findById(Long id);

    @Transactional(readOnly = true)
    Player findByWebSocketSessionId(String webSocketSessionId);

    @Transactional
    void joinGameRoom(String sessionId, Long gameRoomId);

    @Transactional
    void exitGameRoom(String sessionId);

    @Transactional
    void disconnectWebSession(String sessionId);

    @Transactional
    void updatePlayerReady(String sessionId, boolean isReady);

    @Transactional
    void drawBlockAtStart(Long playerId, int whiteBlockCount, int blackBlockCount);

    @Transactional
    void drawBlockAtDrawPhase(Long playerId, BlockColor blockColor);

    @Transactional
    void updatePlayerJoker(Long playerId, int newJokerIndex, BlockColor blockColor);

    @Transactional
    boolean guessBlock(Long guessPlayerId, Long targetPlayerId, int index, int num);

    @Transactional
    void repeatGuess(Long playerId);

    @Transactional(readOnly = true)
    void validSessionIdInGameRoom(String sessionId, Long gameRoomId);
}
