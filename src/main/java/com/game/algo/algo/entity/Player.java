package com.game.algo.algo.entity;

import com.game.algo.algo.data.JokerRange;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@RedisHash(value = "player")
public class Player {

    @Id
    private Long id;

    private String name; // or Member 객체

    private List<Block> blockList = new ArrayList<>();

    private JokerRange whiteJokerRange = new JokerRange();

    private JokerRange blackJokerRange = new JokerRange();


    @Builder
    public Player(String name) {
        this.name = name;
    }

    public void gameReset() {
        blockList = new ArrayList<>();
        whiteJokerRange = new JokerRange();
        blackJokerRange = new JokerRange();
    }

    public void sortBlock() {
        blockList =  blockList.stream()
                .sorted((a, b) -> {
                    if (a.getNum() == b.getNum()) {
                        return a.getTypeNumber() - b.getTypeNumber();
                    }
                    return a.getNum() - b.getNum();
                })
                .collect(Collectors.toList());
    }

    public void addBlock(Block block) {
        blockList.add(block);
        sortBlock();
    }

    public void addAllBlock(List<Block> blockList) {
        this.blockList.addAll(blockList);
        sortBlock();
    }
}
