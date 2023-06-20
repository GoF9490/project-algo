package com.game.algo.algo.service;

import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.entity.GameManager;
import com.game.algo.algo.entity.Player;

public interface GameService {

    void choiceBlock(GameManager gameManager, Player player, ChoiceBlockInfo choiceBlock);
}
