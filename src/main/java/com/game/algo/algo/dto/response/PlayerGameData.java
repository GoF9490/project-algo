package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.Player;
import lombok.*;

import java.util.List;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerGameData {

    private Long id;

    private String name;

    private Integer orderNumber;

    private List<Integer> blockCodeList;

    private Integer drawBlockIndexNum;

    private boolean ready;

    private boolean retire;


    public static PlayerGameData from(Player player) {
        return PlayerGameData.builder()
                .id(player.getId())
                .name(player.getName())
                .orderNumber(player.getOrderNumber())
                .blockCodeList(player.getBlockListCode(false))
                .drawBlockIndexNum(player.getDrawBlockIndexNum())
                .ready(player.isReady())
                .retire(player.isRetire())
                .build();
    }
}
