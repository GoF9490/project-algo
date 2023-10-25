package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class PlayerServiceTest {

    @Autowired private PlayerService playerService;
    @Autowired private GameRoomRepository gameRoomRepository;
    @Autowired private PlayerRepository playerRepository;

    @Test
    @DisplayName("Player를 생성하고 저장, Id로 찾기가 정상적으로 이루어져야 합니다.")
    public void createPlayerAndFindPlayerByIdSuccess() throws Exception {
        //given
        String name = "foo";
        String webSocketSessionId = "sessionId";

        //when
        Long playerId = playerService.create(name, webSocketSessionId);
        Player player = playerService.findById(playerId);

        //then
        assertThat(player.getId()).isEqualTo(playerId);
        assertThat(player.getName()).isEqualTo(name);
        assertThat(player.getWebSocketSessionId()).isEqualTo(webSocketSessionId);
    }

    @Test
    @DisplayName("존재하지 않는 Player의 Id를 조회하려 하면 알맞은 익셉션이 발생합니다.")
    public void findPlayerByIdFail() throws Exception {
        //given
        Long playerId = 15L;

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> playerService.findById(playerId))
                .withMessageMatching(GameExceptionCode.PLAYER_NOT_FOUND.getMessage());
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
        playerService.updatePlayerReady(player.getWebSocketSessionId(), true);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();
        assertThat(findPlayer.isReady()).isTrue();
    }

    @Test
    @DisplayName("Player가 GameRoom에 정상적으로 참가되어야 합니다.")
    public void joinGameRoomSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        //when
        playerService.joinGameRoom(player.getWebSocketSessionId(), gameRoom.getId());

        //then
        GameRoom findGameRoom = gameRoomRepository.findById(gameRoom.getId()).get();

        assertThat(findGameRoom.getPlayerList().size()).isEqualTo(1);
        assertThat(findGameRoom.getPlayerList().get(0).getId()).isEqualTo(player.getId());
    }

    @Test
    @DisplayName("GameRoom에 참가자가 4명이상 있을경우 익셉션이 발생합니다.")
    public void joinGameRoomFail() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        LongStream.range(0, 4)
                .mapToObj(l -> playerRepository.save(Player.create("foo" + l, "sessionId" + l)).getWebSocketSessionId())
                .forEach(sessionId -> playerService.joinGameRoom(sessionId, gameRoom.getId()));
        String latePlayerId = playerRepository.save(Player.create("lastPlayer", "sessionId")).getWebSocketSessionId();

        //expect
        assertThatExceptionOfType(GameLogicException.class)
                .isThrownBy(() -> playerService.joinGameRoom(latePlayerId, gameRoom.getId()))
                .withMessageMatching(GameExceptionCode.GAME_ROOM_IS_FULL.getMessage());
    }

    @Test
    @DisplayName("Player는 GameRoom에서 정상적으로 나가지며, Player가 아무도 없으면 GameRoom을 삭제합니다.")
    public void exitGameRoomSuccess() throws Exception {
        //given
        GameRoom gameRoom = gameRoomRepository.save(GameRoom.create("GameRoom"));
        Player player = playerRepository.save(Player.create("foo", "sessionId"));

        gameRoom.joinPlayer(player);

        //when
        playerService.exitGameRoom(player.getWebSocketSessionId());

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
        playerService.disconnectWebSession(player.getWebSocketSessionId());

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
        playerService.disconnectWebSession(player2.getWebSocketSessionId());

        //then
        Player disconnectPlayer = playerRepository.findById(player2.getId()).get();

        assertThat(disconnectPlayer.getName()).isEqualTo("disconnect");
        assertThat(disconnectPlayer.getWebSocketSessionId()).isEqualTo("disconnect");
        assertThat(disconnectPlayer.isRetire()).isTrue();
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
        player.joinGameRoom(gameRoom);

        //when
        playerService.drawBlockAtStart(player.getId(), whiteBlockCount, blackBlockCount);

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
                .isThrownBy(() -> playerService.drawBlockAtStart(player.getId(), whiteBlockCount, blackBlockCount))
                .withMessageMatching(GameExceptionCode.INVALID_NUMBER_OF_BLOCKS.getMessage());
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
        playerService.drawBlockAtDrawPhase(player.getId(), BlockColor.WHITE);

        //then
        Player findPlayer = playerRepository.findById(player.getId()).get();

        assertThat(findPlayer.getBlockList().size()).isEqualTo(1);
        assertThat(findPlayer.getBlockList().get(0).isColor(BlockColor.WHITE)).isTrue();
        assertThat(findPlayer.getDrawBlockIndexNum()).isEqualTo(0);
        assertThat(findPlayer.isReady()).isTrue();
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
        playerService.updatePlayerJoker(player.getId(), 0, BlockColor.WHITE);

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
        playerService.guessBlock(player.getId(), targetPlayer.getId(), 0, 0);

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
        playerService.guessBlock(player.getId(), targetPlayer.getId(), 0, 5);

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
        playerService.guessBlock(player.getId(), targetPlayer.getId(), 0, 0);

        //then
        Player findTargetPlayer = playerRepository.findById(targetPlayer.getId()).get();

        assertThat(findTargetPlayer.getBlockList().get(0).isClose()).isFalse();
        assertThat(findTargetPlayer.isRetire()).isTrue();
    }


    private long howManyWhiteBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.WHITE)).count();
    }

    private long howManyBlackBlock(List<Block> BlockList) {
        return BlockList.stream().filter(block -> block.isColor(BlockColor.BLACK)).count();
    }
}