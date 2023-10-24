package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.dto.response.GameRoomSimple;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GameRoomServiceImpl implements GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final PlayerRepository playerRepository; // 임시방편

    @Transactional
    public Long create(String title) {
        GameRoom gameRoom = GameRoom.create(title);
        return gameRoomRepository.save(gameRoom).getId();
    }

    @Transactional(readOnly = true)
    public GameRoom findById(Long id) {
        return gameRoomRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.GAME_ROOM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<GameRoomSimple> findSimpleListByStart(int page, boolean start){
        return gameRoomRepository.findGameRoomSimples(page, GameProperty.FIND_GAME_ROOM_SIZE, start);
    }

    @Transactional
    public void deleteById(Long id) {
        gameRoomRepository.deleteById(id);
    }

    @Transactional
    public void gameStart(Long gameRoomId) {
        GameRoom findGameRoom = findById(gameRoomId);

        validGameStart(findGameRoom);

        findGameRoom.gameReset();
        findGameRoom.randomSetPlayerOrder();
        findGameRoom.updatePhase(GameRoom.Phase.SETTING);
    }

    @Transactional
    public void endSettingPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.SETTING);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.allPlayerReadyOff();
        findGameRoom.updatePhase(GameRoom.Phase.START);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void autoProgressAtStartPhase(Long gameRoomId) {
        GameRoom findGameRoom = findById(gameRoomId);
        Player findPlayer = findGameRoom.getProgressPlayer();

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.START);
        if (findPlayer.isReady()) {
            return;
        }

        findPlayer.updateReady(true);

        int count = GameProperty.numberOfBlockAtStart(findGameRoom.getPlayerList().size());

        double randomValue = Math.random();

        int whiteBlockCount = (int) (randomValue * (count + 1));
        int blackBlockCount = count - whiteBlockCount;

        serveRandomBlocks(findGameRoom, findPlayer, BlockColor.WHITE, whiteBlockCount);
        serveRandomBlocks(findGameRoom, findPlayer, BlockColor.BLACK, blackBlockCount);
    }

    @Transactional
    public void serveRandomBlocks(GameRoom gameRoom, Player player, BlockColor blockColor, int count) {
        while (count-- > 0) player.addBlock(gameRoom.drawRandomBlock(blockColor));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endStartPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.START);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        if (findGameRoom.areAllPlayersReady()) {
            findGameRoom.allPlayerReadyOff();
            findGameRoom.updatePhase(GameRoom.Phase.DRAW);
            findGameRoom.addJoker();
            findGameRoom.progressZero();
        } else {
            findGameRoom.nextPlayer();
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void autoDrawAtDrawPhase(Long gameRoomId) {
        GameRoom findGameRoom = findById(gameRoomId);
        Player findPlayer = findGameRoom.getProgressPlayer();

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.DRAW);
        if (findPlayer.isReady()) {
            return;
        }

        double randomValue = Math.random();
        BlockColor blockColor = (randomValue * 2 < 1) ? BlockColor.WHITE : BlockColor.BLACK;

        Block drawBlock = findGameRoom.drawRandomBlock(blockColor);
        findPlayer.addBlock(drawBlock);
        findPlayer.updateReady(true);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endDrawPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.DRAW);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.updatePhase(GameRoom.Phase.SORT);
        findGameRoom.allPlayerReadyOff();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endSortPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.SORT);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.updatePhase(GameRoom.Phase.GUESS);
        findGameRoom.allPlayerReadyOff();
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endGuessPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.GUESS);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        Player progressPlayer = findGameRoom.getProgressPlayer();

        if (progressPlayer.isReady()) {
            findGameRoom.updatePhase(GameRoom.Phase.REPEAT);
        } else {
            findGameRoom.getProgressPlayer().openDrawCard();
            findGameRoom.updatePhase(GameRoom.Phase.END);
        }

        checkGameOver(findGameRoom);

        progressPlayer.updateReady(false);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endRepeatPhase(Long gameRoomId, int progressPlayerNum, boolean repeatGuess) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.REPEAT);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        if (repeatGuess) {
            findGameRoom.updatePhase(GameRoom.Phase.GUESS);
        } else {
            findGameRoom.updatePhase(GameRoom.Phase.END);
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endEndPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.END);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.nextPlayer();
        findGameRoom.updatePhase(GameRoom.Phase.DRAW);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endGameOverPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.GAMEOVER);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.updatePhase(GameRoom.Phase.WAIT);
        findGameRoom.gameReset();
        findGameRoom.getPlayerList().forEach(Player::gameReset);

        banDisconnectPlayer(findGameRoom);
    }

    private void validGameStart(GameRoom findGameRoom) {
        if (!findGameRoom.areAllPlayersReady()) {
            throw new GameLogicException(GameExceptionCode.PLAYER_NOT_READY);
        }
        if (findGameRoom.getPlayerList().size() < 2) {
            throw new GameLogicException(GameExceptionCode.LACK_OF_PLAYER);
        }
    }

    private void checkPlayerOrderSync(GameRoom gameRoom, int playerOrderNum) {
        if (gameRoom.getProgressPlayerNumber() != playerOrderNum) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }
    }

    private void checkGamePhaseSync(GameRoom gameRoom, GameRoom.Phase phase) {
        if (gameRoom.getPhase() != phase) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }
    }

    private static void checkGameOver(GameRoom gameRoom) {
        if (gameRoom.isGameOver()) {
            gameRoom.updatePhase(GameRoom.Phase.GAMEOVER);
        }
    }

    private void banDisconnectPlayer(GameRoom gameRoom) {
        List<Player> disconnectPlayer = gameRoom.getPlayerList().stream()
                .filter(player -> player.getWebSocketSessionId().equals("disconnect"))
                .collect(Collectors.toList());

        disconnectPlayer.forEach(Player::exit);

        playerRepository.deleteAll(disconnectPlayer); // gameRoom에서 exit 시킨다음 batch 돌려서 삭제하는 편이 나을듯

        deleteEmptyGameRoom(gameRoom);
    }

    private void deleteEmptyGameRoom(GameRoom gameRoom) {
        if (gameRoom.getPlayerList().stream().allMatch(player -> player.getWebSocketSessionId().equals("disconnect"))) {
            gameRoomRepository.delete(gameRoom);
        }
    }
}
