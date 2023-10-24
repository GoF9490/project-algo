package com.game.algo.algo.repository;

import com.game.algo.algo.dto.response.GameRoomSimple;

import java.util.List;

public interface GameRoomJpaRepositoryCustom {

    List<GameRoomSimple> findGameRoomSimples(int page, int size, boolean start);
}
