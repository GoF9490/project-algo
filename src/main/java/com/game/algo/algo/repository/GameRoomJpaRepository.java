package com.game.algo.algo.repository;

import com.game.algo.algo.entity.GameRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRoomJpaRepository extends JpaRepository<GameRoom, Long>, GameRoomJpaRepositoryCustom {

    @Override
    @EntityGraph(attributePaths = {"playerList"})
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GameRoom> findById(Long id);

    Page<GameRoom> findAllByGameStart(Boolean gameStart, Pageable pageable);
}
