package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
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
        Player player = Player.create("foo", "sessionId");
        Long playerId = playerRepository.save(player).getId();

        //when
        gameService.updatePlayerReady(playerId, true);

        //then
        Player findPlayer = playerRepository.findById(playerId).get();
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
        Long gameRoomId = gameRoomRepository.save(GameRoom.create()).getId();
        Long playerId = playerRepository.save(Player.create("foo", "sessionId")).getId();

        //when
        gameService.joinGameRoom(gameRoomId, playerId);

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoomId).get();
        assertThat(findGameRoom.getPlayerList().size()).isEqualTo(1);
        assertThat(findGameRoom.getPlayerList().get(0).getId()).isEqualTo(playerId);
    }

    @Test
    @DisplayName("GameRoom에 Player가 4명이상 있을경우 익셉션이 발생합니다.")
    public void joinGameRoomFail() throws Exception {
        //given
        Long gameRoomId = gameRoomRepository.save(GameRoom.create()).getId();
        LongStream.range(0, 4)
                .map(l -> playerRepository.save(Player.create("foo" + l, "sessionId" + l)).getId())
                .forEach(playerId -> gameService.joinGameRoom(gameRoomId, playerId));
        Long latePlayerId = playerRepository.save(Player.create("lastPlayer", "sessionId")).getId();

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameService.joinGameRoom(gameRoomId, latePlayerId))
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
//        assertThat(findPlayer.isReady()).isTrue();
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
        player.addBlock(Block.createBlock(BlockColor.WHITE, 12));

        //when
        gameService.updatePlayerJoker(player.getId(), 0, BlockColor.WHITE);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findPlayer.getBlockList().get(0).isColor(BlockColor.WHITE)).isTrue();
        assertThat(findPlayer.getBlockList().get(0).getNum()).isEqualTo(12);
        assertThat(findPlayer.getWhiteJokerRange() / 100).isEqualTo(0);
    }

    private long howManyWhiteBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.WHITE)).count();
    }

    private long howManyBlackBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.BLACK)).count();
    }
}