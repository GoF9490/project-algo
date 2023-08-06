package com.game.algo.websocket.dto;

import com.game.algo.websocket.data.MessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageDataResponse<T> {

    private MessageType type;

    private T message;


    public static MessageDataResponse create(MessageType type, Object message){
        return new MessageDataResponse(
                type,
                message);
    }
}
