package com.game.algo.algo.dto;

import com.game.algo.algo.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor//(access = AccessLevel.PROTECTED)
@AllArgsConstructor//(access = AccessLevel.PROTECTED)
public class PlayerGameData {

    private Long id;

    private String name;

    private Integer orderNumber;

    private List<Integer> blockCodeList;

    private boolean ready;


    public static PlayerGameData create(Player player) {
        return new PlayerGameData(
                player.getId(),
                player.getName(),
                player.getOrderNumber(),
                player.getBlockListCode(false),
                player.isReady());
    }
}
