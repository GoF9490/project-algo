package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.GameRoom;
import lombok.*;

@Getter
@Builder(access = AccessLevel.PROTECTED)
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

    public static GameRoomSimple create(Long gameRoomId,
                                        String gameRoomTitle,
                                        Integer playerCount) {

        return GameRoomSimple.builder()
                .gameRoomId(gameRoomId)
                .gameRoomTitle(gameRoomTitle)
                .playerCount(playerCount)
                .build();
    }
}
