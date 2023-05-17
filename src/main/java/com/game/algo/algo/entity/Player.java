package com.game.algo.algo.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Getter
@RedisHash(value = "player")
public class Player {

    @Id
    private Long id;

    private String name;

    @Builder
    public Player(String name) {
        this.name = name;
    }
}
