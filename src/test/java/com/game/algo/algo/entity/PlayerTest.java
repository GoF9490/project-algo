package com.game.algo.algo.entity;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    @DisplayName("플레이어의 게임상태 초기화가 정상적으로 이루어져야 한다.")
    public void playerReset() throws Exception {
        //given
        String playerName = "player1";
        Player player = new Player(playerName);

        //when
        player.reset();

        //then
        assertThat(player.getName()).isEqualTo(playerName);
        assertThat(player.getBlockList().size()).isEqualTo(0);
        assertThat(player.getWhiteJokerRange().getStart()).isEqualTo(0);
        assertThat(player.getWhiteJokerRange().getEnd()).isEqualTo(12);
        assertThat(player.getBlackJokerRange().getStart()).isEqualTo(0);
        assertThat(player.getBlackJokerRange().getEnd()).isEqualTo(12);
    }
}