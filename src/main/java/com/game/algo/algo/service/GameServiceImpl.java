package com.game.algo.algo.service;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.PlayerDice;
import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameManager;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameManagerRepository;
import com.game.algo.algo.repository.PlayerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 클라이언트와 통신해야함
 * 매 페이즈마다 클라이언트와 확인절차를 거치게끔 하는게 좋을듯?
 * 도중에 튕기면 그에 알맞는 조치를 취해야함
 */

@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameManagerRepository gameManagerRepository;
    private final PlayerJpaRepository playerJpaRepository;

    public void testLogging(String message) {
        System.out.println(message);
    }

    public void updatePlayerReady(Player player, boolean isReady) {
        player.updateReady(isReady);
    }

    public void checkAllPlayerReady(GameManager gameManager) {
        int allPlayerCount = gameManager.getPlayerList().size();
        int readyPlayerCount = (int) gameManager.getPlayerList().stream().filter(Player::isReady).count();

        if (allPlayerCount >= 2 && allPlayerCount == readyPlayerCount) {
            gameReset(gameManager);
        }
    }

    public void gameReset(GameManager gameManager) { // 테스트해야함
        gameManager.gameReset();
        gameManager.playerOrderReset();

        // playerOrder 클라이언트에 전달
    }

    public void gameStart(GameManager gameManager) {
        gameManager.updatePhase(GameManager.Phase.READY);
    }

    public void choiceBlock(GameManager gameManager, Player player, ChoiceBlockInfo choiceBlock) {
        List<Block> receivedBlock = new ArrayList<>();

        receivedBlock.addAll(getRandomBlock(gameManager, BlockColor.WHITE, choiceBlock.getWhite()));
        receivedBlock.addAll(getRandomBlock(gameManager, BlockColor.BLACK, choiceBlock.getBlack()));

        receivedBlock.forEach(player::addBlock);

        // 조커를 가지고있을때
    }

    public Long createGameManager(Player player){
        GameManager gameManager = GameManager.create(player);
        return gameManagerRepository.save(gameManager).getId();
    }

    public Long createPlayer(String name, String webSocketSessionId) {
        Player player = Player.create(name, webSocketSessionId);
        return playerJpaRepository.save(player).getId();
    }

    public GameManager findGameManagerById(Long id) {
        return gameManagerRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.GAME_MANAGER_NOT_FOUND));
    }

    public Player findPlayerById(Long id) {
        return playerJpaRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.PLAYER_NOT_FOUND));
    }

    private void choiceFirstBlocks(List<Player> playerList) {
        /**
         * 첫번째 플레이어에게 블록을 선택하도록 알림.
         * 인원수에따라 선택가능한 블록의 수도 달라짐. (3명이하 5개, 4명 4개)
         * 첫번째 플레이어가 블록 선택을 응답하면 흑과 백의 수만큼 GameManager 에서 빼서 해당 플레이에의 패로 넣음.
         * 이후 다음 플레이어에게 블록을 선택하도록 알림.
         * 이 작업을 해당 방의 인원수만큼 반복하고 모든 인원이 진행했으면 게임 시작.
         */
    }

    private List<Block> getRandomBlock(GameManager gameManager, BlockColor blockColor, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> gameManager.drawRandomBlock(blockColor))
                .collect(Collectors.toList());
    }
}
