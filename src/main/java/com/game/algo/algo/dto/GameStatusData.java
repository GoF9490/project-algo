package com.game.algo.algo.dto;

import com.game.algo.algo.entity.GameRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusData {

    private Long id;

    private GameRoom.Phase phase;

    private List<PlayerGameData> playerDataList = new ArrayList<>();

    private Integer progressPlayerNumber;

    private Integer whiteBlockCount;

    private Integer blackBlockCount;

    public static GameStatusData create(GameRoom gameRoom) {
        return new GameStatusData(
                gameRoom.getId(),
                gameRoom.getPhase(),
                gameRoom.getPlayerList().stream()
                        .map(PlayerGameData::create)
                        .collect(Collectors.toList()),
                gameRoom.getProgressPlayerNumber(),
                gameRoom.getWhiteBlockList().size(),
                gameRoom.getBlackBlockList().size());
    }
}
