package com.game.algo.algo.dto;

import com.game.algo.algo.data.BlockColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChoiceJokerPos {

    private Long playerId;

    private Long gameRoomId;

    private BlockColor blockColor;

    private Integer index;
}
