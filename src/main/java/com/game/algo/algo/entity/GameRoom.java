package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.global.converter.BlockArrayConverter;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



@Getter
@Entity
//@RedisHash(value = "game_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(value = EnumType.STRING)
    @Builder.Default
    private Phase phase = Phase.WAIT; // phase wait 이면 게임시작전

    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Player> playerList = new ArrayList<>();

    @Builder.Default
    private Integer progressPlayerNumber = 0;

    @Convert(converter = BlockArrayConverter.class)
    @Builder.Default
    private List<Block> whiteBlockList = new ArrayList<>();

    @Convert(converter = BlockArrayConverter.class)
    @Builder.Default
    private List<Block> blackBlockList = new ArrayList<>();

    @Builder.Default
    private boolean gameStart = false;


    public static GameRoom create(String title) {
        return GameRoom.builder()
                .title(title)
                .build();
    }

    public void gameReset() {
        progressPlayerNumber = 0;
        blockReset();
    }

    public void joinPlayer(Player player) {
        checkVacancy();

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
        for (int i=0; i<playerList.size(); i++) {
            progressPlayerNumberUp();
            if (!getProgressPlayer().isRetire()){
                return;
            }
        }
    }

    public void progressZero() {
        progressPlayerNumber = 0;
    }

    public Player getProgressPlayer() {
        return playerList.stream() // 플레이어 뜯어 찾는 방법
                .filter(player -> player.getOrderNumber() == progressPlayerNumber)
                .findFirst()
                .orElseThrow(() -> new GameLogicException(GameExceptionCode.PLAYER_NOT_FOUND));
    }

    public void playerOrderReset() {
        List<Player> playerOrderList = playerList.stream()
                .sorted(Comparator.comparing(player -> Math.random()))
                .collect(Collectors.toList());

        IntStream.range(0, playerOrderList.size())
                .forEach(i -> {
                    playerOrderList.get(i).updateOrder(i);
                });
    }

    public Block drawRandomBlock(BlockColor blockColor) {
        double randomValue = Math.random();

        try {
            if (blockColor == BlockColor.WHITE) {
                return whiteBlockList.remove((int)(randomValue * whiteBlockList.size()));
            } else {
                return blackBlockList.remove((int)(randomValue * blackBlockList.size()));
            }
        } catch (Exception e) {
            throw new GameLogicException(GameExceptionCode.BLOCK_IS_DEPLETED);
        }
    }

    public void updatePhase(Phase phase) {
        this.phase = phase;
        if (phase == Phase.WAIT) {
            gameStart = false;
        } else {
            gameStart = true;
        }
    }

    public void addJoker() {
        if (whiteBlockList.stream().noneMatch(Block::isJoker)) {
            whiteBlockList.add(Block.create(BlockColor.WHITE, 12));
            whiteBlockList = new ArrayList<>(whiteBlockList);

            blackBlockList.add(Block.create(BlockColor.BLACK, 12));
            blackBlockList = new ArrayList<>(blackBlockList);
        }
    }

    public boolean isGameOver() {
        int leftPlayer = (int) playerList.stream()
                .filter(player -> !player.isRetire())
                .count();

        return leftPlayer == 1;
    }

    public void removePlayer(Player player) {
        playerList.remove(player);
        playerList = new ArrayList<>(playerList);
    }

    private void blockReset() {
        whiteBlockList = blockSet(BlockColor.WHITE);
        blackBlockList = blockSet(BlockColor.BLACK);
    }

    private List<Block> blockSet(BlockColor blockColor) {
        return IntStream.range(0, 12)
                .mapToObj(num -> Block.create(blockColor, num))
                .collect(Collectors.toList());
    }

    private void checkVacancy() {
        if (playerList.size() >= GameProperty.PLAYER_MAX_COUNT){
            throw new GameLogicException(GameExceptionCode.GAME_ROOM_IS_FULL);
        }

        if (gameStart) {
            throw new GameLogicException(GameExceptionCode.ALREADY_GAME_START);
        }
    }

    private void progressPlayerNumberUp() {
        if (++progressPlayerNumber == playerList.size()) {
            progressPlayerNumber = 0;
        }
    }


    public enum Phase {
        WAIT(0), // 게임 시작 전
        SETTING(5), // 게임 세팅 (블럭 리셋, 플레이어 순서 지정)
        START(20), // 시작, 진행순서대로 블록을 뽑고 이후 게임 시작
        DRAW(10), // 블록을 하나 선택함
        SORT(10), // 뽑은 블록을 정렬함 ( 조커 고려 )
        GUESS(30), // 뽑은 블록을 두고 추리함
        REPEAT(30), // 추리 성공 여부에따라 더할지 결정함
        END(5), // 현재 플레이어의 차례를 끝내고 다음 플레이어의 차례로 바꿈
        GAMEOVER(10);

        private int waitTime;

        Phase(int waitTime) {
            this.waitTime = waitTime;
        }

        public int getWaitTime() {
            return waitTime;
        }
    }
}
