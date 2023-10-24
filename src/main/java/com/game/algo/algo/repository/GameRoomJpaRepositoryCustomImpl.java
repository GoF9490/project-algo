package com.game.algo.algo.repository;

import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.dto.response.GameRoomSimple;

import com.game.algo.algo.entity.QGameRoom;
import com.game.algo.algo.entity.QPlayer;
import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GameRoomJpaRepositoryCustomImpl implements GameRoomJpaRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GameRoomSimple> findGameRoomSimples(int page, int size, boolean start) {
        QGameRoom gameRoom = QGameRoom.gameRoom;
        QPlayer player = QPlayer.player;

        return jpaQueryFactory
                .select(gameRoom.id, gameRoom.title, player.count().intValue())
                .from(gameRoom)
                .leftJoin(player).on(player.gameRoom.id.eq(gameRoom.id))
                .where(gameRoom.gameStart.eq(start))
                .groupBy(gameRoom.id)
                .offset((long) page * size)
                .limit(size)
                .stream()
                .map(tuple -> GameRoomSimple.create(
                        tuple.get(gameRoom.id),
                        tuple.get(gameRoom.title),
                        tuple.get(player.count().intValue())))
                .collect(Collectors.toList());
    }
}
