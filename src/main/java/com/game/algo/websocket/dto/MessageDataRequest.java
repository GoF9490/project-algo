package com.game.algo.websocket.dto;

import com.game.algo.websocket.data.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDataRequest {

    private MessageType type;

    private String message;
}
