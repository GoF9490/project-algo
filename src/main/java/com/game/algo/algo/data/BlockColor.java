package com.game.algo.algo.data;

//안쓰면 삭제
public enum BlockColor {

    BLACK(-1),
    WHITE(1);

    final int order;

    BlockColor(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
