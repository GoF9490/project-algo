package com.game.algo.algo.data;

import com.game.algo.algo.entity.GameRoom;

/**
 * BlockCode
 * 0 ~ 11 숫자블록, 12 = 조커, 13 = Status.CLOSE
 */

public class GameProperty {

    public static final String VERSION = "0.01";

    public static final int JOKER_BLOCK_NUMBER = 12;

    public static final int CLOSED_BLOCK_NUMBER = 13;

    public static final int ZERO_BLOCK_NUMBER = 14;

    public static final int PLAYER_MAX_COUNT = 4;

    public static final int FIND_GAME_ROOM_SIZE = 60;

    public static int numberOfBlockAtStart(int playerCount) {
        return (playerCount < 4) ? 4 : 3;
    }
}
