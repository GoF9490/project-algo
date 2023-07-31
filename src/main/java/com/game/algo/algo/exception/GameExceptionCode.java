package com.game.algo.algo.exception;

import lombok.Getter;

@Getter
public enum GameExceptionCode {

    GAME_ROOM_NOT_FOUND(400, "game room not found"),
    GAME_ROOM_IS_FULL(400, "game room not found"),
    PLAYER_NOT_FOUND(400, "player not found"),
    JOKER_NOT_MATCH(400, "joker not match"),
    JOKER_ALREADY_CHANGED(400, "joker already changed");

    private int status;

    private String message;

    GameExceptionCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
