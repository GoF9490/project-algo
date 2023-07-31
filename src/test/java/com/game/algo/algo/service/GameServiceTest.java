package com.game.algo.algo.service;

import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class GameServiceTest {

    @Autowired private GameService gameService;

    @RepeatedTest(5)
    @DisplayName("ChoiceBlockInfo 의 데이터를 토대로 GameRoom 의 블록을 Player 에게 전달합니다.")
    public void choiceBlockTest() throws Exception {
        //given
        Player player = Player.create("player1", null);
        player.gameReset();

        GameRoom gameRoom = GameRoom.create();
        gameRoom.gameReset();

        ChoiceBlockInfo choiceBlockInfo = new ChoiceBlockInfo(2, 3);

        //when
        gameService.choiceBlock(gameRoom, player, choiceBlockInfo);

        //then
        assertThat(player.getBlockList().size()).isEqualTo(choiceBlockInfo.getWhite() + choiceBlockInfo.getBlack());

        assertThat(howManyWhiteBlock(player.getBlockList())).isEqualTo(choiceBlockInfo.getWhite());
        assertThat(howManyBlackBlock(player.getBlockList())).isEqualTo(choiceBlockInfo.getBlack());

        player.getBlockList().stream()
                .forEach(block -> assertThat(block.getNum()).isBetween(0, 12));

        System.out.println(player.getBlockList().stream()
                .map(block -> block.getBlockCode(true))
                .collect(Collectors.toList()).toString());
    }

    private long howManyWhiteBlock(List<Block> BlockList) {
        return BlockList.stream().filter(Block::isWhite).count();
    }

    private long howManyBlackBlock(List<Block> BlockList) {
        return BlockList.stream().filter(Block::isBlack).count();
    }
}