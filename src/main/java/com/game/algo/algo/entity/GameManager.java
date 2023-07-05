package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
//@Entity
//@RedisHash(value = "game_manager")
@NoArgsConstructor
public class GameManager {

    @Id
    private Long id;

    private Phase phase = Phase.WAIT;

    private List<Player> playerList = new ArrayList<>();

    private List<Player> playerOrder = new ArrayList<>();

    private Integer turn = 0;

    private List<Block> whiteBlockList;

    private List<Block> blackBlockList;




    public void gameReset() {
        phase = Phase.READY;
        playerOrder = new ArrayList<>();
        turn = 0;
        blockReset();
    }

    public void joinPlayer(Player player) {
        List<Player> playerListEdit = new ArrayList<>(playerList);
        playerListEdit.add(player);
        playerList = playerListEdit;
    }

    public void playerOrderReset() {
        playerOrder = playerList.stream()
                .sorted(Comparator.comparing(player -> Math.random()))
                .collect(Collectors.toList());
    }

    public Block drawRandomBlock(BlockColor blockColor) {
        double randomValue = Math.random();

        if (blockColor == BlockColor.WHITE) {
            return whiteBlockList.remove((int)(randomValue * whiteBlockList.size()));
        } else {
            return blackBlockList.remove((int)(randomValue * blackBlockList.size()));
        }
    }

    private void blockReset() {
        this.whiteBlockList = blockSet(BlockColor.WHITE);
        this.blackBlockList = blockSet(BlockColor.BLACK);
    }

    private List<Block> blockSet(BlockColor blockColor) {
        return IntStream.range(0, 13)
                .mapToObj(num -> Block.createBlock(blockColor, num))
                .collect(Collectors.toList());
    }

    public enum Phase {
        WAIT, // 게임 시작 전
        READY, // 게임 세팅, 이후 진행순서 정함
        START, // 시작, 진행순서대로 블록을 뽑고 이후 게임 시작
        CONTROL, // 플레이어의 차례를 순서대로 바꿈
        DRAW, // 블록을 하나 선택함
        SET,
        GUESS,
        REPEAT,
        END
    }
}
