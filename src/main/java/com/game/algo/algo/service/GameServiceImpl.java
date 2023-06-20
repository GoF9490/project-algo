package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameManager;
import com.game.algo.algo.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

//    private final GameManagerRepository gameManagerRepository;
//    private final PlayerRepository playerRepository;

    public void choiceBlock(GameManager gameManager, Player player, ChoiceBlockInfo choiceBlock) {
        List<Block> receivedBlock = new ArrayList<>();

        receivedBlock.addAll(getRandomBlock(gameManager, BlockColor.WHITE, choiceBlock.getWhite()));
        receivedBlock.addAll(getRandomBlock(gameManager, BlockColor.BLACK, choiceBlock.getBlack()));

        player.addAllBlock(receivedBlock);
    }

    private List<Block> getRandomBlock(GameManager gameManager, BlockColor blockColor, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> gameManager.drawRandomBlock(blockColor))
                .collect(Collectors.toList());
    }
}
