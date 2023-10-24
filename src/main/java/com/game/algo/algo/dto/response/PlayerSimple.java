package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.Player;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerSimple {

    private Long id;

    private String name;

    private String sessionId;

    public static PlayerSimple from(Player player) {
        return PlayerSimple.builder()
                .id(player.getId())
                .name(player.getName())
                .sessionId(player.getWebSocketSessionId())
                .build();
    }
}
