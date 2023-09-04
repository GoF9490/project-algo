package com.game.algo.algo.dto;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DrawBlockData { // 보내고 받아서 쓰기

    private String sessionId;

    private List<Integer> blockCodeList;

    private Integer drawBlockCode;

    public static DrawBlockData create(Player player) {
        List<Integer> newBlockCodeList = player.getBlockListCode(true);

        return new DrawBlockData(
                player.getWebSocketSessionId(),
                newBlockCodeList,
                newBlockCodeList.get(player.getDrawBlockIndexNum()))
                .filterDrawBlock();
    }

    private DrawBlockData filterDrawBlock() {
        blockCodeList = blockCodeList.stream()
                .filter(i -> !Objects.equals(i, drawBlockCode))
                .collect(Collectors.toList());

        return this;
    }
}
