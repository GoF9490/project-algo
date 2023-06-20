package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

class PlayerTest {

    @Test
    @DisplayName("플레이어의 게임상태 초기화가 정상적으로 이루어져야 한다.")
    public void playerReset() throws Exception {
        //given
        String playerName = "player1";
        Player player = new Player(playerName);

        //when
        player.gameReset();

        //then
        assertThat(player.getName()).isEqualTo(playerName);
        assertThat(player.getBlockList().size()).isEqualTo(0);
        assertThat(player.getWhiteJokerRange().getStart()).isEqualTo(0);
        assertThat(player.getWhiteJokerRange().getEnd()).isEqualTo(12);
        assertThat(player.getBlackJokerRange().getStart()).isEqualTo(0);
        assertThat(player.getBlackJokerRange().getEnd()).isEqualTo(12);
    }

    @Test
    @DisplayName("플레이어에게 블럭을 추가할 수 있다.")
    public void addBlockTest() throws Exception {
        //given
        Player player = new Player("foo");
        player.gameReset();

        Block block = Block.createBlock(BlockColor.BLACK, 1);

        //when
        player.addBlock(block);

        //then
        assertThat(player.getBlockList().size()).isEqualTo(1);

        assertThat(player.getBlockList().get(0).getTypeNumber()).isEqualTo(4);
        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(1);
    }

    @Test
    @DisplayName("블럭을 담은 리스트로 플레이어의 블럭에 추가할 수 있다.")
    public void addAllBlockTest() throws Exception {
        //given
        Player player = new Player("foo");
        player.gameReset();

        List<Block> blockList = IntStream.range(0, 3)
                .mapToObj(i -> Block.createBlock(BlockColor.WHITE, i))
                .collect(Collectors.toList());

        //when
        player.addAllBlock(blockList);

        //then
        assertThat(player.getBlockList().size()).isEqualTo(3);

        assertThat(player.getBlockList().get(0).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(0);

        assertThat(player.getBlockList().get(1).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(1).getNum()).isEqualTo(1);

        assertThat(player.getBlockList().get(2).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(2).getNum()).isEqualTo(2);
    }

    @Test
    @DisplayName("0, 1, 2 이 담긴 블록 리스트에  -1이 들어갈 경우 0, 1, -1, 2 순으로 정렬이 되어야 한다.(양수 흰블럭 음수 검은블럭)")
    public void playerBlockSortTest() throws Exception {
        //given
        Player player = new Player("foo");
        player.gameReset();

        List<Block> blockList = IntStream.range(0, 3)
                .mapToObj(i -> Block.createBlock(BlockColor.WHITE, i))
                .collect(Collectors.toList());

        Block block = Block.createBlock(BlockColor.BLACK, 1);

        //when
        player.addBlock(block);
        player.addAllBlock(blockList);

        //then
        assertThat(player.getBlockList().size()).isEqualTo(4);

        assertThat(player.getBlockList().get(0).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(0);

        assertThat(player.getBlockList().get(1).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(1).getNum()).isEqualTo(1);

        assertThat(player.getBlockList().get(2).getTypeNumber()).isEqualTo(4);
        assertThat(player.getBlockList().get(2).getNum()).isEqualTo(1);

        assertThat(player.getBlockList().get(3).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(3).getNum()).isEqualTo(2);
    }
}