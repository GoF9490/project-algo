package com.game.algo.algo.entity;

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

    private List<Block> block = new ArrayList<>();


    @Builder
    public Player(String name) {
        this.name = name;
    }

    public void sortBlock() {
        block =  block.stream()
                .sorted((a, b) -> {
                    if (a.getType() == b.getType()) {
                        return a.getNum() - b.getNum();
                    }
                    return a.getType().getOrder() - b.getType().getOrder();
                })
                .collect(Collectors.toList());
    }
}
