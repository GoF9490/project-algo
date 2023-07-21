package com.game.algo.algo.service;

import com.game.algo.algo.entity.Player;

public interface PlayerService {

    Long createPlayer(String name, String webSocketSessionId);

    Player findPlayerById(Long id);

    void updatePlayerReady(Player player, boolean isReady);
}
