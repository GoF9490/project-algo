package com.game.algo.websocket.dto;

import com.game.algo.websocket.data.MessageDataType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageDataRequest {

    private MessageDataType type;

    private String message;
}
