package com.game.algo.algo.entity;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.data.JokerRange;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.web.socket.WebSocketSession;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@RedisHash(value = "player")
public class Player {

    @Id
    private Long id;

    private String name; // or Member 객체

    private WebSocketSession webSocketSession;

    private List<Block> blockList = new ArrayList<>();

    private JokerRange whiteJokerRange;

    private JokerRange blackJokerRange;
    
    private boolean needWhiteJokerRelocation = false;

    private boolean needBlackJokerRelocation = false;
    
    // 조커 재배치 메서드 만들기


//    @Builder
//    public Player(String name) {
//        this.name = name;
//    }

    private Player(String name, WebSocketSession webSocketSession) {
        this.name = name;
        this.webSocketSession = webSocketSession;
    }

    public static Player create(String name, WebSocketSession webSocketSession) {
        return new Player(name, webSocketSession);
    }

    public void gameReset() {
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
    }

    public void updateJoker(int frontNum, int backNum, BlockColor jokerColor) {
        Block findJoker = findJoker(backNum, jokerColor);

        if (jokerColor == BlockColor.WHITE && needWhiteJokerRelocation) {
            findJoker.setNum(backNum);
            whiteJokerRange = new JokerRange(frontNum, backNum);
            needWhiteJokerRelocation = false;
        } else if (jokerColor == BlockColor.BLACK && needBlackJokerRelocation) {
            findJoker.setNum(backNum);
            blackJokerRange = new JokerRange(frontNum, backNum);
            needBlackJokerRelocation = false;
        } else {
            throw new GameLogicException(GameExceptionCode.JOKER_ALREADY_CHANGED);
        }

        blockList = new ArrayList<>(blockList);
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

    private boolean betweenRange(Block block, JokerRange jokerRange) {
        return jokerRange.getFrontNum() <= block.getNum() && block.getNum() < jokerRange.getBackNum();
    }

    private void distinguishJoker(Block block) {
        if (block.isJoker()) {
            if (block.isWhite()) {
                whiteJokerRange = new JokerRange(0, 12);
                needWhiteJokerRelocation = true;
            } else {
                blackJokerRange = new JokerRange(0, 12);
                needBlackJokerRelocation = true;
            }
        }
    }
}
