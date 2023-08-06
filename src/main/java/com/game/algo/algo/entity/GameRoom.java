package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.game.algo.algo.data.GameServiceConst.*;

@Getter
@Entity
//@RedisHash(value = "game_manager")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Phase phase = Phase.WAIT;

    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL)
    private List<Player> playerList = new ArrayList<>();

    private Integer progressPlayerNumber = 0;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Block> whiteBlockList = null;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Block> blackBlockList = null;


    public static GameRoom create() {
        GameRoom gameRoom = new GameRoom();
        return gameRoom;
    }

    public void gameReset() {
        progressPlayerNumber = 0;
        blockReset();
    }

    public void joinPlayer(Player player) {
        if (playerList.size() >= PLAYER_MAX_COUNT){
            throw new GameLogicException(GameExceptionCode.GAME_ROOM_IS_FULL);
        }

        List<Player> playerListEdit = new ArrayList<>(playerList);
        playerListEdit.add(player);
        player.joinGameRoom(this);
        playerList = playerListEdit;
    }

    public boolean areAllPlayersReady() {
        return playerList.stream().allMatch(Player::isReady);
    }

    public void allPlayerReadyOff() {
        playerList.forEach(player -> player.updateReady(false));
    }

    public void nextPlayer() {
        if (++progressPlayerNumber == playerList.size()) {
            progressPlayerNumber = 0;
        }
    }

    public Player getProgressPlayer() {
        return playerList.get(progressPlayerNumber);
    }

    public void playerOrderReset() {
        List<Player> playerOrderList = playerList.stream()
                .sorted(Comparator.comparing(player -> Math.random()))
                .collect(Collectors.toList());

        IntStream.range(0, playerOrderList.size())
                .forEach(i -> playerOrderList.get(i).updateOrder(i+1));
    }

    public Block drawRandomBlock(BlockColor blockColor) {
        double randomValue = Math.random();

        if (blockColor == BlockColor.WHITE) {
            return (whiteBlockList.size() != 0)
                    ? whiteBlockList.remove((int)(randomValue * whiteBlockList.size()))
                    : blackBlockList.remove((int)(randomValue * blackBlockList.size()));
        } else {
            return (blackBlockList.size() != 0)
                    ? blackBlockList.remove((int)(randomValue * blackBlockList.size()))
                    : whiteBlockList.remove((int)(randomValue * whiteBlockList.size()));
        }
    }

    public void updatePhase(Phase phase) {
        this.phase = phase;
    }

    public void addJoker() {
        whiteBlockList.add(Block.createBlock(BlockColor.WHITE, 12));
        blackBlockList.add(Block.createBlock(BlockColor.BLACK, 12));
    }

    private void blockReset() {
        whiteBlockList = blockSet(BlockColor.WHITE);
        blackBlockList = blockSet(BlockColor.BLACK);
    }

    private List<Block> blockSet(BlockColor blockColor) {
        return IntStream.range(0, 12)
                .mapToObj(num -> Block.createBlock(blockColor, num))
                .collect(Collectors.toList());
    }

    public enum Phase {
        WAIT, // 게임 시작 전
        SETTING, // 게임 세팅 (블럭 리셋, 플레이어 순서 지정)
        START, // 시작, 진행순서대로 블록을 뽑고 이후 게임 시작
        CONTROL, // 플레이어의 차례를 순서대로 바꿈
        DRAW, // 블록을 하나 선택함
        SORT, // 뽑은 블록을 정렬함 ( 조커 고려 )
        GUESS, // 뽑은 블록을 두고 추리함
        REPEAT, // 추리 성공 여부에따라 더할지 결정함
        END
    }
}
