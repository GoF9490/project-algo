package com.game.algo.algo.repository;

import com.game.algo.algo.entity.Player;
import org.springframework.data.repository.CrudRepository;

public interface PlayerRedisRepository extends CrudRepository<Player, Long> {
}
