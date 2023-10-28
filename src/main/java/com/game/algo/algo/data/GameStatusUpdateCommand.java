package com.game.algo.algo.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class GameStatusUpdateCommand {

    private List<String> sessionIdList;

    public static GameStatusUpdateCommand create(List<String> sessionIdList) {
        return GameStatusUpdateCommand.builder()
                .sessionIdList(sessionIdList)
                .build();
    }
}
