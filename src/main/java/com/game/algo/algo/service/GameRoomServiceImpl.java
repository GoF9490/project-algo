package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.data.GameStatusUpdateCommand;
import com.game.algo.algo.dto.response.GameRoomSimple;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRoomServiceImpl implements GameRoomService {

    private final ApplicationEventPublisher eventPublisher;
    private final GameRoomRepository gameRoomRepository;
    private final PlayerRepository playerRepository; // 임시방편

    @Override
    @Transactional
    public Long create(String title) {
        GameRoom gameRoom = GameRoom.create(title);
        return gameRoomRepository.save(gameRoom).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public GameRoom findById(Long id) {
        return gameRoomRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.GAME_ROOM_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameRoomSimple> findSimpleListByStart(int page, boolean gameStart){
        return gameRoomRepository.getGameRoomSimpleListByGameStart(page, GameProperty.FIND_GAME_ROOM_SIZE, gameStart);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        gameRoomRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void gameStart(Long gameRoomId) {
        GameRoom findGameRoom = findById(gameRoomId);

         validGameStart(findGameRoom);

        findGameRoom.gameReset();
        findGameRoom.randomSetPlayerOrder();
        findGameRoom.updatePhase(GameRoom.Phase.SETTING);

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional
    public void endSettingPhase(Long gameRoomId, String sessionId) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.SETTING);
        validJoinPlayer(findGameRoom, sessionId);

        findGameRoom.allPlayerReadyOff();
        findGameRoom.updatePhase(GameRoom.Phase.START);

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void autoProgressAtStartPhase(Long gameRoomId) {
        GameRoom findGameRoom = findById(gameRoomId);
        Player player = findGameRoom.getProgressPlayer();

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.START);
        if (player.isReady()) {
            return;
        }

        int count = GameProperty.numberOfBlockAtStart(findGameRoom.getPlayerList().size());
        double randomValue = Math.random();

        int whiteBlockCount = (int) (randomValue * (count + 1));
        int blackBlockCount = count - whiteBlockCount;

        serveRandomBlocks(findGameRoom, player, BlockColor.WHITE, whiteBlockCount);
        serveRandomBlocks(findGameRoom, player, BlockColor.BLACK, blackBlockCount);

        player.updateReady(true);
    }

    @Override
    @Transactional
    public void serveRandomBlocks(GameRoom gameRoom, Player player, BlockColor blockColor, int count) {
        while (count-- > 0) player.addBlock(gameRoom.drawRandomBlock(blockColor));
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endStartPhase(Long gameRoomId, String sessionId) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.START);
        validJoinPlayer(findGameRoom, sessionId);

        if (!findGameRoom.getProgressPlayer().isReady()) {
            autoProgressAtStartPhase(gameRoomId);
        }

        if (findGameRoom.areAllPlayersReady()) {
            findGameRoom.allPlayerReadyOff();
            findGameRoom.updatePhase(GameRoom.Phase.DRAW);
            findGameRoom.addJoker();
            findGameRoom.progressZero();
        } else {
            findGameRoom.nextPlayer();
        }

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void autoProgressAtDrawPhase(Long gameRoomId) {
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

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endDrawPhase(Long gameRoomId, String sessionId) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.DRAW);
        validJoinPlayer(findGameRoom, sessionId);

        if (!findGameRoom.getProgressPlayer().isReady()) {
            autoProgressAtDrawPhase(gameRoomId);
        }

        findGameRoom.updatePhase(GameRoom.Phase.SORT);
        findGameRoom.allPlayerReadyOff();

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endSortPhase(Long gameRoomId, String sessionId) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.SORT);
        validJoinPlayer(findGameRoom, sessionId);

        findGameRoom.updatePhase(GameRoom.Phase.GUESS);
        findGameRoom.allPlayerReadyOff();

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endGuessPhase(Long gameRoomId, String sessionId) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.GUESS);
        validJoinPlayer(findGameRoom, sessionId);

        Player progressPlayer = findGameRoom.getProgressPlayer();

        if (progressPlayer.isReady()) {
            findGameRoom.updatePhase(GameRoom.Phase.REPEAT);
        } else {
            findGameRoom.getProgressPlayer().openDrawCard();
            findGameRoom.updatePhase(GameRoom.Phase.END);
        }
        checkGameOver(findGameRoom);
        progressPlayer.updateReady(false);

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endRepeatPhase(Long gameRoomId, String sessionId, boolean repeatGuess) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.REPEAT);
        validJoinPlayer(findGameRoom, sessionId);

        if (repeatGuess) {
            findGameRoom.updatePhase(GameRoom.Phase.GUESS);
        } else {
            findGameRoom.updatePhase(GameRoom.Phase.END);
        }

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endEndPhase(Long gameRoomId, String sessionId) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.END);
        validJoinPlayer(findGameRoom, sessionId);

        findGameRoom.nextPlayer();
        findGameRoom.updatePhase(GameRoom.Phase.DRAW);

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void endGameOverPhase(Long gameRoomId, String sessionId) {
        GameRoom findGameRoom = findById(gameRoomId);

        checkGamePhaseSync(findGameRoom, GameRoom.Phase.GAMEOVER);
        validJoinPlayer(findGameRoom, sessionId);

        findGameRoom.updatePhase(GameRoom.Phase.WAIT);
        findGameRoom.gameReset();
        findGameRoom.getPlayerList().forEach(Player::gameReset);

        banDisconnectPlayer(findGameRoom);

        sendGameStatusUpdateCommand(findGameRoom);
    }

    @Override
    public void sendGameStatusUpdateCommand(GameRoom gameRoom) {
        List<String> sessionIdList = gameRoom.getPlayerList().stream()
                .map(Player::getWebSocketSessionId)
                .toList();
        eventPublisher.publishEvent(GameStatusUpdateCommand.create(sessionIdList));
    }

    private void validGameStart(GameRoom findGameRoom) {
        if (!findGameRoom.areAllPlayersReady()) {
            throw new GameLogicException(GameExceptionCode.PLAYER_NOT_READY);
        }
        if (findGameRoom.getPlayerList().size() < 2) {
            throw new GameLogicException(GameExceptionCode.LACK_OF_PLAYER);
        }
    }

    private void validJoinPlayer(GameRoom gameRoom, String sessionId) {
        if (gameRoom.getPlayerList().stream().map(Player::getWebSocketSessionId).noneMatch(s -> s.equals(sessionId))) {
            throw new GameLogicException(GameExceptionCode.INVALID_PLAYER);
        }
    }

    private void checkGamePhaseSync(GameRoom gameRoom, GameRoom.Phase phase) {
        if (gameRoom.getPhase() != phase) {
            throw new GameLogicException(GameExceptionCode.PHASE_NOT_SYNC);
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
