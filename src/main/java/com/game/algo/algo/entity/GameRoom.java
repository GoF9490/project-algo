package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameServiceConst;
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
        phase = Phase.READY; // 고려
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
            return whiteBlockList.remove((int)(randomValue * whiteBlockList.size()));
        } else {
            return blackBlockList.remove((int)(randomValue * blackBlockList.size()));
        }
    }

    public void updatePhase(Phase phase) {
        this.phase = phase;
    }

    private void blockReset() {
        this.whiteBlockList = blockSet(BlockColor.WHITE);
        this.blackBlockList = blockSet(BlockColor.BLACK);
    }

    private List<Block> blockSet(BlockColor blockColor) {
        return IntStream.range(0, 13)
                .mapToObj(num -> Block.createBlock(blockColor, num))
                .collect(Collectors.toList());
    }

    public enum Phase {
        WAIT, // 게임 시작 전
        READY, // 게임 세팅, 이후 진행순서 정함
        START, // 시작, 진행순서대로 블록을 뽑고 이후 게임 시작
        CONTROL, // 플레이어의 차례를 순서대로 바꿈
        DRAW, // 블록을 하나 선택함
        SET,
        GUESS,
        REPEAT,
        END
    }
}
