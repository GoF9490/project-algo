package com.game.algo.algo.repository;

import com.game.algo.algo.entity.GameManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameManagerRepository extends JpaRepository<GameManager, Long> {
}
