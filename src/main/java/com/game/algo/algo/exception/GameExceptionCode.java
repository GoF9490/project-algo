package com.game.algo.algo.exception;

import lombok.Getter;

@Getter
public enum GameExceptionCode {

    GAME_ROOM_NOT_FOUND(400, "game room not found"),
    GAME_ROOM_IS_FULL(400, "game room not found"),
    PLAYER_NOT_FOUND(400, "player not found"),
    PLAYER_NOT_READY(400, "player not ready"),
    LACK_OF_PLAYER(400, "lack of player"),
    INVALID_NUMBER_OF_BLOCKS(400, "invalid number of blocks"),
    OUT_OF_SYNC_GAME_PHASE(400, "out of sync game phase"),
    OUT_OF_SYNC_PLAYER_ORDER(400, "out of sync player order"),
    BLOCK_IS_DEPLETED(400, "block is depleted"),
    JOKER_NOT_MATCH(400, "joker not match"),
    ALREADY_GAME_START(400, "already game start"),
    ALREADY_EXECUTED(400, "already executed");

    private int status;

    private String message;

    GameExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
