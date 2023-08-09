package com.game.algo.algo.repository;

import com.game.algo.algo.entity.GameRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

    @Override
    @EntityGraph(attributePaths = {"playerList"})
    Optional<GameRoom> findById(Long id);
}
