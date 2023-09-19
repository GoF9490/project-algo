package com.game.algo.algo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomCreate {

    private Long playerId;

    private String title;
}
