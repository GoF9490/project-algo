package com.game.algo.algo.dto.response;

import com.game.algo.algo.entity.Player;
import lombok.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class DrawBlockData { // 보내고 받아서 쓰기

    private String sessionId;

    private List<Integer> blockCodeList;

    private Integer drawBlockCode;

    public static DrawBlockData from(Player player) {
        List<Integer> newBlockCodeList = player.getBlockListCode(true);

        return DrawBlockData.builder()
                .sessionId(player.getWebSocketSessionId())
                .blockCodeList(newBlockCodeList)
                .drawBlockCode(newBlockCodeList.get(player.getDrawBlockIndexNum()))
                .build()
                .filterDrawBlock();
    }

    private DrawBlockData filterDrawBlock() {
        blockCodeList = blockCodeList.stream()
                .filter(i -> !Objects.equals(i, drawBlockCode))
                .collect(Collectors.toList());

        return this;
    }
}

/**
 * 유니티로 테스트한 항목들 (조커 경우의 수)
 * 
 // case 1
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0, -1, 1, 11),
 12);

 // case 2
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0, -1, 12, 6, 11),
 4);

 // case 3
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0, -1, 1, 11, 12),
 6);

 // case 4
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0,  6, 12, 11),
 -6);

 // case 5
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0,  -6, 12, 11),
 6);

 // case 6
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0,  -6, 12, -11),
 11);

 // case 7
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0,  -6, 12, 11),
 -11);

 // case 8
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0,  -6, 6,  12, 11),
 -12);

 // case 9
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0, -12, -6, 6,  12, 11),
 -7);

 // case 10
 return new DrawBlockData(
 player.getWebSocketSessionId(),
 List.of(0, -6, 6, -12,  12, 11),
 -7);
 */
