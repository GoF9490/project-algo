package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameConstant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.game.algo.algo.data.GameConstant.JOKER_BLOCK_NUMBER;
import static org.assertj.core.api.Assertions.*;

class PlayerTest {

    @Test
    @DisplayName("플레이어의 게임상태 초기화가 정상적으로 이루어져야 한다.")
    public void playerReset() throws Exception {
        //given
        String playerName = "player1";
        Player player = Player.create(playerName, null);

        //when
        player.gameReset();

        //then
        assertThat(player.getName()).isEqualTo(playerName);
        assertThat(player.getBlockList().size()).isEqualTo(0);
        assertThat(player.getWhiteJokerRange()).isEqualTo(null);
        assertThat(player.getBlackJokerRange()).isEqualTo(null);
    }

    @Test
    @DisplayName("플레이어에게 블럭을 추가할 수 있다.")
    public void addBlockTest() throws Exception {
        //given
        Player player = Player.create("foo", null);
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
        Player player = Player.create("foo", null);
        player.gameReset();

        List<Block> blockList = IntStream.range(0, 3)
                .mapToObj(i -> Block.createBlock(BlockColor.WHITE, i))
                .collect(Collectors.toList());

        //when
        blockList.forEach(player::addBlock);

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
    @DisplayName("0, 1, 2 이 담긴 블럭 리스트에  -1이 들어갈 경우 0, 1, -1, 2 순으로 정렬이 되어야 한다.(양수 흰블럭 음수 검은블럭)")
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

        assertThat(player.getBlockList().get(0).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(0);

        assertThat(player.getBlockList().get(1).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(1).getNum()).isEqualTo(1);

        assertThat(player.getBlockList().get(2).getTypeNumber()).isEqualTo(4);
        assertThat(player.getBlockList().get(2).getNum()).isEqualTo(1);

        assertThat(player.getBlockList().get(3).getTypeNumber()).isEqualTo(3);
        assertThat(player.getBlockList().get(3).getNum()).isEqualTo(2);
    }

    @Test
    @DisplayName("joker 블럭을 뽑았을 때 player 에 알맞는 색의 JokerRange 가 설정되고, JokerRelocation 이 true 가 되어야한다.")
    public void addJoker() throws Exception {
        //given
        Block whiteJoker = Block.createBlock(BlockColor.WHITE, JOKER_BLOCK_NUMBER);
        Block blackJoker = Block.createBlock(BlockColor.BLACK, JOKER_BLOCK_NUMBER);

        Player player = Player.create("foo", null);
        player.gameReset();

        //when
        player.addBlock(whiteJoker);
        player.addBlock(blackJoker);

        //then
        assertThat(player.isNeedWhiteJokerRelocation()).isTrue();
        assertThat(player.isNeedBlackJokerRelocation()).isTrue();
        assertThat(player.getWhiteJokerRange().getFrontNum()).isEqualTo(0);
        assertThat(player.getWhiteJokerRange().getBackNum()).isEqualTo(12);
        assertThat(player.getBlackJokerRange().getFrontNum()).isEqualTo(0);
        assertThat(player.getBlackJokerRange().getBackNum()).isEqualTo(12);
    }

    @Test
    @DisplayName("(흰색블럭) player 의 joker 를 업데이트 할 경우 올바르게 jokerRange 와 조커 블럭의 숫자가 바뀌고 JokerRelocation 이 false 가 되어야한다.")
    public void updateJoker() throws Exception {
        //given
        Player player = Player.create("foo", null);
        player.gameReset();

        int frontNum = 1;
        int backNum = 5;

        player.addBlock(Block.createBlock(BlockColor.WHITE, JOKER_BLOCK_NUMBER));

        //when
        player.updateJoker(frontNum, backNum, BlockColor.WHITE);

        //then
        assertThat(player.isNeedWhiteJokerRelocation()).isFalse();
        assertThat(player.getWhiteJokerRange().getFrontNum()).isEqualTo(frontNum);
        assertThat(player.getWhiteJokerRange().getBackNum()).isEqualTo(backNum);
        assertThat(player.getBlockList().get(0).getNum()).isEqualTo(backNum);
    }

    @Test
    @DisplayName("(흰색블럭) jokerRange 범위 내의 같은색의 블럭이 추가될경우 JokerRelocation 이 true 가 되어야 한다.")
    public void addBlockWhenHaveJoker() throws Exception {
        //given
        int frontNum = 1;
        int backNum = 5;
        int betweenNum = 3;

        Block block = Block.createBlock(BlockColor.WHITE, betweenNum);

        Player player = Player.create("foo", null);
        player.gameReset();

        player.addBlock(Block.createBlock(BlockColor.WHITE, JOKER_BLOCK_NUMBER));

        player.updateJoker(frontNum, backNum, BlockColor.WHITE);

        //when
        player.addBlock(block);

        //then
        assertThat(player.isNeedWhiteJokerRelocation()).isTrue();
    }
}