package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.GameRoom;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GameStatusData {

    private Long id;

    private GameRoom.Phase phase;

    @Builder.Default
    private List<PlayerGameData> playerDataList = new ArrayList<>();

    private Integer progressPlayerNumber;

    private Integer whiteBlockCount;

    private Integer blackBlockCount;

    public static GameStatusData from(GameRoom gameRoom) {
        return GameStatusData.builder()
                .id(gameRoom.getId())
                .phase(gameRoom.getPhase())
                .playerDataList(gameRoom.getPlayerList().stream()
                        .map(PlayerGameData::from)
                        .collect(Collectors.toList()))
                .progressPlayerNumber(gameRoom.getProgressPlayerNumber())
                .whiteBlockCount(gameRoom.getWhiteBlockList().size())
                .blackBlockCount(gameRoom.getBlackBlockList().size())
                .build()
                .hidePlayerDataBeforeSorting();
    }

    private GameStatusData hidePlayerDataBeforeSorting() {
        if (this.phase == GameRoom.Phase.SORT) {
            this.playerDataList = null;
        }
        return this;
    }
}
