package com.game.algo.algo.repository;

import com.game.algo.algo.entity.GameRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

    @Override
    @EntityGraph(attributePaths = {"playerList"})
//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GameRoom> findById(Long id);
}
