package com.game.algo.algo.exception;

import lombok.Getter;

public class GameLogicException extends RuntimeException {

    @Getter
    private GameExceptionCode gameExceptionCode;

    public GameLogicException(GameExceptionCode gameExceptionCode) {
        super(gameExceptionCode.getMessage());
        this.gameExceptionCode = gameExceptionCode;
    }
}
