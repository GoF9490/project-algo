package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.dto.OwnerBlockData;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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

        checkGamePhaseSync(findGameRoom, Phase.SETTING);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        if (findGameRoom.areAllPlayersReady()) { // 구조를 바꿀까, 컨트롤러단에서 체크해서 호출하게끔 (boolean 리턴타입이 거슬림)
            findGameRoom.allPlayerReadyOff();
            findGameRoom.updatePhase(Phase.START);
            return true;
        } else {
            return false;
        }
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

        // 굳이? 일단 주석처리 해놓고 오류없으면 지우기
//        findPlayer.completeWhiteJokerRelocation();
//        findPlayer.completeBlackJokerRelocation();
    }

    @Transactional
    public boolean endStartPhase(Long gameRoomId, int progressPlayerNum) {
        GameRoom findGameRoom = findGameRoomById(gameRoomId);

        checkGamePhaseSync(findGameRoom, Phase.START);
        checkPlayerOrderSync(findGameRoom, progressPlayerNum);

        if (findGameRoom.areAllPlayersReady()) { // 구조를 바꿀까
            findGameRoom.allPlayerReadyOff();
            findGameRoom.updatePhase(Phase.DRAW);
            findGameRoom.addJoker();
            return true;
        } else {
            return false;
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
        
        if (findGameRoom.getProgressPlayer().isReady()) {
            findGameRoom.getProgressPlayer().updateReady(false);
            findGameRoom.updatePhase(Phase.SORT);
        } else {
            throw new GameLogicException(GameExceptionCode.PLAYER_NOT_READY); // 변경 필요?
        }
    }

    public void updatePlayerJoker(Long playerId, int frontNum, int backNum, BlockColor blockColor) {
        Player findPlayer = findPlayerById(playerId);

        findPlayer.updateJoker(frontNum, backNum, blockColor);
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
