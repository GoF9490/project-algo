package com.game.algo.algo.data;

import lombok.Getter;

import javax.persistence.Embeddable;

@Getter
@Embeddable
public class BlackJokerRange extends JokerRange{

    public BlackJokerRange(int frontNum, int backNum) {
        super(frontNum, backNum);
    }
}
