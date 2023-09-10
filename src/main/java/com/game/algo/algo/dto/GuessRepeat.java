package com.game.algo.algo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GuessRepeat {

    private Long gameRoomId;

    private Long playerId;

    private boolean repeatGuess;
}
