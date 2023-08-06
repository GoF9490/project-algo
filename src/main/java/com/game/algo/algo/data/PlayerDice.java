package com.game.algo.algo.data;

import com.game.algo.algo.entity.Player;
import lombok.Getter;

@Getter
public class PlayerDice {

    private Player player;

    private int num;

    private PlayerDice(Player player, int num) {
        this.player = player;
        this.num = num;
    }

    public static PlayerDice create(Player player) {
        double randomValue = Math.random();
        return new PlayerDice(player, (int)(randomValue * 100));
    }
}
