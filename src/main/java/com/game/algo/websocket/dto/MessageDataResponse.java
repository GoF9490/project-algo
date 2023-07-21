package com.game.algo.websocket.dto;

import com.game.algo.websocket.data.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDataResponse {

    private MessageType type;

    private String message;
}
