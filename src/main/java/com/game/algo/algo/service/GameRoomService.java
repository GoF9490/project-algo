package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.response.GameRoomSimple;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GameRoomService {
    @Transactional
    Long create(String title);

    @Transactional(readOnly = true)
    GameRoom findById(Long id);

    @Transactional(readOnly = true)
    List<GameRoomSimple> findSimpleListByStart(int page, boolean start);

    @Transactional
    void deleteById(Long id);

    @Transactional
    void gameStart(Long gameRoomId);

    @Transactional
    void endSettingPhase(Long gameRoomId, int progressPlayerNum);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void autoProgressAtStartPhase(Long gameRoomId);

    @Transactional
    void serveRandomBlocks(GameRoom gameRoom, Player player, BlockColor blockColor, int count);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void endStartPhase(Long gameRoomId, int progressPlayerNum);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void autoProgressAtDrawPhase(Long gameRoomId);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void endDrawPhase(Long gameRoomId, int progressPlayerNum);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void endSortPhase(Long gameRoomId, int progressPlayerNum);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void endGuessPhase(Long gameRoomId, int progressPlayerNum);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void endRepeatPhase(Long gameRoomId, int progressPlayerNum, boolean repeatGuess);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void endEndPhase(Long gameRoomId, int progressPlayerNum);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void endGameOverPhase(Long gameRoomId, int progressPlayerNum);
}
