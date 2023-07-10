package com.game.algo.algo.service;

import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.entity.GameManager;
import com.game.algo.algo.entity.Player;

public interface GameService {

    void testLogging(String message);

    void choiceBlock(GameManager gameManager, Player player, ChoiceBlockInfo choiceBlock);

    void updatePlayerReady(Player player, boolean isReady);
}
