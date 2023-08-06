package com.game.algo.algo.dto;

import com.game.algo.websocket.data.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MultipleMessageSupporter<T> {

    private List<String> sessionIdList = new ArrayList<>();

    private MessageType messageType;

    private T data;

    public static <T> MultipleMessageSupporter<T> create(List<String> sessionIdList, MessageType messageType, T data) {
        return new MultipleMessageSupporter<>(sessionIdList, messageType, data);
    }
}
