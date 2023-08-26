package com.game.algo.algo.dto;

import com.game.algo.algo.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor//(access = AccessLevel.PROTECTED)
@AllArgsConstructor//(access = AccessLevel.PROTECTED)
public class OwnerBlockData {

    private String sessionId;

    private List<Integer> blockCodeList;


    public static OwnerBlockData create(Player player) {
        return new OwnerBlockData(
                player.getWebSocketSessionId(),
                player.getBlockListCode(true));
    }
}
