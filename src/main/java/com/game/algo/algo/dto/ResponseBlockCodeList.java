package com.game.algo.algo.dto;

import com.game.algo.algo.entity.Block;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BlockCode
 * 0 ~ 11 숫자블록, 12 = 조커, 13 = Status.CLOSE
 */

@Getter
public class ResponseBlockCodeList {

    List<Integer> blockCodeList = new ArrayList<>();

    private ResponseBlockCodeList(List<Integer> blockCodeList) {
        this.blockCodeList = blockCodeList;
    }

    public static ResponseBlockCodeList createBlockCodeList(List<Block> blockList, boolean isMaster) {
        List<Integer> blockCodeList = blockList.stream()
                .map(block -> block.getBlockCode(isMaster))
                .collect(Collectors.toList());

        return new ResponseBlockCodeList(blockCodeList);
    }
}
