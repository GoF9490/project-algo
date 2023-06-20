package com.game.algo.algo.service;

import com.game.algo.algo.entity.GameManager;
import com.game.algo.algo.entity.Player;
import org.junit.jupiter.api.Test;

class GameServiceTest {

    private GameService gameService = new GameServiceImpl();

    @Test
    public void choiceBlockTest() throws Exception {
        //given
        GameManager gameManager = new GameManager();
        gameManager.gameReset();

        Player player = new Player("player1");
        player.reset();

        //when

        //then
    }

}