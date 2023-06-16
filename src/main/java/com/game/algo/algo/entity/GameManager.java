package com.game.algo.algo.entity;

import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
//@Entity
//@RedisHash(value = "game_manager")
public class GameManager {

    @Id
    private Long id;

    private Phase phase = Phase.WAIT;

    private List<Player> playerList = new ArrayList<>();

    private List<Player> playerOrder = new ArrayList<>();

    private Integer turn = 0;

    private List<Block> whiteBlock;

    private List<Block> blackBlock;


    public void gameReset() {
        phase = Phase.READY;
        playerOrder = new ArrayList<>();
        turn = 0;
        blockReset();
    }

    private void blockReset() {
        this.whiteBlock = whiteBlockSet();
        this.blackBlock = blackBlockSet();
    }

    private List<Block> whiteBlockSet() {
        return IntStream.range(0, 13)
                .mapToObj(Block::createWhiteBlock)
                .collect(Collectors.toList());
    }

    private List<Block> blackBlockSet() {
        return IntStream.range(0, 13)
                .mapToObj(Block::createBlackBlock)
                .collect(Collectors.toList());
    }

    private enum Phase {
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
