package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.GameRoom;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRoomFind { // 레거시

    private List<GameRoomSimple> gameRoomSimpleList;

    private Integer page;

    public static GameRoomFind from(Page<GameRoom> gameRoomPage) {
        return GameRoomFind.builder()
                .gameRoomSimpleList(gameRoomPage.get().map(GameRoomSimple::from).collect(Collectors.toList()))
                .page(gameRoomPage.getNumber())
                .build();
    }

    public static GameRoomFind create(List<GameRoomSimple> gameRoomList, int page) {
        return GameRoomFind.builder()
                .gameRoomSimpleList(gameRoomList)
                .page(page)
                .build();
    }
}
