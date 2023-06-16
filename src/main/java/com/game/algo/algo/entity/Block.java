package com.game.algo.algo.entity;

import lombok.Getter;

/**
 * BlockCode
 * 0 ~ 11 숫자블록, 12 = 조커, 13 = Status.CLOSE
 */

@Getter
//@Entity
public class Block {

    private Type type;

    private Status status = Status.CLOSE;

    private Integer num = 0;

    private Block(Type type, Integer num) {
        this.type = type;
        this.num = num;
    }

    public static Block createWhiteBlock(int num) {
        return num == 12 ? new Block(Type.WHITE_JOKER, num) : new Block(Type.WHITE, num);
    }

    public static Block createBlackBlock(int num) {
        return num == 12 ? new Block(Type.BLACK_JOKER, num) : new Block(Type.BLACK, num);
    }

    public Integer getNum() {
        return num;
    }

    public Integer getBlockCode(boolean isMaster) {
        if (!isMaster && isClose()) {
            return 13 * blackIsMinus();
        }

        if (isJoker()) {
            return 12 * blackIsMinus();
        }

        return num * blackIsMinus();
    }

    private int blackIsMinus() {
        return isBlack() ? 1 : -1;
    }

    private boolean isClose() {
        return status == Status.CLOSE;
    }

    private boolean isBlack() {
        return type == Type.BLACK || type == Type.BLACK_JOKER;
    }

    private boolean isJoker() {
        return num == 12;
    }


    /**
     * enum
     */

    public enum Type {
        WHITE_JOKER(1),
        BLACK_JOKER(2),
        WHITE(3),
        BLACK(4);

        private int order;

        Type(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }
    }

    public enum Status {
        CLOSE,
        OPEN;
    }
}
