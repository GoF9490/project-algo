package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.global.audit.Auditable;
import com.game.algo.global.converter.BlockArrayConverter;
import com.querydsl.core.annotations.QueryEntity;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
//@RedisHash(value = "player")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class Player extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // or Member 객체

    @Builder.Default
    private boolean ready = false;

    @Builder.Default
    private boolean retire = false;

    @Setter
    private String webSocketSessionId; // 대안 필요(?)

    @ManyToOne
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    private int orderNumber;

    @Convert(converter = BlockArrayConverter.class)
    @Builder.Default
    private List<Block> blockList = new ArrayList<>();

    @Builder.Default
    private Integer drawBlockIndexNum = -1;

    @Builder.Default
    private Integer whiteJokerRange = 12; // startNum * 100 + endNum

    @Builder.Default
    private Integer blackJokerRange = 12; // startNum * 100 + endNum


    public static Player create(String name, String webSocketSessionId) {
        return Player.builder()
                .name(name)
                .webSocketSessionId(webSocketSessionId)
                .build();
    }

    public void joinGameRoom(GameRoom gameRoom){
        this.gameRoom = gameRoom;
        orderNumber = gameRoom.getPlayerList().size() - 1;
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
        ready = false;
        retire = false;
        // orderNumber = 0;
        blockList = new ArrayList<>();
        drawBlockIndexNum = -1;
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
        blockList = new ArrayList<>(blockList);
    }

    public void updateJokerIndex(int index, BlockColor jokerColor) {
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

    public boolean guessBlock(int index, int num) {
        Block findBlock = blockList.get(index);
        if (findBlock.getNum() == num) {
            findBlock.open();
            blockList = new ArrayList<>(blockList);
            checkRetire();
            return true;
        }
        return false;
    }

    public void openDrawCard() {
        getDrawBlock().open();
        blockList = new ArrayList<>(blockList);
    }

    public void exit() {
        if (gameRoom != null) {
            gameRoom.removePlayer(this);
            gameRoom = null;
        }
    }

    public void disconnect() {
        webSocketSessionId = "disconnect";
        name = "disconnect";
        retire = true;
        blockList.forEach(Block::open);
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

    private Block getDrawBlock() {
        return blockList.get(drawBlockIndexNum);
    }

    private void checkRetire() {
        if (blockList.stream().noneMatch(Block::isClose)) {
            retire = true;
        }
    }
}
