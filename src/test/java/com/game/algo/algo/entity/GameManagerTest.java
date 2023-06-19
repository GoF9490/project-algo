package com.game.algo.algo.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class GameManagerTest {

    @Test
    @DisplayName("게임 리셋 후 페이즈는 READY 여야 한다.")
    public void checkTypeAtGameReset() throws Exception {
        //given
        GameManager gameManager = new GameManager();

        //when
        gameManager.gameReset();

        //then
        Assertions.assertThat(gameManager.getPhase()).isEqualTo(GameManager.Phase.READY);
    }

    @Test
    @DisplayName("게임 리셋 후 흰 / 검은 블록은 1~12까지 생성되어야 한다.(12는 JOKER)")
    public void checkAllBlocksNumberAtGameReset() throws Exception {
        //given
        GameManager gameManager = new GameManager();

        //when
        gameManager.gameReset();

        //then
        IntStream.range(0, 13).forEach(i -> {
            Assertions.assertThat(gameManager.getWhiteBlockList().get(i).getNum()).isEqualTo(i);
            Assertions.assertThat(gameManager.getBlackBlockList().get(i).getNum()).isEqualTo(i);
        });
    }

    @Test
    @DisplayName("게임 리셋 후 흰 / 검은 블록은 각각 whiteBlockList / blackBlockList 에 담겨있어야 한다.")
    public void checkAllBlocksTypeAtGameReset() throws Exception {
        //given
        GameManager gameManager = new GameManager();

        //when
        gameManager.gameReset();

        //then
        IntStream.range(0, 13).forEach(i -> {
            if (i == 12) {
                Assertions.assertThat(gameManager.getWhiteBlockList().get(i).getTypeNumber()).isEqualTo(1);
                Assertions.assertThat(gameManager.getBlackBlockList().get(i).getTypeNumber()).isEqualTo(2);
            } else {
                Assertions.assertThat(gameManager.getWhiteBlockList().get(i).getTypeNumber()).isEqualTo(3);
                Assertions.assertThat(gameManager.getBlackBlockList().get(i).getTypeNumber()).isEqualTo(4);
            }
        });
    }

}