package com.game.algo.algo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlockGuess {

    private Long gameRoomId;

    private Long playerId;

    private Long targetPlayerId;

    private Integer blockIndex;

    private Integer blockNum;
}
