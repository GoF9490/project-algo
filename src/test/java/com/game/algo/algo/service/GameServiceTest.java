package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class GameServiceTest {

    @Autowired private GameService gameService;


    @Test
    @DisplayName("존재하지 않는 Player의 Id를 조회하려 하면 알맞은 익셉션이 발생합니다.")
    public void findPlayerByIdFail() throws Exception {
        //given
        Long playerId = 15L;

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.findPlayerById(playerId))
                .withMessageMatching(GameExceptionCode.PLAYER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Player를 생성하고 저장, Id로 찾기가 정상적으로 이루어져야 합니다.")
    public void createPlayerAndFindPlayerByIdSuccess() throws Exception {
        //given
        String name = "foo";
        String webSocketSessionId = "sessionId";

        //when
        Long playerId = gameService.createPlayer(name, webSocketSessionId);
        Player player = gameService.findPlayerById(playerId);

        //then
        assertThat(player.getId()).isEqualTo(playerId);
        assertThat(player.getName()).isEqualTo(name);
        assertThat(player.getWebSocketSessionId()).isEqualTo(webSocketSessionId);
    }

    @Test
    @DisplayName("Player의 ready값이 true로 변경되어야 합니다.")
    public void updatePlayerReadySuccess() throws Exception {
        //given
        Long playerId = gameService.createPlayer("foo", "sessionId");

        //when
        gameService.updatePlayerReady(playerId, true);
        Player player = gameService.findPlayerById(playerId);

        //then
        assertThat(player.isReady()).isTrue();

    }

    @Test
    @DisplayName("존재하지 않는 GameRoom의 Id를 조회하려 하면 알맞은 익셉션이 발생합니다.")
    public void findGameRoomByIdSuccess() throws Exception {
        //given
        Long gameRoomId = 15L;

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.findGameRoomById(gameRoomId))
                .withMessageMatching(GameExceptionCode.GAME_ROOM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("GameRoom을 생성하고 저장, Id로 찾기가 정상적으로 이루어져야 합니다.")
    public void createGameRoomAndFindGameRoomByIdSuccess() throws Exception {
        //given

        //when
        Long gameRoomId = gameService.createGameRoom();
        GameRoom gameRoom = gameService.findGameRoomById(gameRoomId);

        //then
        assertThat(gameRoom.getId()).isEqualTo(gameRoomId);
    }

    @Test
    @DisplayName("GameRoom에 Player가 정상적으로 참가되어야 합니다.")
    public void joinGameRoomSuccess() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        Long playerId = gameService.createPlayer("foo", "sessionId");

        //when
        gameService.joinGameRoom(gameRoomId, playerId);
        GameRoom gameRoom = gameService.findGameRoomById(gameRoomId);

        //then
        assertThat(gameRoom.getPlayerList().size()).isEqualTo(1);
        assertThat(gameRoom.getPlayerList().get(0).getId()).isEqualTo(playerId);
    }

    @Test
    @DisplayName("GameRoom에 Player가 4명이상 있을경우 익셉션이 발생합니다.")
    public void joinGameRoomFail() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        LongStream.range(0, 4)
                .map(l -> gameService.createPlayer("foo" + l, "sessionId" + l))
                .forEach(playerId -> gameService.joinGameRoom(gameRoomId, playerId));
        Long latePlayerId = gameService.createPlayer("latePlayer", "sessionId");

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.joinGameRoom(gameRoomId, latePlayerId))
                .withMessageMatching(GameExceptionCode.GAME_ROOM_IS_FULL.getMessage());
    }

    @Test
    @DisplayName("모든 플레이어가 준비완료일때 정상적으로 게임이 시작되며 SETTING 페이즈로 넘어갑니다.")
    public void gameStartSuccess() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);
        IntStream.range(0, 4)
                .mapToObj(i -> Player.create("player" + i, "foo"))
                .forEach(player -> {
                    findGameRoom.joinPlayer(player);
                    player.updateReady(true);
                });

        //when
        gameService.gameStart(gameRoomId);

        //then
        assertThat(findGameRoom.areAllPlayersReady()).isFalse();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SETTING);

    }

//    @Test
//    @DisplayName("플레이어가 한명일경우 게임을 시작할 수 없습니다.")
//    public void gameStartFailCase1() throws Exception {
//        //given
//        Long gameRoomId = gameService.createGameRoom();
//        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);
//        Player player = Player.create("player1", "foo");
//        player.updateReady(true);
//        findGameRoom.joinPlayer(player);
//
//        //expect
//        assertThatExceptionOfType(GameLogicException.class)
//                .isThrownBy(() -> gameService.gameStart(gameRoomId))
//                .withMessageMatching(GameExceptionCode.LACK_OF_PLAYER.getMessage());
//    }
//
//    @Test
//    @DisplayName("모든 플레이어가 준비하지 않으면 게임을 시작할 수 없습니다.")
//    public void gameStartFailCase2() throws Exception {
//        //given
//        Long gameRoomId = gameService.createGameRoom();
//        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);
//        IntStream.range(0, 4)
//                .mapToObj(i -> Player.create("player" + i, "foo"))
//                .forEach(player -> {
//                    findGameRoom.joinPlayer(player);
//                });
//
//        //expect
//        assertThatExceptionOfType(GameLogicException.class)
//                .isThrownBy(() -> gameService.gameStart(gameRoomId))
//                .withMessageMatching(GameExceptionCode.ALL_PLAYER_NOT_READY.getMessage());
//    }

    @Test
    @DisplayName("조건에 맞기에 SETTING 페이즈가 START 페이즈로 넘어갑니다.")
    public void endSettingPhaseSuccess() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);

        findGameRoom.updatePhase(GameRoom.Phase.SETTING);

        IntStream.range(0, 2)
                .mapToLong(i -> gameService.createPlayer("player" + i, "sessionId" + i))
                .forEach(playerId -> {
                    gameService.joinGameRoom(gameRoomId, playerId);
                    gameService.updatePlayerReady(playerId, true);
                });

        //when
        gameService.endSettingPhase(gameRoomId, findGameRoom.getProgressPlayerNumber());

        //then
        assertThat(gameService.findGameRoomById(gameRoomId).getPhase()).isEqualTo(GameRoom.Phase.START);
    }

    @Test
    @DisplayName("progressPlayerNum 숫자가 맞지않기에 SETTING 페이즈에 머무릅니다.")
    public void endSettingPhaseFail() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);

        findGameRoom.updatePhase(GameRoom.Phase.SETTING);

        IntStream.range(0, 2)
                .mapToLong(i -> gameService.createPlayer("player" + i, "sessionId" + i))
                .forEach(playerId -> {
                    gameService.joinGameRoom(gameRoomId, playerId);
                });

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.endSettingPhase(gameRoomId, 10))
                .withMessageMatching(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE.getMessage());

        assertThat(gameService.findGameRoomById(gameRoomId).getPhase()).isEqualTo(GameRoom.Phase.SETTING);

    }

    @RepeatedTest(5)
    @DisplayName("GameRoom의 블록을 Player에게 알맞게 전달합니다.")
    public void drawBlockTestSuccess() throws Exception {
        //given
        Long playerId = gameService.createPlayer("foo", "sessionId");
        Long gameRoomId = gameService.createGameRoom();

        int whiteBlockCount = 1;
        int blackBlockCount = 3;

        gameService.findGameRoomById(gameRoomId).gameReset();

        //when
        gameService.drawBlockAtStart(gameRoomId, playerId, whiteBlockCount, blackBlockCount);

        Player player = gameService.findPlayerById(playerId);

        //then
        assertThat(player.getBlockList().size()).isEqualTo(whiteBlockCount + blackBlockCount);

        assertThat(howManyWhiteBlock(player.getBlockList())).isEqualTo(whiteBlockCount);
        assertThat(howManyBlackBlock(player.getBlockList())).isEqualTo(blackBlockCount);

        player.getBlockList()
                .forEach(block -> assertThat(block.getNum()).isBetween(0, 12));

        System.out.println(player.getBlockList().stream()
                .map(block -> block.getBlockCode(true))
                .collect(Collectors.toList()).toString());
    }

    @Test
    @DisplayName("뽑으려는 블록의 개수가 비정상적일때 익셉션이 발생합니다.")
    public void drawBlockTestFail_InvalidNumberOfBlocks() throws Exception {
        //given
        Long playerId = gameService.createPlayer("foo", "sessionId");
        Long gameRoomId = gameService.createGameRoom();

        int whiteBlockCount = 2;
        int blackBlockCount = 3;

        gameService.joinGameRoom(gameRoomId, playerId);

        //expect
        gameService.findGameRoomById(gameRoomId).gameReset();
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.drawBlockAtStart(gameRoomId, playerId, whiteBlockCount, blackBlockCount))
                .withMessageMatching(GameExceptionCode.INVALID_NUMBER_OF_BLOCKS.getMessage());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 동시성 문제 체크를 위해
    @DisplayName("요청이 여러번 들어와도 autoDraw 기능이 1번에 한해 성공적으로 이루어집니다.")
    public void autoDrawAtStartSuccess() throws Exception {
        //given
        Long playerId = gameService.createPlayer("foo", "sessionId");
        Long gameRoomId = gameService.createGameRoom();

        gameService.gameStart(gameRoomId);

        gameService.joinGameRoom(gameRoomId, playerId);

        //when
        try {
            IntStream.range(0, 4)
                    .parallel()
                    .forEach(i -> gameService.autoDrawAtStart(gameRoomId));
        } catch (CannotAcquireLockException e) {

        }

        //then
        Player findPlayer = gameService.findPlayerById(playerId);
        assertThat(findPlayer.getBlockList().size()).isEqualTo(4);
        assertThat(findPlayer.isReady()).isTrue();

        findPlayer.getBlockList()
                .forEach(block -> System.out.println(block.getBlockCode(true)));
    }

    @Test
    @DisplayName("START 페이즈에서 DRAW 페이즈로 정상적으로 넘어가져야 합니다.")
    public void endStartPhaseSuccess() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);

        findGameRoom.updatePhase(GameRoom.Phase.START);

        IntStream.range(0, 2)
                .mapToLong(i -> gameService.createPlayer("player" + i, "sessionId" + i))
                .forEach(playerId -> {
                    gameService.joinGameRoom(gameRoomId, playerId);
                    gameService.updatePlayerReady(playerId, true);
                });

        //when
        gameService.endStartPhase(gameRoomId, findGameRoom.getProgressPlayerNumber());

        //then
        assertThat(gameService.findGameRoomById(gameRoomId).getPhase()).isEqualTo(GameRoom.Phase.DRAW);
        assertThat(gameService.findGameRoomById(gameRoomId).getWhiteBlockList().stream().anyMatch(Block::isJoker)).isTrue();
    }

    @Test
    @DisplayName("조건에 맞지않기에 START 페이즈에 머무릅니다.")
    public void endStartPhaseFail() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);

        findGameRoom.updatePhase(GameRoom.Phase.START);

        IntStream.range(0, 2)
                .mapToLong(i -> gameService.createPlayer("player" + i, "sessionId" + i))
                .forEach(playerId -> {
                    gameService.joinGameRoom(gameRoomId, playerId);
                });

        //when
        gameService.endStartPhase(gameRoomId, findGameRoom.getProgressPlayerNumber());

        //then
        assertThat(gameService.findGameRoomById(gameRoomId).getPhase()).isEqualTo(GameRoom.Phase.START);
        assertThat(gameService.findGameRoomById(gameRoomId).getWhiteBlockList().stream().noneMatch(Block::isJoker)).isTrue();
    }

    @Test
    @DisplayName("Player가 GameRoom에 있는 지정한 색깔의 블럭을 하나 가져옵니다.")
    public void drawBlockAtDrawPhaseSuccess() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        Long playerId = gameService.createPlayer("foo", "bar");

        gameService.joinGameRoom(gameRoomId, playerId);

        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);
        findGameRoom.gameReset();
        findGameRoom.addJoker();
        findGameRoom.updatePhase(GameRoom.Phase.DRAW);

        //when
        gameService.drawBlockAtDrawPhase(gameRoomId, playerId, BlockColor.WHITE);

        //then
        Player findPlayer = gameService.findPlayerById(playerId);

        assertThat(findPlayer.getBlockList().size()).isEqualTo(1);
        assertThat(findPlayer.getBlockList().get(0).isWhite()).isTrue();
        assertThat(findPlayer.getDrawBlockIndexNum()).isEqualTo(0);
//        assertThat(findPlayer.isReady()).isTrue();
    }

    @Test
    @DisplayName("플레이어가 자동으로 블럭을 하나 가져오는데 성공합니다.")
    public void autoDrawAtDrawPhaseSuccess() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        Long playerId = gameService.createPlayer("foo", "bar");

        gameService.joinGameRoom(gameRoomId, playerId);

        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);
        findGameRoom.gameReset();
        findGameRoom.addJoker();
        findGameRoom.updatePhase(GameRoom.Phase.DRAW);

        //when
        gameService.autoDrawAtDrawPhase(gameRoomId);

        //then
        Player findPlayer = gameService.findPlayerById(playerId);

        assertThat(findPlayer.getBlockList().size()).isEqualTo(1);
        assertThat(findPlayer.getDrawBlockIndexNum()).isEqualTo(0);
//        assertThat(findPlayer.isReady()).isTrue();
    }

    @Test
    @DisplayName("DRAW 페이즈가 정상적으로 SORT 페이즈로 넘어갑니다.")
    public void endDrawPhaseSuccess() throws Exception {
        //given
        Long gameRoomId = gameService.createGameRoom();
        Long playerId = gameService.createPlayer("foo", "bar");

        gameService.joinGameRoom(gameRoomId, playerId);

        GameRoom findGameRoom = gameService.findGameRoomById(gameRoomId);
        findGameRoom.updatePhase(GameRoom.Phase.DRAW);

        gameService.updatePlayerReady(playerId, true);

        //when
        gameService.endDrawPhase(gameRoomId, findGameRoom.getProgressPlayerNumber());

        //then
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SORT);
        assertThat(findGameRoom.getProgressPlayer().isReady()).isFalse();
    }

    private long howManyWhiteBlock(List<Block> BlockList) {
        return BlockList.stream().filter(Block::isWhite).count();
    }

    private long howManyBlackBlock(List<Block> BlockList) {
        return BlockList.stream().filter(Block::isBlack).count();
    }
}