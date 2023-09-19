package com.game.algo.algo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StartBlockDraw {

    private Long playerId;

    private Long gameRoomId;

    private int whiteBlockCount = 0;

    private int blackBlockCount = 0;
}
