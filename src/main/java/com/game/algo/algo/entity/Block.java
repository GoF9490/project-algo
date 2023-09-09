package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {

    private BlockColor blockColor;

    private Integer num = 0;

    private boolean isOpen;


    protected Block(BlockColor blockColor, Integer num) {
        this.blockColor = blockColor;
        this.num = num;
    }

    public static Block create(BlockColor blockColor, int num) {
        return new Block(blockColor, num);
    }

    public static Block create(BlockColor blockColor, int num, boolean isOpen) {
        return new Block(blockColor, num, isOpen);
    }

    public static int parseBlockCode(int blockCode) {
        int num = Math.abs(blockCode);

        if (num == ZERO_BLOCK_NUMBER) {
            return 0;
        }
        return num;
    }

    public Integer getBlockCode(boolean isOwner) {
        if (!isOwner && isClose()) {
            return CLOSED_BLOCK_NUMBER * blockColor.getCode();
        }

        if (isJoker()) {
            return JOKER_BLOCK_NUMBER * blockColor.getCode();
        }

        if (num == 0) {
            return ZERO_BLOCK_NUMBER * blockColor.getCode();
        }

        return num * blockColor.getCode();
    }

    public boolean isColor(BlockColor blockColor) {
        return this.blockColor == blockColor;
    }

    public boolean isJoker() {
        return num == JOKER_BLOCK_NUMBER || num == -1 * JOKER_BLOCK_NUMBER;
    }

    public void open() {
        this.isOpen = true;
    }

    public boolean comparePosition(Block otherBlock) {
        if (otherBlock.isJoker()) {
            return false;
        }

        if(Objects.equals(this.getNum(), otherBlock.getNum())) {
            return this.blockColor.getCode() > otherBlock.getBlockColor().getCode();
        } else {
            return this.getNum() > otherBlock.getNum();
        }
    }

    public boolean isClose() {
        return !isOpen;
    }
}
