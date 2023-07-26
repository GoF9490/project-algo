package com.game.algo.algo.dto.messagetype;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomJoin {

    private Long gameRoomId;

    private Long playerId;
}
