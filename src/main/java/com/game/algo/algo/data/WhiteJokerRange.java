package com.game.algo.algo.data;

import lombok.Getter;

import javax.persistence.Embeddable;

@Getter
@Embeddable
public class WhiteJokerRange extends JokerRange{

    public WhiteJokerRange(int frontNum, int backNum) {
        super(frontNum, backNum);
    }
}
