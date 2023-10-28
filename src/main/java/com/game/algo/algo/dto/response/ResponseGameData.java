package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseGameData {

    private GameStatusData gameStatusData;

    private int waitForSecond;

    private OwnerBlockData ownerBlockData;

    public static ResponseGameData from(Player player) {
        if (player.getGameRoom() == null) {
            throw new GameLogicException(GameExceptionCode.PLAYER_NOT_JOIN_ROOM);
        }

        return ResponseGameData.builder()
                .gameStatusData(GameStatusData.from(player.getGameRoom()))
                .waitForSecond(player.getGameRoom().getPhase().getWaitTime())
                .ownerBlockData(OwnerBlockData.from(player))
                .build();
    }
}
