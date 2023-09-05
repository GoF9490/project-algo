package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import java.util.Objects;

import static com.game.algo.algo.data.GameProperty.*;

/**
 * BlockCode
 * 1 ~ 11 숫자블록, 12 = 조커, 13 = Status.CLOSE, 14 = 숫자 0;
 */

@Getter
//@Entity
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {

    @Enumerated(value = EnumType.STRING)
    private BlockColor blockColor;

    @Getter(value = AccessLevel.NONE)
    @Enumerated(value = EnumType.STRING)
    private Status status = Status.CLOSE;

    private Integer num = 0;

    private Block(BlockColor blockColor, Integer num) {
        this.blockColor = blockColor;
        this.num = num;
    }

    public static Block createBlock(BlockColor blockColor, int num) {
        return new Block(blockColor, num);
    }

    public Integer getBlockCode(boolean isOwner) {
        if (!isOwner && isClose()) {
            return CLOSED_BLOCK_NUMBER * blockColor.getOrder();
        }

        if (isJoker()) {
            return JOKER_BLOCK_NUMBER * blockColor.getOrder();
        }

        if (num == 0) {
            return ZERO_BLOCK_NUMBER * blockColor.getOrder();
        }

        return num * blockColor.getOrder();
    }

    public boolean isColor(BlockColor blockColor) {
        return this.blockColor == blockColor;
    }

    public boolean isJoker() {
        return num == JOKER_BLOCK_NUMBER || num == -1 * JOKER_BLOCK_NUMBER;
    }

    public void open() {
        this.status = Status.OPEN;
    }

    public boolean comparePosition(Block otherBlock) {
        if (otherBlock.isJoker()) {
            return false;
        }

        if(Objects.equals(this.getNum(), otherBlock.getNum())) {
            return this.blockColor.getOrder() > otherBlock.getBlockColor().getOrder();
        } else {
            return this.getNum() > otherBlock.getNum();
        }
    }

    private boolean isClose() {
        return status == Status.CLOSE;
    }


    private enum Status {
        CLOSE,
        OPEN;
    }
}
