package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerServiceImpl implements PlayerService {

    private final GameRoomServiceImpl gameRoomService;
    private final PlayerRepository playerRepository;

    public Long create(String name, String webSocketSessionId) {
        Player player = Player.create(name, webSocketSessionId);
        return playerRepository.save(player).getId();
    }

    @Transactional(readOnly = true)
    public Player findById(Long id) {
        return playerRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.PLAYER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Player findByWebSocketSessionId(String webSocketSessionId) {
        return playerRepository.findByWebSocketSessionId(webSocketSessionId)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.PLAYER_NOT_FOUND));
    }

    @Transactional
    public void joinGameRoom(Long gameRoomId, Long playerId) {
        Player findPlayer = findById(playerId);
        GameRoom findGameRoom = gameRoomService.findById(gameRoomId);
        findGameRoom.joinPlayer(findPlayer);
    }

    @Transactional
    public void exitGameRoom(String sessionId) {
        Player findPlayer = findByWebSocketSessionId(sessionId);
        GameRoom gameRoom = findPlayer.getGameRoom();

        findPlayer.exit();

        deleteEmptyGameRoom(gameRoom);
    }

    @Transactional
    public void disconnectWebSession(String sessionId) {
        Player findPlayer = findByWebSocketSessionId(sessionId);

        if (findPlayer.getGameRoom() == null) {
            playerRepository.delete(findPlayer);
            return;
        }

        GameRoom gameRoom = findPlayer.getGameRoom();

        if (gameRoom.isGameStart()) {
            findPlayer.disconnect();
            gameRoom.updatePhase(GameRoom.Phase.GUESS);
        } else {
            findPlayer.exit();
            playerRepository.delete(findPlayer);
        }

        deleteEmptyGameRoom(gameRoom);
    }

    @Transactional
    public void updatePlayerReady(Long playerId, boolean isReady) {
        Player findPlayer = findById(playerId);

        if (findPlayer.getGameRoom().getPhase() != GameRoom.Phase.WAIT) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }

        findPlayer.updateReady(isReady);
    }

    @Transactional
    public void drawBlockAtStart(Long playerId, int whiteBlockCount, int blackBlockCount) {
        Player findPlayer = findById(playerId);
        GameRoom gameRoom = findPlayer.getGameRoom();

        int maxBlockCount = GameProperty.numberOfBlockAtStart(gameRoom.getPlayerList().size());

        if (whiteBlockCount + blackBlockCount != maxBlockCount){
            gameRoomService.autoProgressAtStartPhase(gameRoom.getId());
            throw new GameLogicException(GameExceptionCode.INVALID_NUMBER_OF_BLOCKS);
        }

        gameRoomService.serveRandomBlocks(gameRoom, findPlayer, BlockColor.WHITE, whiteBlockCount);
        gameRoomService.serveRandomBlocks(gameRoom, findPlayer, BlockColor.BLACK, blackBlockCount);

        findPlayer.updateReady(true);
    }

    @Transactional
    public void drawBlockAtDrawPhase(Long gameRoomId, Long playerId, BlockColor blockColor) {
        Player findPlayer = findById(playerId);
        GameRoom gameRoom = findPlayer.getGameRoom();

        Block drawBlock = gameRoom.drawRandomBlock(blockColor);
        findPlayer.addBlock(drawBlock);
        findPlayer.updateReady(true);
    }

    @Transactional
    public void updatePlayerJoker(Long playerId,int newJokerIndex, BlockColor blockColor) {
        Player findPlayer = findById(playerId);

        if (findPlayer.getGameRoom().getPhase() != GameRoom.Phase.SORT) {
            throw new GameLogicException(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE);
        }
        if (findPlayer.isReady()) {
            throw new GameLogicException(GameExceptionCode.ALREADY_EXECUTED);
        }

        findPlayer.updateJokerIndex(newJokerIndex, blockColor);
        findPlayer.updateReady(true);
    }

    @Transactional
    public boolean guessBlock(Long guessPlayerId, Long targetPlayerId, int index, int num) {
        Player targetPlayer = findById(targetPlayerId);

        if (targetPlayer.guessBlock(index, num)) {
            Player guessPlayer = findById(guessPlayerId);
            guessPlayer.updateReady(true);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public void repeatGuess(Long playerId) {
        Player findPlayer = findById(playerId);
        gameRoomService.endRepeatPhase(findPlayer.getGameRoom().getId(), findPlayer.getOrderNumber(), true);
    }

    private void deleteEmptyGameRoom(GameRoom gameRoom) {
        if (gameRoom.getPlayerList().stream().allMatch(player -> player.getWebSocketSessionId().equals("disconnect"))) {
            gameRoomService.deleteById(gameRoom.getId());
        }
    }
}
