package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.Player;
import lombok.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OwnerBlockData {

    private String sessionId;

    private List<Integer> blockCodeList;


    public static OwnerBlockData from(Player player) {
        return OwnerBlockData.builder()
                .sessionId(player.getWebSocketSessionId())
                .blockCodeList(player.getBlockListCode(true))
                .build();
    }
}
