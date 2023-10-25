package com.game.algo.algo.repository;

import com.game.algo.algo.dto.response.GameRoomSimple;

import java.util.List;

public interface GameRoomRepositoryCustom {

    List<GameRoomSimple> getGameRoomSimpleListByGameStart(int page, int size, boolean gameStart);
}
