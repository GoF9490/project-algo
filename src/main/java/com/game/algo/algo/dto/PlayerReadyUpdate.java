package com.game.algo.algo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerReadyUpdate {

    private Integer id;

    private String name;

    private Integer gameManagerId;

    private Boolean ready;
}
