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
public class RelocationJokerData { // 보내고 받아서 쓰기

    private String sessionId;

    private BlockColor blockColor;

    private List<Integer> blockCodeList;

    private Integer drawBlockCode;

    public static RelocationJokerData create(Player player, BlockColor blockColor) {
        List<Integer> newBlockCodeList = player.getBlockListCode(true);

        return new RelocationJokerData(
                player.getWebSocketSessionId(),
                blockColor,
                newBlockCodeList,
                newBlockCodeList.get(player.getDrawBlockIndexNum()))
                .filterBlockCodeList(blockColor);
    }

    private RelocationJokerData filterBlockCodeList(BlockColor blockColor) {
        blockCodeList = blockCodeList.stream()
                .filter(i -> !Objects.equals(i, drawBlockCode) && isInRange(i, blockColor))
                .collect(Collectors.toList());

        return this;
    }
    
    private boolean isInRange(int i, BlockColor blockColor) {
        if (blockColor == BlockColor.WHITE) {
            return i > 0;
        }
        return i < 0;
    }
}
