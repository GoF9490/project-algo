package com.game.algo.algo.dto.request;

import com.game.algo.algo.data.BlockColor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlockDraw {

    private Long playerId;

    private Long gameRoomId;

    private BlockColor blockColor;
}
