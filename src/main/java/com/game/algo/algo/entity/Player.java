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

    private Integer drawBlockIndexNum = -1;

    private Integer whiteJokerRange = 12; // startNum * 100 + endNum

    private Integer blackJokerRange = 12; // startNum * 100 + endNum


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
        whiteJokerRange = 12;
        blackJokerRange = 12;
    }

    public int findPosition(Block drawBlock) {
        Block findBlock = blockList.stream()
                .filter(block -> block.comparePosition(drawBlock))
                .findFirst()
                .orElse(null);

        if (findBlock == null) {
            return blockList.size();
        }
        return blockList.indexOf(findBlock);
    }

    public void addBlock(Block block) {
        int indexNum = findPosition(block);
        blockList.add(indexNum, block);
        setDrawBlockIndexNum(block);
    }

    public void changeJokerNum(int index, BlockColor jokerColor) {
        Block findDrawBlock = getDrawBlock();
        Block findJoker = findJoker(jokerColor);
        blockList.remove(findJoker);

        if (!betweenRange(findDrawBlock, findDrawBlock.isColor(BlockColor.WHITE) ? whiteJokerRange : blackJokerRange)) {
            throw new GameLogicException(GameExceptionCode.JOKER_NOT_MATCH);
        }

        int frontNum = (index == 0) ? 0 : blockList.get(index - 1).getNum();
        int backNum = (index >= blockList.size()) ? 12 : blockList.get(index).getNum();

        if (jokerColor == BlockColor.WHITE) {
            whiteJokerRange = frontNum * 100 + backNum;
        } else if (jokerColor == BlockColor.BLACK) {
            blackJokerRange = frontNum * 100 + backNum;
        }

        blockList.add(index, findJoker);

        blockList = new ArrayList<>(blockList);
        setDrawBlockIndexNum(findDrawBlock);
    }

    public void updateOrder(int order) {
        this.orderNumber = order;
    }

    public Block getDrawBlock() {
        return blockList.get(drawBlockIndexNum);
    }

    public boolean guessBlock(int index, int num) {
        Block findBlock = blockList.get(index);
        if (findBlock.getNum() == num) {
            findBlock.open();
            return true;
        }
        return false;
    }

    private Player(String name, String webSocketSessionId) {
        this.name = name;
        this.webSocketSessionId = webSocketSessionId;
    }

    private Block findJoker(BlockColor blockColor) {
        List<Block> findJoker = blockList.stream()
                .filter(block -> block.isColor(blockColor) && block.isJoker())
                .collect(Collectors.toList());

        if (findJoker.size() != 1) {
            throw new GameLogicException(GameExceptionCode.JOKER_NOT_MATCH);
        }

        return findJoker.get(0);
    }

    private boolean betweenRange(Block block, int jokerRange) {
        if (block.isJoker()) {
            return true;
        }
        return jokerRange / 100 <= block.getNum() && block.getNum() <= jokerRange % 100;
    }

    private void setDrawBlockIndexNum(Block block) {
        drawBlockIndexNum = blockList.indexOf(block);
    }
}
