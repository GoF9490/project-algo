package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.response.GameRoomFind;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class GameServiceTest {

    @Autowired private GameService gameService;
    @Autowired private GameRoomRepository gameRoomRepository;
    @Autowired private PlayerRepository playerRepository;

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);
        gameRoom.updatePhase(GameRoom.Phase.WAIT);

        //when
        gameService.updatePlayerReady(player.getId(), true);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();
        assertThat(findPlayer.isReady()).isTrue();

    }

    @Test
    @DisplayName("존재하지 않는 GameRoom의 Id를 조회하려 하면 알맞은 익셉션이 발생합니다.")
    public void findGameRoomByIdFail() throws Exception {
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
        //when
        Long gameRoomId = gameService.createGameRoom("GameRoom");
        GameRoom gameRoom = gameService.findGameRoomById(gameRoomId);

        //then
        assertThat(gameRoom.getId()).isEqualTo(gameRoomId);
    }

    @Test
    @DisplayName("GameRoom에 Player가 정상적으로 참가되어야 합니다.")
    public void joinGameRoomSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        //when
        gameService.joinGameRoom(gameRoom.getId(), player.getId());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPlayerList().size()).isEqualTo(1);
        assertThat(findGameRoom.getPlayerList().get(0).getId()).isEqualTo(player.getId());
    }

    @Test
    @DisplayName("GameRoom에 Player가 4명이상 있을경우 익셉션이 발생합니다.")
    public void joinGameRoomFail() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        LongStream.range(0, 4)
                .map(l -> playerRepository.save(Player.create("foo" + l, "sessionId" + l)).getId())
                .forEach(playerId -> gameService.joinGameRoom(gameRoom.getId(), playerId));
        Long latePlayerId = playerRepository.save(Player.create("lastPlayer", "sessionId")).getId();

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.joinGameRoom(gameRoom.getId(), latePlayerId))
                .withMessageMatching(GameExceptionCode.GAME_ROOM_IS_FULL.getMessage());
    }

    @Test
    @DisplayName("모든 플레이어가 준비완료일때 정상적으로 게임이 시작되며 SETTING 페이즈로 넘어갑니다.")
    public void gameStartSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        IntStream.range(0, 4)
                .mapToObj(i -> playerRepository.save(Player.create("player" + i, "sessionId")))
                .forEach(player -> {
                    player.updateReady(true);
                    gameRoom.joinPlayer(player);
                });

        //when
        gameService.gameStart(gameRoom.getId());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.areAllPlayersReady()).isFalse();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SETTING);
    }
    
    // 1인 플레이 환경으로 테스트중이기에 다인플레이에서의 익셉션은 잠시 닫아놓기
//    @Test
//    @DisplayName("플레이어가 한명일경우 게임을 시작할 수 없습니다.")
//    public void gameStartFailCase1() throws Exception {
//        //given
//        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
//        Player player = playerRepository.save(Player.create("foo", "sessionId"));
//        player.updateReady(true);
//        gameRoom.joinPlayer(player);
//
//        //expect
//        assertThatExceptionOfType(GameLogicException.class)
//                .isThrownBy(() -> gameService.gameStart(gameRoom.getId()))
//                .withMessageMatching(GameExceptionCode.LACK_OF_PLAYER.getMessage());
//    }
//
//    @Test
//    @DisplayName("모든 플레이어가 준비하지 않으면 게임을 시작할 수 없습니다.")
//    public void gameStartFailCase2() throws Exception {
//        //given
//        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
//        IntStream.range(0, 4)
//                .mapToObj(i -> playerRepository.save(Player.create("player" + i, "sessionId")))
//                .forEach(gameRoom::joinPlayer);
//
//        //expect
//        assertThatExceptionOfType(GameLogicException.class)
//                .isThrownBy(() -> gameService.gameStart(gameRoom.getId()))
//                .withMessageMatching(GameExceptionCode.PLAYER_NOT_READY.getMessage());
//    }

    @Test
    @DisplayName("조건에 맞기에 SETTING 페이즈가 START 페이즈로 넘어갑니다.")
    public void endSettingPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(player -> {
                    player.updateReady(true);
                    gameRoom.joinPlayer(player);
                });

        gameRoom.updatePhase(GameRoom.Phase.SETTING);

        //when
        gameService.endSettingPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.START);
    }

    @Test
    @DisplayName("progressPlayerNum 숫자가 맞지않기에 SETTING 페이즈에 머무릅니다.")
    public void endSettingPhaseFail() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(gameRoom::joinPlayer);

        gameRoom.updatePhase(GameRoom.Phase.SETTING);

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.endSettingPhase(gameRoom.getId(), 10))
                .withMessageMatching(GameExceptionCode.INVALID_PLAYER.getMessage());

        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SETTING);
    }

    @RepeatedTest(5)
    @DisplayName("GameRoom의 블록을 Player에게 알맞게 전달합니다.")
    public void drawBlockTestSuccess() throws Exception {
        //given
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        int whiteBlockCount = 1;
        int blackBlockCount = 3;

        gameRoom.gameReset();

        //when
        gameService.drawBlockAtStart(gameRoom.getId(), player.getId(), whiteBlockCount, blackBlockCount);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();
        assertThat(findPlayer.getBlockList().size()).isEqualTo(whiteBlockCount + blackBlockCount);

        assertThat(howManyWhiteBlock(findPlayer.getBlockList())).isEqualTo(whiteBlockCount);
        assertThat(howManyBlackBlock(findPlayer.getBlockList())).isEqualTo(blackBlockCount);

        findPlayer.getBlockList()
                .forEach(block -> assertThat(block.getNum()).isBetween(0, 12));

        System.out.println(findPlayer.getBlockList().stream()
                .map(block -> block.getBlockCode(true))
                .collect(Collectors.toList()).toString());
    }

    @Test
    @DisplayName("뽑으려는 블록의 개수가 비정상적일때 익셉션이 발생합니다.")
    public void drawBlockTestFail_InvalidNumberOfBlocks() throws Exception {
        //given
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        int whiteBlockCount = 2;
        int blackBlockCount = 3;

        gameRoom.joinPlayer(player);
        gameRoom.gameReset();

        gameRoom.updatePhase(GameRoom.Phase.START);

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.drawBlockAtStart(gameRoom.getId(), player.getId(), whiteBlockCount, blackBlockCount))
                .withMessageMatching(GameExceptionCode.INVALID_NUMBER_OF_BLOCKS.getMessage());
    }

    @Test
    @DisplayName("START 페이즈에서 DRAW 페이즈로 정상적으로 넘어가져야 합니다.")
    public void endStartPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(player -> {
                    player.updateReady(true);
                    gameRoom.joinPlayer(player);
                });

        gameRoom.updatePhase(GameRoom.Phase.START);

        //when
        gameService.endStartPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.DRAW);
        assertThat(findGameRoom.getWhiteBlockList().stream().anyMatch(Block::isJoker)).isTrue();
    }

    @Test
    @DisplayName("조건에 맞지않기에 START 페이즈에 머무릅니다.")
    public void endStartPhaseFail() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(gameRoom::joinPlayer);

        gameRoom.randomSetPlayerOrder();

        gameRoom.updatePhase(GameRoom.Phase.START);

        //when
        gameService.endStartPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.START);
        assertThat(findGameRoom.getWhiteBlockList().stream().noneMatch(Block::isJoker)).isTrue();
    }

    @Test
    @DisplayName("Player가 GameRoom에 있는 지정한 색깔의 블럭을 하나 가져옵니다.")
    public void drawBlockAtDrawPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);
        gameRoom.gameReset();
        gameRoom.addJoker();
        gameRoom.updatePhase(GameRoom.Phase.DRAW);

        //when
        gameService.drawBlockAtDrawPhase(gameRoom.getId(), player.getId(), BlockColor.WHITE);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findPlayer.getBlockList().size()).isEqualTo(1);
        assertThat(findPlayer.getBlockList().get(0).isColor(BlockColor.WHITE)).isTrue();
        assertThat(findPlayer.getDrawBlockIndexNum()).isEqualTo(0);
        assertThat(findPlayer.isReady()).isTrue();
    }

    @Test
    @DisplayName("플레이어가 자동으로 블럭을 하나 가져오는데 성공합니다.")
    public void autoDrawAtDrawPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);
        gameRoom.gameReset();
        gameRoom.addJoker();
        gameRoom.updatePhase(GameRoom.Phase.DRAW);

        //when
        gameService.autoDrawAtDrawPhase(gameRoom.getId());

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findPlayer.getBlockList().size()).isEqualTo(1);
        assertThat(findPlayer.getDrawBlockIndexNum()).isEqualTo(0);
        assertThat(findPlayer.isReady()).isTrue();
    }

    @Test
    @DisplayName("DRAW 페이즈가 정상적으로 SORT 페이즈로 넘어갑니다.")
    public void endDrawPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);
        gameRoom.updatePhase(GameRoom.Phase.DRAW);
        player.updateReady(true);

        //when
        gameService.endDrawPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SORT);
        assertThat(findGameRoom.getProgressPlayer().isReady()).isFalse();
    }

    @Test
    @DisplayName("해당 Player의 Joker의 위차가 정상적으로 변경됩니다.")
    public void updatePlayerJokerSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();
        gameRoom.joinPlayer(player);
        IntStream.range(0, 4).forEach(i -> player.addBlock(gameRoom.drawRandomBlock(BlockColor.WHITE)));
        player.addBlock(Block.create(BlockColor.WHITE, 12));

        gameRoom.updatePhase(GameRoom.Phase.SORT);

        //when
        gameService.updatePlayerJoker(player.getId(), 0, BlockColor.WHITE);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findPlayer.getBlockList().get(0).isColor(BlockColor.WHITE)).isTrue();
        assertThat(findPlayer.getBlockList().get(0).getNum()).isEqualTo(12);
        assertThat(findPlayer.getWhiteJokerRange() / 100).isEqualTo(0);
    }

    @Test
    @DisplayName("추리에 성공하면 해당 Block의 Status가 OPEN으로 변경되고, 추리에 성공한 Player의 ready가 true가 됩니다.")
    public void guessBlockSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        Player targetPlayer = playerRepository.save(Player.create("bar", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player);

        gameRoom.joinPlayer(targetPlayer);
        IntStream.range(0, 4).forEach(i -> targetPlayer.addBlock(Block.create(BlockColor.WHITE, i)));

        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        //when
        gameService.guessBlock(player.getId(), targetPlayer.getId(), 0, 0);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();
        Player findTargetPlayer = playerRepository.findById(targetPlayer.getId()).get();

        assertThat(findTargetPlayer.getBlockList().get(0).isClose()).isFalse();
        assertThat(findPlayer.isReady()).isTrue();
    }

    @Test
    @DisplayName("추리에 실패하면 실패한 Player의 ready는 false를 유지합니다.")
    public void guessBlockFail() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        Player targetPlayer = playerRepository.save(Player.create("bar", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player);

        gameRoom.joinPlayer(targetPlayer);
        IntStream.range(0, 4).forEach(i -> targetPlayer.addBlock(Block.create(BlockColor.WHITE, i)));

        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        //when
        gameService.guessBlock(player.getId(), targetPlayer.getId(), 0, 5);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();
        Player findTargetPlayer = playerRepository.findById(targetPlayer.getId()).get();

        assertThat(findTargetPlayer.getBlockList().get(0).isClose()).isTrue();
        assertThat(findPlayer.isReady()).isFalse();
    }

    @Test
    @DisplayName("추리에 성공해 targetPlayer의 모든 Block이 OPNE 된 경우 해당 Player를 retire 시킵니다.")
    public void RetireTargetPlayer() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        Player targetPlayer = playerRepository.save(Player.create("bar", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player);

        gameRoom.joinPlayer(targetPlayer);
        targetPlayer.addBlock(Block.create(BlockColor.WHITE, 0));

        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        //when
        gameService.guessBlock(player.getId(), targetPlayer.getId(), 0, 0);

        //then
        Player findTargetPlayer = playerRepository.findById(targetPlayer.getId()).get();

        assertThat(findTargetPlayer.getBlockList().get(0).isClose()).isFalse();
        assertThat(findTargetPlayer.isRetire()).isTrue();
    }

    @Test
    @DisplayName("추리에 성공한(ready == true) Player는 REPEAT 페이즈로 넘어가며, 최근 뽑은 Block이 공개되지 않습니다.")
    public void endGuessPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player targetPlayer = playerRepository.save(Player.create("foo2", "sessionId"));
        Player otherPlayer = playerRepository.save(Player.create("foo1", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(targetPlayer);
        gameRoom.joinPlayer(otherPlayer);
        targetPlayer.addBlock(Block.create(BlockColor.BLACK, 0));
        targetPlayer.addBlock(Block.create(BlockColor.BLACK, 1));
        targetPlayer.updateReady(true);

        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        //when
        gameService.endGuessPhase(gameRoom.getId(),gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        Player findPlayer = playerRepository.findById(targetPlayer.getId()).get();

        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.REPEAT);
        assertThat(findPlayer.getBlockList().get(findPlayer.getDrawBlockIndexNum()).isClose()).isTrue();
        assertThat(findPlayer.isReady()).isFalse();
    }

    @Test
    @DisplayName("추리에 실패하거나 조작을 하지않은(ready == false) Player는 END 페이즈로 넘어가며, 최근 뽑은 Block이 공개됩니다.")
    public void endGuessPhaseFail() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player1 = playerRepository.save(Player.create("foo1", "sessionId1"));
        Player player2 = playerRepository.save(Player.create("foo2", "sessionId2"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player1);
        gameRoom.joinPlayer(player2);
        player1.addBlock(Block.create(BlockColor.BLACK, 0));
        player1.updateReady(false);

        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        //when
        gameService.endGuessPhase(gameRoom.getId(),gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        Player findPlayer = playerRepository.findById(player1.getId()).get();

        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.END);
        assertThat(findPlayer.getBlockList().get(findPlayer.getDrawBlockIndexNum()).isClose()).isFalse();
        assertThat(findPlayer.isReady()).isFalse();
    }

    @Test
    @DisplayName("추리에 성공하여 자신 이외에 모든 플레이어가 retire 했다면 isGameOver를 true로 출력합니다.")
    public void endGuessPhaseGameOver() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));
        player.updateReady(true);

        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        //when
        gameService.endGuessPhase(gameRoom.getId(),gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.isGameOver()).isTrue();
    }

    @Test
    @DisplayName("REPEAT 페이즈에서 추리를 더 하기 원한다면, GUESS 페이즈로 넘어갑니다.")
    public void endRepeatPhaseTrue() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));

        gameRoom.updatePhase(GameRoom.Phase.REPEAT);

        //when
        gameService.endRepeatPhase(gameRoom.getId(), player.getOrderNumber(), true);

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();


        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.GUESS);
        assertThat(findGameRoom.getProgressPlayer().getBlockList().get(0).isClose()).isTrue();
    }

    @Test
    @DisplayName("REPEAT 페이즈에서 추리를 그만둔다면, END 페이즈로 넘어갑니다.")
    public void endRepeatPhaseFalse() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));

        gameRoom.updatePhase(GameRoom.Phase.REPEAT);

        //when
        gameService.endRepeatPhase(gameRoom.getId(), player.getOrderNumber(), false);

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.END);
        assertThat(findGameRoom.getProgressPlayer().getBlockList().get(0).isClose()).isTrue();
    }

    @Test
    @DisplayName("END 페이즈가 끝나고 진행중인 플레이어가 바뀌며 DRAW 페이즈로 돌아갑니다.")
    public void endEndPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        gameRoom.gameReset();

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(gameRoom::joinPlayer);

        gameRoom.randomSetPlayerOrder();

        gameRoom.updatePhase(GameRoom.Phase.END);

        //when
        gameService.endEndPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getProgressPlayerNumber()).isEqualTo(1);
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.DRAW);
    }

    @Test
    @DisplayName("GAMEOVER 페이즈가 끝나면 현재 게임에 대한 정보가 초기화되고 WAIT 페이즈로 넘어갑니다.")
    public void endGameOverPhase() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));
        player.updateReady(true);
        player.updateOrder(1);
        player.guessBlock(0, 0);

        gameRoom.updatePhase(GameRoom.Phase.GAMEOVER);

        //when
        gameService.endGameOverPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findGameRoom.getProgressPlayerNumber()).isEqualTo(0);
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.WAIT);

        assertThat(findPlayer.getBlockList().size()).isEqualTo(0);
        // assertThat(findPlayer.getOrderNumber()).isEqualTo(0);
        assertThat(findPlayer.getDrawBlockIndexNum()).isEqualTo(-1);
        assertThat(findPlayer.isRetire()).isFalse();
        assertThat(findPlayer.isReady()).isFalse();
        assertThat(findPlayer.getWhiteJokerRange()).isEqualTo(12);
        assertThat(findPlayer.getBlackJokerRange()).isEqualTo(12);
    }

    @Test
    @DisplayName("시작중이지 않은 GameRoom들을 페이징해서 가져오는데 성공합니다.")
    public void findGameRoomFindSuccess() throws Exception {
        //given
        IntStream.range(0, 10)
                .forEach(i -> gameRoomRepository.save(GameRoom.create("foo" + i)));

        //when
        GameRoomFind gameRoomFind = gameService.findGameRoomsNotGameStart(0, 20);

        //then
        assertThat(gameRoomFind.getGameRoomSimpleList().size()).isEqualTo(10);
    }

    @Test
    @DisplayName("시작한 것과 아닌것이 각각 5개씩 있을 때 시작하지 않은 GameRoom만 5개 가져옵니다.")
    public void findGameRoomFindOnlyNotGameStart() throws Exception {
        //given
        IntStream.range(0, 5)
                .forEach(i -> gameRoomRepository.save(GameRoom.create("foo" + i)));

        IntStream.range(0, 5)
                .forEach(i -> gameRoomRepository.save(GameRoom.create("foo" + i)).updatePhase(GameRoom.Phase.START));

        //when
        GameRoomFind gameRoomFind = gameService.findGameRoomsNotGameStart(0, 20);

        //then
        assertThat(gameRoomFind.getGameRoomSimpleList().size()).isEqualTo(5);
    }

    @Test
    @DisplayName("Player는 GameRoom에서 정상적으로 나가지며, Player가 아무도 없으면 GameRoom을 삭제합니다.")
    public void exitGameRoomSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);

        //when
        gameService.exitGameRoom(player.getWebSocketSessionId());

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findPlayer.getGameRoom()).isEqualTo(null);
        assertThat(gameRoomRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("GameRoom에 혼자 참가중인 Player가 연결이 끊기면 Player와 비어있는 GameRoom을 제거합니다.")
    public void disconnectWebSessionAtJoinGameOnlyOne() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);

        //when
        gameService.disconnectWebSession(player.getWebSocketSessionId());

        //then
        assertThat(playerRepository.findAll().size()).isEqualTo(0);
        assertThat(gameRoomRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("게임이 시작된 GameRoom안의 Player가 연결이 끊기면 패배처리를 합니다..")
    public void disconnectWebSessionAtGameStart() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player1 = playerRepository.save(Player.create("foo1", "sessionId1"));
        Player player2 = playerRepository.save(Player.create("foo2", "sessionId2"));

        gameRoom.joinPlayer(player1);
        gameRoom.joinPlayer(player2);

        gameRoom.updatePhase(GameRoom.Phase.START);

        //when
        gameService.disconnectWebSession(player2.getWebSocketSessionId());

        //then
        Player disconnectPlayer = playerRepository.findById(player2.getId()).get();

        assertThat(disconnectPlayer.getName()).isEqualTo("disconnect");
        assertThat(disconnectPlayer.getWebSocketSessionId()).isEqualTo("disconnect");
        assertThat(disconnectPlayer.isRetire()).isTrue();
    }

    @Test
    @DisplayName("GameRoom안의 Player가 연결이 끊긴 상태로 게임이 끝나면 해당 Player를 강제로 추방시키고 이후 데이터를 삭제합니다.")
    public void disconnectWebSessionAtGameOver() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player1 = playerRepository.save(Player.create("foo1", "sessionId1"));
        Player player2 = playerRepository.save(Player.create("foo2", "sessionId2"));

        gameRoom.joinPlayer(player1);
        gameRoom.joinPlayer(player2);

        gameService.disconnectWebSession(player2.getWebSocketSessionId());

        gameRoom.updatePhase(GameRoom.Phase.GAMEOVER);

        //when
        gameService.endGameOverPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPlayerList().size()).isEqualTo(1);
        assertThat(playerRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 동시성 문제 체크를 위해
    @DisplayName("요청이 여러번 들어와도 autoDraw 기능이 1번에 한해 성공적으로 이루어집니다.")
    public void autoDrawAtStartSuccess() throws Exception {
        //given
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));

        gameRoom.gameReset();
        gameRoom.joinPlayer(player);

        gameRoom.updatePhase(GameRoom.Phase.START);

        // 트랜잭션이 안끝나서 변경감지가 안먹히는듯. 수동으로 변경사항 저장.
        gameRoomRepository.save(gameRoom);
        playerRepository.save(player);

        Long gameRoomId = gameRoom.getId();

        //when
        try {
            IntStream.range(0, 4)
                    .parallel()
                    .forEach(i -> gameService.autoDrawAtStart(gameRoomId));
        } catch (CannotAcquireLockException e) {

        }

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();
        assertThat(findPlayer.getBlockList().size()).isEqualTo(4);
        assertThat(findPlayer.isReady()).isTrue();

        findPlayer.getBlockList()
                .forEach(block -> System.out.println(block.getBlockCode(true)));

        //after
        gameRoomRepository.delete(gameRoom);
        playerRepository.deleteAll();;
    }

    private long howManyWhiteBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.WHITE)).count();
    }

    private long howManyBlackBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.BLACK)).count();
    }
}