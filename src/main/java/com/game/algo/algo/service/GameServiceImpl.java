package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.response.GameRoomFind;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomJpaRepository;
import com.game.algo.algo.repository.PlayerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.IntStream;

import static com.game.algo.algo.entity.GameRoom.*;

/**
 * 클라이언트와 통신해야함
 * 매 페이즈마다 클라이언트와 확인절차를 거치게끔 하는게 좋을듯?
 * 도중에 튕기면 그에 알맞는 조치를 취해야함
 * 책임을 나누는게 좋을까?
 */

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRoomJpaRepository gameRoomJPARepository;
    private final PlayerJpaRepository playerJpaRepository;

    public Long createPlayer(String name, String webSocketSessionId) {
        Player player = Player.create(name, webSocketSessionId);
        return playerJpaRepository.save(player).getId();
    }

    @Transactional(readOnly = true)
    public Player findPlayerById(Long id) {
        return playerJpaRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.PLAYER_NOT_FOUND));
    }

    @Transactional
    public Long createGameRoom(String title) {
        GameRoom gameRoom = create(title);
        return gameRoomJPARepository.save(gameRoom).getId();
    }

    @Transactional(readOnly = true)
    public GameRoom findGameRoomById(Long id) {
        return gameRoomJPARepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.GAME_ROOM_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public GameRoomFind findGameRoomsNotGameStart(int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        Page<GameRoom> gameRoomPage = gameRoomJPARepository.findAllByGameStart(false, pageRequest);
        return GameRoomFind.from(gameRoomPage);
    }

    @Transactional
    public void joinGameRoom(Long gameRoomId, Long playerId) {
        Player findPlayer = findPlayerById(playerId);
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        findGameRoom.joinPlayer(findPlayer);
    }

    @Transactional
    public void updatePlayerReady(Long playerId, boolean isReady) {
        Player findPlayer = findPlayerById(playerId);

        checkGamePhaseSync(findPlayer.getGameRoom(), Phase.WAIT);

        findPlayer.updateReady(isReady);
    }

    @Transactional
    public void gameStart(Long gameRoomId) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

//        validGameStart(findGameRoom);

        findGameRoom.allPlayerReadyOff();
        findGameRoom.gameReset();
        findGameRoom.playerOrderReset();
        findGameRoom.updatePhase(Phase.SETTING);
    }

    @Transactional
    public void endSettingPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.SETTING);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.allPlayerReadyOff();
        findGameRoom.updatePhase(Phase.START);
    }

    @Transactional
    public void drawBlockAtStart(Long gameRoomId, Long playerId, int whiteBlockCount, int blackBlockCount) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        Player findPlayer = findPlayerById(playerId);

        int maxBlockCount = numberOfBlockAtStart(findGameRoom);

        if (whiteBlockCount + blackBlockCount != maxBlockCount){
            autoDrawAtStart(gameRoomId);
            throw new GameLogicException(GameExceptionCode.INVALID_NUMBER_OF_BLOCKS);
        }

        addRandomBlocks(findGameRoom, findPlayer, BlockColor.WHITE, whiteBlockCount);
        addRandomBlocks(findGameRoom, findPlayer, BlockColor.BLACK, blackBlockCount);

        findPlayer.updateReady(true);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void autoDrawAtStart(Long gameRoomId) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        Player findPlayer = findGameRoom.getProgressPlayer();

        if (findPlayer.isReady()) {
            return;
        }

        findPlayer.updateReady(true);

        int count = numberOfBlockAtStart(findGameRoom);

        double randomValue = Math.random();

        int whiteBlockCount = (int) (randomValue * (count + 1));
        int blackBlockCount = count - whiteBlockCount;

        addRandomBlocks(findGameRoom, findPlayer, BlockColor.WHITE, whiteBlockCount);
        addRandomBlocks(findGameRoom, findPlayer, BlockColor.BLACK, blackBlockCount);
    }

    @Transactional
    public void endStartPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.START);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        if (findGameRoom.areAllPlayersReady()) {
            findGameRoom.allPlayerReadyOff();
            findGameRoom.updatePhase(Phase.DRAW);
            findGameRoom.addJoker();
            findGameRoom.progressZero();
        } else {
            findGameRoom.nextPlayer();
        }
    }
    
    @Transactional
    public void drawBlockAtDrawPhase(Long gameRoomId, Long playerId, BlockColor blockColor) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        Player findPlayer = findPlayerById(playerId);

        Block drawBlock = findGameRoom.drawRandomBlock(blockColor);
        findPlayer.addBlock(drawBlock);
        findPlayer.updateReady(true);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void autoDrawAtDrawPhase(Long gameRoomId) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        Player findPlayer = findGameRoom.getProgressPlayer();

        if (findPlayer.isReady()) {
            return;
        }

        double randomValue = Math.random();
        BlockColor blockColor = (randomValue * 2 < 1) ? BlockColor.WHITE : BlockColor.BLACK;

        Block drawBlock = findGameRoom.drawRandomBlock(blockColor);
        findPlayer.addBlock(drawBlock);
        findPlayer.updateReady(true);
    }
    
    @Transactional
    public void endDrawPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        
        checkGamePhaseSync(findGameRoom, Phase.DRAW);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.updatePhase(Phase.SORT);
        findGameRoom.allPlayerReadyOff();
    }

    @Transactional
    public void updatePlayerJoker(Long playerId,int newJokerIndex, BlockColor blockColor) {
        Player findPlayer = findPlayerById(playerId);

        if (findPlayer.getGameRoom().getPhase() != Phase.SORT) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }
        if (findPlayer.isReady()) {
            throw new GameLogicException(GameExceptionCode.ALREADY_EXECUTED);
        }

        findPlayer.updateJokerIndex(newJokerIndex, blockColor);
        findPlayer.updateReady(true);
    }

    @Transactional
    public void endSortPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.SORT);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.updatePhase(Phase.GUESS);
        findGameRoom.allPlayerReadyOff();
    }

    @Transactional
    public boolean guessBlock(Long guessPlayerId, Long targetPlayerId, int index, int num) {
        Player targetPlayer = findPlayerById(targetPlayerId);

        if (targetPlayer.guessBlock(index, num)) {
            Player guessPlayer = findPlayerById(guessPlayerId);
            guessPlayer.updateReady(true);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public void endGuessPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.GUESS);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        Player progressPlayer = findGameRoom.getProgressPlayer();

        if (progressPlayer.isReady()) {
            if (findGameRoom.isGameOver()) {
                findGameRoom.updatePhase(Phase.GAMEOVER);
            } else {
                findGameRoom.updatePhase(Phase.REPEAT);
            }
        } else {
            findGameRoom.getProgressPlayer().openDrawCard();
            findGameRoom.updatePhase(Phase.END);
        }

        progressPlayer.updateReady(false);
    }

    @Transactional
    public void endRepeatPhase(Long gameRoomId, int progressPlayerNum, boolean repeatGuess) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.REPEAT);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        if (repeatGuess) {
            findGameRoom.updatePhase(Phase.GUESS);
        } else {
            findGameRoom.updatePhase(Phase.END);
        }
    }

    @Transactional
    public void endEndPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.END);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.nextPlayer();
        findGameRoom.updatePhase(Phase.DRAW);
    }

    @Transactional
    public void endGameOverPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.GAMEOVER);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        findGameRoom.updatePhase(Phase.WAIT);
        findGameRoom.gameReset();
        findGameRoom.getPlayerList().forEach(Player::gameReset);
    }

    private void validGameStart(GameRoom findGameRoom) {
        if (!findGameRoom.areAllPlayersReady()) {
            throw new GameLogicException(GameExceptionCode.PLAYER_NOT_READY);
        }
        if (findGameRoom.getPlayerList().size() < 2) {
            throw new GameLogicException(GameExceptionCode.LACK_OF_PLAYER);
        }
    }

    private int numberOfBlockAtStart(GameRoom gameRoom) {
        return (gameRoom.getPlayerList().size() < 4) ? 4 : 3;
    }

    private void addRandomBlocks(GameRoom gameRoom, Player player, BlockColor blockColor, int count) {
        IntStream.range(0, count)
                .mapToObj(i -> gameRoom.drawRandomBlock(blockColor))
                .forEach(player::addBlock);
    }

    private void checkGamePhaseSync(GameRoom gameRoom, Phase phase) {
        if (gameRoom.getPhase() != phase) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }
    }

    private void checkPlayerOrderSync(GameRoom gameRoom, int playerOrderNum) {
        if (gameRoom.getProgressPlayerNumber() != playerOrderNum) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }
    }
}
