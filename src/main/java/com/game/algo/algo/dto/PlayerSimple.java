package com.game.algo.algo.dto;

import com.game.algo.algo.entity.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSimple {

    private Long id;

    private String name;

    private String sessionId;

    public static PlayerSimple create(Player player) {
        return new PlayerSimple(
                player.getId(),
                player.getName(),
                player.getWebSocketSessionId());
    }
}
