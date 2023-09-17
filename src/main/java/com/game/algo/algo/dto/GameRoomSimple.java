package com.game.algo.algo.dto;

import com.game.algo.algo.entity.GameRoom;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRoomSimple {

    private Long gameRoomId;

    private String gameRoomTitle;

    private Integer playerCount;

    public static GameRoomSimple from(GameRoom gameRoom) {
        return GameRoomSimple.builder()
                .gameRoomId(gameRoom.getId())
                .gameRoomTitle(gameRoom.getTitle())
                .playerCount(gameRoom.getPlayerList().size())
                .build();
    }
}
