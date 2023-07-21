package com.game.algo.algo.controller;

import com.game.algo.algo.dto.GameManagerCreate;
import com.game.algo.algo.dto.PlayerCreate;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameWebSocketHandler { // 사용여부 검토 필요

    private final GameService gameService;

    public void createPlayer(PlayerCreate playerCreate) {
        gameService.createPlayer(playerCreate.getName(), playerCreate.getSessionId());

    }

    public void createGameRoom(GameManagerCreate request) {
        Player findPlayer = gameService.findPlayerById(request.getPlayerId());
        gameService.createGameManager(findPlayer);
    }
}
