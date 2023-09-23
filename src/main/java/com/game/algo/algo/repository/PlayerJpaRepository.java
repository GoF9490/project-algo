package com.game.algo.algo.repository;

import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface PlayerJpaRepository extends JpaRepository<Player, Long> {

    @Override
    Optional<Player> findById(Long id);

    @EntityGraph(attributePaths = {"gameRoom"})
    Optional<Player> findByWebSocketSessionId(String webSocketSessionId);
}
