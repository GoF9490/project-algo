package com.game.algo.algo.repository;

import com.game.algo.algo.dto.response.GameRoomSimple;
import com.game.algo.algo.entity.QGameRoom;
import com.game.algo.algo.entity.QPlayer;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class GameRoomRepositoryImpl implements GameRoomRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<GameRoomSimple> getGameRoomSimpleListByGameStart(int page, int size, boolean gameStart) {
        QGameRoom gameRoom = QGameRoom.gameRoom;
        QPlayer player = QPlayer.player;

        List<GameRoomSimple> count = jpaQueryFactory
                .select(gameRoom.id, gameRoom.title, player.count().intValue().as("count"))
                .from(gameRoom)
                .leftJoin(player).on(player.gameRoom.id.eq(gameRoom.id))
                .where(gameRoom.gameStart.eq(gameStart))
                .groupBy(gameRoom.id)
                .offset((long) page * size)
                .limit(size)
                .fetch()
                .stream()
                .map(tuple -> GameRoomSimple.create(
                        tuple.get(gameRoom.id),
                        tuple.get(gameRoom.title),
                        tuple.get(2, Integer.class)))
                .collect(Collectors.toList());

        return count;
    }
}
