package com.game.algo.algo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameStart {

    private Long gameRoomId;
    private Long playerId;
}
