package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

class GameRoomTest {

    @Test
    @DisplayName("게임 리셋 후 페이즈는 READY 여야 한다.")
    public void checkTypeAtGameReset() throws Exception {
        //given
        GameRoom gameRoom = new GameRoom();

        //when
        gameRoom.gameReset();

        //then
        assertThat(gameRoom.getProgressPlayerNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("게임 리셋 후 흰 / 검은 블록은 1~12까지 생성되어야 한다.(12는 JOKER)")
    public void checkAllBlocksNumberAtGameReset() throws Exception {
        //given
        GameRoom gameRoom = new GameRoom();

        //when
        gameRoom.gameReset();

        //then
        IntStream.range(0, 13).forEach(i -> {
            assertThat(gameRoom.getWhiteBlockList().get(i).getNum()).isEqualTo(i);
            assertThat(gameRoom.getBlackBlockList().get(i).getNum()).isEqualTo(i);
        });
    }

    @Test
    @DisplayName("게임 리셋 후 흰 / 검은 블록은 각각 whiteBlockList / blackBlockList 에 담겨있어야 한다.")
    public void checkAllBlocksTypeAtGameReset() throws Exception {
        //given
        GameRoom gameRoom = new GameRoom();

        //when
        gameRoom.gameReset();

        //then
        IntStream.range(0, 13).forEach(i -> {
            if (i == 12) {
                assertThat(gameRoom.getWhiteBlockList().get(i).getTypeNumber()).isEqualTo(1);
                assertThat(gameRoom.getBlackBlockList().get(i).getTypeNumber()).isEqualTo(2);
            } else {
                assertThat(gameRoom.getWhiteBlockList().get(i).getTypeNumber()).isEqualTo(3);
                assertThat(gameRoom.getBlackBlockList().get(i).getTypeNumber()).isEqualTo(4);
            }
        });
    }

    @Test // @RepeatedTest(13)
    @DisplayName("12번의 블록을 랜덤으로 뽑으면 모든 숫자들은 1~12사이의 숫자여야하며, 뽑힌 숫자는 중복되지 않고, GameRoom 의 리스트는 비어있어야 한다.")
    public void drawRandomBlockTest() throws Exception {
        //given
        GameRoom gameRoom = new GameRoom();
        gameRoom.gameReset();

        List<Integer> whiteBlockNumList = new ArrayList<>();
        List<Integer> blackBlockNumList = new ArrayList<>();

        //except
        IntStream.range(0, 13).forEach(i -> {
            Block whiteBlock = gameRoom.drawRandomBlock(BlockColor.WHITE);
            Block blackBlock = gameRoom.drawRandomBlock(BlockColor.BLACK);

            assertThat(whiteBlock.getNum()).isBetween(0, 12);
            assertThat(blackBlock.getNum()).isBetween(0, 12);

            assertThat(whiteBlockNumList.contains(whiteBlock.getNum())).isFalse();
            assertThat(blackBlockNumList.contains(blackBlock.getNum())).isFalse();

            whiteBlockNumList.add(whiteBlock.getNum());
            blackBlockNumList.add(blackBlock.getNum());
        });

        assertThat(gameRoom.getWhiteBlockList().size()).isEqualTo(0);
        assertThat(gameRoom.getBlackBlockList().size()).isEqualTo(0);
    }

    @Test
    @DisplayName("GameRoom 의 playerList 를 토대로 playerOrder 순서를 랜덤으로 정한다.")
    public void playerOrderResetTest() throws Exception {
        //given
        GameRoom gameRoom = new GameRoom();
        IntStream.range(1, 5)
                .mapToObj(i -> Player.create("player" + i, null))
                .forEach(gameRoom::joinPlayer);

        //when
        gameRoom.playerOrderReset();

        //then
//        gameManager.getPlayerList().stream()
//                .forEach(player -> System.out.println("player : " + player.getName()));
//        gameManager.getPlayerOrder().stream()
//                .forEach(player -> System.out.println("order : " + player.getName()));

        gameRoom.getPlayerList().stream()
                .forEach(player -> assertThat(player.getOrderNumber()).isBetween(1, 4));
    }
}