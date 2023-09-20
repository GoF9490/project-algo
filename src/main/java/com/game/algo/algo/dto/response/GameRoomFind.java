package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.GameRoom;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRoomFind {

    private List<GameRoomSimple> gameRoomSimpleList;

    private Integer page;

    private boolean lastPage;

    public static GameRoomFind from(Page<GameRoom> gameRoomPage) {
        return GameRoomFind.builder()
                .gameRoomSimpleList(gameRoomPage.get().map(GameRoomSimple::from).collect(Collectors.toList()))
                .page(gameRoomPage.getNumber())
                .lastPage(gameRoomPage.isLast())
                .build();
    }
}
