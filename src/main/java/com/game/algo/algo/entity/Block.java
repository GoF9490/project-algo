package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;

import static com.game.algo.algo.data.GameConstant.*;

/**
 * BlockCode
 * 1 ~ 11 숫자블록, 12 = 조커, 13 = Status.CLOSE, 14 = 숫자 0;
 */

@Getter
//@Entity
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Block {

    @Getter(value = AccessLevel.NONE)
    private Type type;

    private Status status = Status.CLOSE;

    private Integer num = 0;

    private Block(BlockColor blockColor, Integer num) {
        this.type = matchType(blockColor, num);
        this.num = num;
    }

    public static Block createBlock(BlockColor blockColor, int num) {
        return new Block(blockColor, num);
    }

    public Integer getBlockCode(boolean isOwner) {
        if (!isOwner && isClose()) {
            return CLOSED_BLOCK_NUMBER * blackIsMinus();
        }

        if (isJoker()) {
            return JOKER_BLOCK_NUMBER * blackIsMinus();
        }

        if (num == 0) {
            return ZERO_BLOCK_NUMBER * blackIsMinus();
        }

        return num * blackIsMinus();
    }

    public void setNum(int num) {
        this.num = num;
    }

    public int getTypeNumber() {
        return type.getOrder();
    }

    public boolean isBlack() {
        return type == Type.BLACK || type == Type.BLACK_JOKER;
    }

    public boolean isWhite() {
        return type == Type.WHITE || type == Type.WHITE_JOKER;
    }

    public boolean isJoker() {
        return type == Type.WHITE_JOKER || type == Type.BLACK_JOKER;
    }

    public boolean isJoker(BlockColor blockColor) {
        return blockColor == BlockColor.WHITE ? type == Type.WHITE_JOKER : type == Type.BLACK_JOKER;
    }

    private Type matchType(BlockColor blockColor, Integer num) {
        if (blockColor == BlockColor.WHITE) {
            return num == JOKER_BLOCK_NUMBER ? Type.WHITE_JOKER : Type.WHITE;
        } else {
            return num == JOKER_BLOCK_NUMBER ? Type.BLACK_JOKER : Type.BLACK;
        }
    }

    private int blackIsMinus() {
        return isBlack() ? -1 : 1;
    }

    private boolean isClose() {
        return status == Status.CLOSE;
    }

    private boolean isJokerNumber() {
        return num == JOKER_BLOCK_NUMBER;
    }


    /**
     * enum
     */

    private enum Type {
        WHITE_JOKER(1),
        BLACK_JOKER(2),
        WHITE(3),
        BLACK(4);

        private final int order;

        Type(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }
    }

    private enum Status {
        CLOSE,
        OPEN;
    }
}
