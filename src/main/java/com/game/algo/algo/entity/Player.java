package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
//@RedisHash(value = "player")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // or Member 객체

    private boolean ready = false;

    private boolean retire = false;

    private String webSocketSessionId; // 대안 필요(?)

    @ManyToOne
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    private int orderNumber;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<Block> blockList = new ArrayList<>();

    private Integer drawBlockIndexNum;

    private Integer whiteJokerRange; // startNum * 100 + endNum

    private Integer blackJokerRange; // startNum * 100 + endNum

    private boolean needWhiteJokerRelocation = false;

    private boolean needBlackJokerRelocation = false;
    
    // 조커 재배치 메서드 만들기


//    @Builder
//    public Player(String name) {
//        this.name = name;
//    }

    public static Player create(String name, String webSocketSessionId) {
        return new Player(name, webSocketSessionId);
    }

    public void joinGameRoom(GameRoom gameRoom){
        this.gameRoom = gameRoom;
    }

    public List<Integer> getBlockListCode(boolean isOwner) {
        return blockList.stream()
                .map(block -> block.getBlockCode(isOwner))
                .collect(Collectors.toList());
    }

    public void updateReady(boolean ready) {
        this.ready =  ready;
    }

    public void gameReset() {
        orderNumber = 0;
        blockList = new ArrayList<>();
        whiteJokerRange = null;
        blackJokerRange = null;
    }

    public void sortBlock() {
        blockList =  blockList.stream()
                .sorted((a, b) -> {
                    if(Objects.equals(a.getNum(), b.getNum())) {
                        return a.getTypeNumber() - b.getTypeNumber();
                    } else {
                        return a.getNum() - b.getNum();
                    }
                })
                .collect(Collectors.toList());
    }

    public void addBlock(Block block) {
        distinguishJoker(block);
        exploreJokerRange(block);

        blockList.add(block);
        sortBlock();
        setDrawBlockIndexNum(block);
    }

    public void updateJoker(int frontNum, int backNum, BlockColor jokerColor) {
        Block findJoker = findJoker(backNum, jokerColor);

        if (jokerColor == BlockColor.WHITE && needWhiteJokerRelocation) {
            findJoker.setNum(backNum);
            whiteJokerRange = frontNum * 100 + backNum;
            needWhiteJokerRelocation = false;
        } else if (jokerColor == BlockColor.BLACK && needBlackJokerRelocation) {
            findJoker.setNum(backNum);
            blackJokerRange = frontNum * 100 + backNum;
            needBlackJokerRelocation = false;
        } else {
            throw new GameLogicException(GameExceptionCode.JOKER_ALREADY_CHANGED);
        }

        blockList = new ArrayList<>(blockList);
        sortBlock();
        setDrawBlockIndexNum(findJoker);
    }

    public void updateOrder(int order) {
        this.orderNumber = order;
    }

    public void completeWhiteJokerRelocation() {
        needWhiteJokerRelocation = false;
    }

    public void completeBlackJokerRelocation() {
        needBlackJokerRelocation = false;
    }

    private Player(String name, String webSocketSessionId) {
        this.name = name;
        this.webSocketSessionId = webSocketSessionId;
    }

    private Block findJoker(int backNum, BlockColor jokerColor) {
        List<Block> findJoker = blockList.stream()
                .filter(block -> block.isJoker(jokerColor))
                .collect(Collectors.toList());

        if (findJoker.size() != 1) {
            throw new GameLogicException(GameExceptionCode.JOKER_NOT_MATCH);
        }

        return findJoker.get(0);
    }

    private void exploreJokerRange(Block block) {
        if (whiteJokerRange != null && block.isWhite() && betweenRange(block, whiteJokerRange)) {
            needWhiteJokerRelocation = true;
        } else if (blackJokerRange != null && block.isBlack() && betweenRange(block, blackJokerRange)) {
            needBlackJokerRelocation = true;
        }
    }

    private boolean betweenRange(Block block, int jokerRange) {
        return jokerRange / 100 <= block.getNum() && block.getNum() < jokerRange % 100;
    }

    private void distinguishJoker(Block block) {
        if (block.isJoker()) {
            if (block.isWhite()) {
                whiteJokerRange = 12;
                needWhiteJokerRelocation = true;
            } else {
                blackJokerRange = 12;
                needBlackJokerRelocation = true;
            }
        }
    }

    private void setDrawBlockIndexNum(Block block) {
        drawBlockIndexNum = blockList.indexOf(block);
    }
}
