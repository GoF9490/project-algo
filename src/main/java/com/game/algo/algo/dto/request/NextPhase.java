package com.game.algo.algo.dto.request;

import com.game.algo.algo.entity.GameRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NextPhase {

    private Long gameRoomId;

    private Long playerId;

    private GameRoom.Phase phase;

    private Integer progressPlayerNum;
}
