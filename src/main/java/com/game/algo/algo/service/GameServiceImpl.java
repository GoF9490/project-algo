package com.game.algo.algo.service;

import com.game.algo.algo.annotation.SendGameStatusByWebSocket;
import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.ChoiceBlockInfo;
import com.game.algo.algo.entity.Block;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.repository.GameRoomRepository;
import com.game.algo.algo.repository.PlayerJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    private final GameRoomRepository gameRoomRepository;
    private final PlayerJpaRepository playerJpaRepository;

    public void testLogging(String message) {
        System.out.println(message);
    }

    public void updatePlayerReady(Player player, boolean isReady) {
        player.updateReady(isReady);
    }

    public void checkAllPlayerReady(GameRoom gameRoom) {
        int allPlayerCount = gameRoom.getPlayerList().size();
        int readyPlayerCount = (int) gameRoom.getPlayerList().stream().filter(Player::isReady).count();

        if (allPlayerCount >= 2 && allPlayerCount == readyPlayerCount) {
            gameReset(gameRoom);
        }
    }

    public void gameReset(GameRoom gameRoom) { // 테스트해야함
        gameRoom.gameReset();
        gameRoom.playerOrderReset();

        // playerOrder 클라이언트에 전달
    }

    public void gameStart(GameRoom gameRoom) {
        gameRoom.updatePhase(GameRoom.Phase.READY);
    }

    public void choiceBlock(GameRoom gameRoom, Player player, ChoiceBlockInfo choiceBlock) {
        List<Block> receivedBlock = new ArrayList<>();

        receivedBlock.addAll(getRandomBlock(gameRoom, BlockColor.WHITE, choiceBlock.getWhite()));
        receivedBlock.addAll(getRandomBlock(gameRoom, BlockColor.BLACK, choiceBlock.getBlack()));

        receivedBlock.forEach(player::addBlock);

        // 조커를 가지고있을때
    }

    public Long createGameRoom(){
        GameRoom gameRoom = GameRoom.create();
        return gameRoomRepository.save(gameRoom).getId();
    }

    public Long createPlayer(String name, String webSocketSessionId) {
        Player player = Player.create(name, webSocketSessionId);
        return playerJpaRepository.save(player).getId();
    }

    public GameRoom findGameRoomById(Long id) {
        return gameRoomRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.GAME_ROOM_NOT_FOUND));
    }

    public Player findPlayerById(Long id) {
        return playerJpaRepository.findById(id)
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.PLAYER_NOT_FOUND));
    }

    @SendGameStatusByWebSocket
    public boolean joinGameRoom(GameRoom gameRoom, Player player){
        return gameRoom.joinPlayer(player);
    }

    private void choiceFirstBlocks(List<Player> playerList) {
        /**
         * 첫번째 플레이어에게 블록을 선택하도록 알림.
         * 인원수에따라 선택가능한 블록의 수도 달라짐. (3명이하 5개, 4명 4개)
         * 첫번째 플레이어가 블록 선택을 응답하면 흑과 백의 수만큼 GameRoom 에서 빼서 해당 플레이에의 패로 넣음.
         * 이후 다음 플레이어에게 블록을 선택하도록 알림.
         * 이 작업을 해당 방의 인원수만큼 반복하고 모든 인원이 진행했으면 게임 시작.
         */
    }

    private List<Block> getRandomBlock(GameRoom gameRoom, BlockColor blockColor, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> gameRoom.drawRandomBlock(blockColor))
                .collect(Collectors.toList());
    }
}
