package com.game.algo.algo.data;

//안쓰면 삭제
public enum BlockColor {

    BLACK(-1),
    WHITE(1);

    final int code;

    BlockColor(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
