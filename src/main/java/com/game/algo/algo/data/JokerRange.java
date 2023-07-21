package com.game.algo.algo.data;

import com.game.algo.algo.entity.Block;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class JokerRange {

    private int frontNum = 0;

    private int backNum = 12;

//    @Embeddable
//    public static class White extends JokerRange{
//        public White(int frontNum, int backNum) {
//            super(frontNum, backNum);
//        }
//    }
//
//    @Embeddable
//    public static class Black extends JokerRange{
//        public Black(int frontNum, int backNum) {
//            super(frontNum, backNum);
//        }
//    }
}
