package com.game.algo.algo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerReadyUpdate {

    private Long playerId;

    private Long gameRoomId;

    private Boolean ready;
}
