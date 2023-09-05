package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.game.algo.algo.data.GameProperty.JOKER_BLOCK_NUMBER;
import static org.assertj.core.api.Assertions.*;

class PlayerTest {

    @Test
    @DisplayName("Player의 게임상태 초기화가 정상적으로 이루어져야 합니다.")
    public void playerReset() throws Exception {
        //given
        String playerName = "player1";
        Player player = Player.create(playerName, null);

        //when
        player.gameReset();

        //then
        assertThat(player.getName()).isEqualTo(playerName);
        assertThat(player.getBlockList().size()).isEqualTo(0);
        assertThat(player.getWhiteJokerRange()).isEqualTo(12);
        assertThat(player.getBlackJokerRange()).isEqualTo(12);
    }

    @Test
    @DisplayName("Player에게 정상적으로 Block이 추가됩니다.")
    public void addBlockTest() throws Exception {
        //given
        Player player = Player.create("foo", null);
        player.gameReset();

        Block block = Block.createBlock(BlockColor.BLACK, 1);

        //when
        player.addBlock(block);

        //then
        assertThat(player.getBlockList().size()).isEqualTo(1);

        assertThat(player.getBlockList().get(0).isColor(BlockColor.BLACK)).isTrue();
        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(1);
    }

    @Test
    @DisplayName("0, 1, 2 이 담긴 Block 리스트에  -1이 들어갈 경우 0, 1, -1, 2 순으로 정렬이 되어야 합니다.(양수 흰블럭 음수 검은블럭)")
    public void playerBlockSortTest() throws Exception {
        //given
        Player player = Player.create("foo", null);
        player.gameReset();

        List<Block> blockList = IntStream.range(0, 3)
                .mapToObj(i -> Block.createBlock(BlockColor.WHITE, i))
                .collect(Collectors.toList());

        Block block = Block.createBlock(BlockColor.BLACK, 1);

        //when
        player.addBlock(block);
        blockList.forEach(player::addBlock);

        //then
        assertThat(player.getBlockList().size()).isEqualTo(4);

        assertThat(player.getBlockList().get(0).isColor(BlockColor.WHITE)).isTrue();
        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(0);

        assertThat(player.getBlockList().get(1).isColor(BlockColor.BLACK)).isTrue();
        assertThat(player.getBlockList().get(1).getNum()).isEqualTo(1);

        assertThat(player.getBlockList().get(2).isColor(BlockColor.WHITE)).isTrue();
        assertThat(player.getBlockList().get(2).getNum()).isEqualTo(1);

        assertThat(player.getBlockList().get(3).isColor(BlockColor.WHITE)).isTrue();
        assertThat(player.getBlockList().get(3).getNum()).isEqualTo(2);

        assertThat(player.getDrawBlockIndexNum()).isEqualTo(3);
    }

    @Test
    @DisplayName("(흰색블럭) Player 의 조커를 업데이트 할 경우 올바르게 jokerRange가 바뀌어야 합니다.")
    public void updateJoker() throws Exception {
        //given
        int frontNum = 1;
        int backNum = 5;

        Block frontBlock = Block.createBlock(BlockColor.WHITE, frontNum);
        Block backBlock = Block.createBlock(BlockColor.WHITE, backNum);
        Block jokerBlock = Block.createBlock(BlockColor.WHITE, JOKER_BLOCK_NUMBER);


        Player player = Player.create("foo", null);
        player.gameReset();

        player.addBlock(frontBlock);
        player.addBlock(backBlock);
        player.addBlock(jokerBlock);

        //when
        player.changeJokerNum(1, BlockColor.WHITE);

        //then
        System.out.println(player.getBlockListCode(true).toString());
        assertThat(player.getWhiteJokerRange() / 100).isEqualTo(frontNum);
        assertThat(player.getWhiteJokerRange() % 100).isEqualTo(backNum);

        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(frontNum);
        assertThat(player.getBlockList().get(0).isJoker()).isFalse();
        assertThat(player.getBlockListCode(true).get(0)).isEqualTo(frontNum);

        assertThat(player.getBlockList().get(1).getNum()).isEqualTo(JOKER_BLOCK_NUMBER);
        assertThat(player.getBlockList().get(1).isJoker()).isTrue();
        assertThat(player.getBlockListCode(true).get(1)).isEqualTo(JOKER_BLOCK_NUMBER);

        assertThat(player.getBlockList().get(2).getNum()).isEqualTo(backNum);
        assertThat(player.getBlockList().get(2).isJoker()).isFalse();
        assertThat(player.getBlockListCode(true).get(2)).isEqualTo(backNum);
    }
}