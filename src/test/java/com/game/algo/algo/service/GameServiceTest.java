package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerJpaRepository;
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

    @Autowired private PlayerJpaRepository playerRepository;

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());

        gameRoom.updatePhase(GameRoom.Phase.SETTING);

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(player -> {
                    player.updateReady(true);
                    gameRoom.joinPlayer(player);
                });

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());

        gameRoom.updatePhase(GameRoom.Phase.SETTING);

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(gameRoom::joinPlayer);

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.endSettingPhase(gameRoom.getId(), 10))
                .withMessageMatching(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE.getMessage());

        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SETTING);

    }

    @RepeatedTest(5)
    @DisplayName("GameRoom의 블록을 Player에게 알맞게 전달합니다.")
    public void drawBlockTestSuccess() throws Exception {
        //given
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());

        int whiteBlockCount = 2;
        int blackBlockCount = 3;

        gameRoom.joinPlayer(player);
        gameRoom.gameReset();

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.drawBlockAtStart(gameRoom.getId(), player.getId(), whiteBlockCount, blackBlockCount))
                .withMessageMatching(GameExceptionCode.INVALID_NUMBER_OF_BLOCKS.getMessage());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 동시성 문제 체크를 위해
    @DisplayName("요청이 여러번 들어와도 autoDraw 기능이 1번에 한해 성공적으로 이루어집니다.")
    public void autoDrawAtStartSuccess() throws Exception {
        //given
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());

        gameRoom.gameReset();
        gameRoom.joinPlayer(player);

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
    }

    @Test
    @DisplayName("START 페이즈에서 DRAW 페이즈로 정상적으로 넘어가져야 합니다.")
    public void endStartPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());

        gameRoom.updatePhase(GameRoom.Phase.START);

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(player -> {
                    player.updateReady(true);
                    gameRoom.joinPlayer(player);
                });

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());

        gameRoom.updatePhase(GameRoom.Phase.START);

        IntStream.range(0, 2)
                .mapToObj(i -> playerRepository.save(Player.create("foo" + i, "sessionId")))
                .forEach(gameRoom::joinPlayer);

        gameRoom.playerOrderReset();

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.SORT);
        gameRoom.joinPlayer(player);
        IntStream.range(0, 4).forEach(i -> player.addBlock(gameRoom.drawRandomBlock(BlockColor.WHITE)));
        player.addBlock(Block.create(BlockColor.WHITE, 12));

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        Player targetPlayer = playerRepository.save(Player.create("bar", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        gameRoom.joinPlayer(player);

        gameRoom.joinPlayer(targetPlayer);
        IntStream.range(0, 4).forEach(i -> targetPlayer.addBlock(Block.create(BlockColor.WHITE, i)));

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        Player targetPlayer = playerRepository.save(Player.create("bar", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        gameRoom.joinPlayer(player);

        gameRoom.joinPlayer(targetPlayer);
        IntStream.range(0, 4).forEach(i -> targetPlayer.addBlock(Block.create(BlockColor.WHITE, i)));

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        Player targetPlayer = playerRepository.save(Player.create("bar", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        gameRoom.joinPlayer(player);

        gameRoom.joinPlayer(targetPlayer);
        targetPlayer.addBlock(Block.create(BlockColor.WHITE, 0));

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player targetPlayer = playerRepository.save(Player.create("foo2", "sessionId"));
        Player otherPlayer = playerRepository.save(Player.create("foo1", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        gameRoom.joinPlayer(targetPlayer);
        gameRoom.joinPlayer(otherPlayer);
        targetPlayer.addBlock(Block.create(BlockColor.BLACK, 0));
        targetPlayer.addBlock(Block.create(BlockColor.BLACK, 1));
        targetPlayer.updateReady(true);

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));
        player.updateReady(false);

        //when
        gameService.endGuessPhase(gameRoom.getId(),gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.END);
        assertThat(findPlayer.getBlockList().get(findPlayer.getDrawBlockIndexNum()).isClose()).isFalse();
        assertThat(findPlayer.isReady()).isFalse();
    }

    @Test
    @DisplayName("추리에 성공하여 자신 이외에 모든 플레이어가 retire 했다면 isGameOver를 true로 출력합니다.")
    public void endGuessPhaseGameOver() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.GUESS);

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));
        player.updateReady(true);

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.REPEAT);

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));

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
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create());
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.gameReset();
        gameRoom.updatePhase(GameRoom.Phase.REPEAT);

        gameRoom.joinPlayer(player);
        player.addBlock(Block.create(BlockColor.BLACK, 0));

        //when
        gameService.endRepeatPhase(gameRoom.getId(), player.getOrderNumber(), false);

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.END);
        assertThat(findGameRoom.getProgressPlayer().getBlockList().get(0).isClose()).isTrue();
    }

    private long howManyWhiteBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.WHITE)).count();
    }

    private long howManyBlackBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.BLACK)).count();
    }
}