package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.response.GameRoomFind;
import com.game.algo.algo.dto.response.GameRoomSimple;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
@Transactional
class GameRoomServiceTest {

    @Autowired private GameRoomService gameRoomService;
    @Autowired private GameRoomRepository gameRoomRepository;
    @Autowired private PlayerRepository playerRepository;

    @Test
    @DisplayName("GameRoom을 생성하고 저장, Id로 찾기가 정상적으로 이루어져야 합니다.")
    public void createGameRoomAndFindGameRoomByIdSuccess() throws Exception {
        //when
        Long gameRoomId = gameRoomService.create("GameRoom");
        GameRoom gameRoom = gameRoomService.findById(gameRoomId);

        //then
        assertThat(gameRoom.getId()).isEqualTo(gameRoomId);
    }

    @Test
    @DisplayName("존재하지 않는 GameRoom의 Id를 조회하려 하면 알맞은 익셉션이 발생합니다.")
    public void findGameRoomByIdFail() throws Exception {
        //given
        Long gameRoomId = 15L;

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameRoomService.findById(gameRoomId))
                .withMessageMatching(GameExceptionCode.GAME_ROOM_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("시작한 것과 아닌것이 각각 5개씩 있을 때 시작하지 않은 GameRoom만 5개 가져옵니다.")
    public void findSimpleListByStart() throws Exception {
        //given
        IntStream.range(0, 5)
                .forEach(i -> gameRoomRepository.save(GameRoom.create("foo" + i)));

        IntStream.range(0, 5)
                .forEach(i -> gameRoomRepository.save(GameRoom.create("foo" + i)).updatePhase(GameRoom.Phase.START));

        //when
        List<GameRoomSimple> simpleList = gameRoomService.findSimpleListByStart(0, false);

        //then
        assertThat(simpleList.size()).isEqualTo(5);
    }

    @Test
    @DisplayName("GameRoom을 삭제합니다.")
    public void deleteById() throws Exception {
        //given
        Long gameRoomId = gameRoomService.create("GameRoom");

        //when
        gameRoomService.deleteById(gameRoomId);

        //then
        assertThat(gameRoomRepository.findById(gameRoomId).isEmpty()).isTrue();
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
        gameRoomService.gameStart(gameRoom.getId());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.areAllPlayersReady()).isFalse();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SETTING);
    }

    @Test
    @DisplayName("플레이어가 한명일경우 게임을 시작할 수 없습니다.")
    public void gameStartFailCase1() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));
        player.updateReady(true);
        gameRoom.joinPlayer(player);

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameRoomService.gameStart(gameRoom.getId()))
                .withMessageMatching(GameExceptionCode.LACK_OF_PLAYER.getMessage());
    }

    @Test
    @DisplayName("모든 플레이어가 준비하지 않으면 게임을 시작할 수 없습니다.")
    public void gameStartFailCase2() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        IntStream.range(0, 4)
                .mapToObj(i -> playerRepository.save(Player.create("player" + i, "sessionId")))
                .forEach(gameRoom::joinPlayer);

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> gameRoomService.gameStart(gameRoom.getId()))
                .withMessageMatching(GameExceptionCode.PLAYER_NOT_READY.getMessage());
    }

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
        gameRoomService.endSettingPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

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
                .isThrownBy(() -> gameRoomService.endSettingPhase(gameRoom.getId(), 10))
                .withMessageMatching(GameExceptionCode.OUT_OF_SYNC_GAME_PHASE.getMessage());

        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SETTING);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 동시성 문제 체크를 위해
    @DisplayName("요청이 여러번 들어와도 autoDraw 기능이 1번에 한해 성공적으로 이루어집니다.")
    public void autoProgressAtStartPhase() throws Exception {
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
                    .forEach(i -> gameRoomService.autoProgressAtStartPhase(gameRoomId));
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
        gameRoomService.endStartPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

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
        gameRoomService.endStartPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.START);
        assertThat(findGameRoom.getWhiteBlockList().stream().noneMatch(Block::isJoker)).isTrue();
    }

    @Test
    @DisplayName("플레이어가 자동으로 블럭을 하나 가져오는데 성공합니다.")
    public void autoProgressAtDrawPhaseSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);
        gameRoom.gameReset();
        gameRoom.addJoker();
        gameRoom.updatePhase(GameRoom.Phase.DRAW);

        //when
        gameRoomService.autoProgressAtDrawPhase(gameRoom.getId());

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
        gameRoomService.endDrawPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.SORT);
        assertThat(findGameRoom.getProgressPlayer().isReady()).isFalse();
    }

    @Test
    void endSortPhase() {
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
        gameRoomService.endGuessPhase(gameRoom.getId(),gameRoom.getProgressPlayerNumber());

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
        gameRoomService.endGuessPhase(gameRoom.getId(),gameRoom.getProgressPlayerNumber());

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
        gameRoomService.endGuessPhase(gameRoom.getId(),gameRoom.getProgressPlayerNumber());

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
        gameRoomService.endRepeatPhase(gameRoom.getId(), player.getOrderNumber(), true);

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
        gameRoomService.endRepeatPhase(gameRoom.getId(), player.getOrderNumber(), false);

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
        gameRoomService.endEndPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

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
        gameRoomService.endGameOverPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findGameRoom.getProgressPlayerNumber()).isEqualTo(0);
        assertThat(findGameRoom.getPhase()).isEqualTo(GameRoom.Phase.WAIT);

        assertThat(findPlayer.getBlockList().size()).isEqualTo(0);
        assertThat(findPlayer.getDrawBlockIndexNum()).isEqualTo(-1);
        assertThat(findPlayer.isRetire()).isFalse();
        assertThat(findPlayer.isReady()).isFalse();
        assertThat(findPlayer.getWhiteJokerRange()).isEqualTo(12);
        assertThat(findPlayer.getBlackJokerRange()).isEqualTo(12);
    }

    @Test
    @DisplayName("GameRoom안의 Player가 연결이 끊긴 상태로 게임이 끝나면 해당 Player를 강제로 추방시키고 이후 데이터를 삭제합니다.")
    public void banDisconnectPlayerAtGameOver() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player1 = playerRepository.save(Player.create("foo1", "sessionId1"));
        Player player2 = playerRepository.save(Player.create("foo2", "sessionId2"));

        gameRoom.joinPlayer(player1);
        gameRoom.joinPlayer(player2);

        player2.disconnect();

        gameRoom.updatePhase(GameRoom.Phase.GAMEOVER);

        //when
        gameRoomService.endGameOverPhase(gameRoom.getId(), gameRoom.getProgressPlayerNumber());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPlayerList().size()).isEqualTo(1);
        assertThat(playerRepository.findAll().size()).isEqualTo(1);
    }
}