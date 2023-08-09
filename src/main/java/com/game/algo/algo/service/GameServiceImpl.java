package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.dto.MultipleMessageSupporter;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerJpaRepository;
import com.game.algo.websocket.data.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Transactional
    public void drawBlockAtStart(Long gameRoomId, Long playerId, int whiteBlockCount, int blackBlockCount) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        Player findPlayer = findPlayerById(playerId);

        int maxBlockCount = numberOfBlockAtStart(findGameRoom);

        if (whiteBlockCount + blackBlockCount != maxBlockCount){
            autoDrawAtStart(findGameRoom, findPlayer, maxBlockCount);
            throw new GameLogicException(GameExceptionCode.INVALID_NUMBER_OF_BLOCKS);
        }

        addRandomBlocks(findGameRoom, findPlayer, BlockColor.WHITE, whiteBlockCount);
        addRandomBlocks(findGameRoom, findPlayer, BlockColor.BLACK, blackBlockCount);
    }

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
        GameRoom gameRoom = GameRoom.create();
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
    public void gameStart(Long gameRoomId, Long playerId) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        if (!findGameRoom.areAllPlayersReady()) {
            throw new GameLogicException(GameExceptionCode.ALL_PLAYER_NOT_READY);
        }

        findGameRoom.allPlayerReadyOff();
        findGameRoom.gameReset();
        findGameRoom.playerOrderReset();
        findGameRoom.updatePhase(GameRoom.Phase.SETTING);
    }

    public void nextPhase(Long gameRoomId) { // 조정중
        GameRoom gameRoom = findGameRoomById(gameRoomId);
        switch (gameRoom.getPhase()) {
            case SETTING:
                if (gameRoom.areAllPlayersReady()) {
                    gameRoom.allPlayerReadyOff();
                    gameRoom.updatePhase(GameRoom.Phase.START);
                } else {
                    if (!gameRoom.getProgressPlayer().isReady()) {
                        autoDrawAtStart(gameRoom, gameRoom.getProgressPlayer(), numberOfBlockAtStart(gameRoom));
                    }
                    gameRoom.nextPlayer();
                }
                break;

            case START:
                break;
        }
    }

    @Transactional
    public void settingPhaseLogic(Long gameRoomId) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);
        checkGamePhaseSync(findGameRoom, GameRoom.Phase.SETTING);

        if (findGameRoom.areAllPlayersReady()) {
            findGameRoom.allPlayerReadyOff();
            findGameRoom.updatePhase(GameRoom.Phase.START);
        } else {
            if (!findGameRoom.getProgressPlayer().isReady()) {
                autoDrawAtStart(findGameRoom, findGameRoom.getProgressPlayer(), numberOfBlockAtStart(findGameRoom));
            }
            findGameRoom.nextPlayer();
        }
    }

    @Transactional(readOnly = true)
    public List<String> getSessionIdListInGameRoom(Long gameRoomId) {
        return findGameRoomById(gameRoomId).getPlayerList().stream()
                .map(Player::getWebSocketSessionId)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GameStatusData getGameStatusData(Long gameRoomId) {
        return GameStatusData.create(findGameRoomById(gameRoomId));
    }

    private void autoDrawAtStart(GameRoom gameRoom, Player player, int count) {
        double randomValue = Math.random();

        int whiteBlockCount = (int) (randomValue * (count + 1));
        int blackBlockCount = count - whiteBlockCount;

        addRandomBlocks(gameRoom, player, BlockColor.WHITE, whiteBlockCount);
        addRandomBlocks(gameRoom, player, BlockColor.BLACK, blackBlockCount);

        player.completeWhiteJokerRelocation();
        player.completeBlackJokerRelocation();
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

    private void checkGamePhaseSync(GameRoom gameRoom, GameRoom.Phase phase) {
        if (gameRoom.getPhase() != phase) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }
    }
}
