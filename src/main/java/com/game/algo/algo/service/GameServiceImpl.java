package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.dto.OwnerBlockData;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerJpaRepository;
import com.game.algo.websocket.data.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.game.algo.algo.entity.GameRoom.*;

/**
 * 클라이언트와 통신해야함
 * 매 페이즈마다 클라이언트와 확인절차를 거치게끔 하는게 좋을듯?
 * 도중에 튕기면 그에 알맞는 조치를 취해야함
 */

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRoomRepository gameRoomRepository;
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
    public Long createGameRoom(){
        GameRoom gameRoom = create();
        return gameRoomRepository.save(gameRoom).getId();
    }

    @Transactional(readOnly = true)
    public GameRoom findGameRoomById(Long id) {
        return gameRoomRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.GAME_ROOM_NOT_FOUND));
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
    public boolean endSettingPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        return doNextPhase(findGameRoom, progressPlayerNum, Phase.SETTING, Phase.START);
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

        findPlayer.completeWhiteJokerRelocation();
        findPlayer.completeBlackJokerRelocation();
    }

    @Transactional
    public boolean endStartPhase(Long gameRoomId, int playerOrderNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        if (doNextPhase(findGameRoom, playerOrderNum, Phase.START, Phase.CONTROL)) {
            findGameRoom.addJoker();
            return true;
        }
        return false;
    }

    private boolean doNextPhase(GameRoom findGameRoom, int playerOrderNum, Phase nowPhase, Phase nextPhase) {
        checkGamePhaseSync(findGameRoom, nowPhase);
        checkPlayerOrderSync(findGameRoom, playerOrderNum);

        if (findGameRoom.areAllPlayersReady()) {
            findGameRoom.allPlayerReadyOff();
            findGameRoom.updatePhase(nextPhase);
            return true;
        } else {
            return false;
        }
    }

    @Transactional(readOnly = true)
    public GameStatusData getGameStatusData(Long gameRoomId) {
        return GameStatusData.create(findGameRoomById(gameRoomId));
    }

    @Transactional(readOnly = true)
    public List<String> getSessionIdListInGameRoom(Long gameRoomId) {
        return findGameRoomById(gameRoomId).getPlayerList().stream()
                .map(Player::getWebSocketSessionId)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OwnerBlockData> getOwnerBlockDataList(Long gameRoomId) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        return findGameRoom.getPlayerList().stream()
                .map(OwnerBlockData::create)
                .collect(Collectors.toList());
    }

    private void validGameStart(GameRoom findGameRoom) {
        if (!findGameRoom.areAllPlayersReady()) {
            throw new GameLogicException(GameExceptionCode.ALL_PLAYER_NOT_READY);
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

    private List<String> getSessionIdListInGameRoom(GameRoom gameRoom) {
        return gameRoom.getPlayerList().stream()
                .map(Player::getWebSocketSessionId)
                .collect(Collectors.toList());
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
