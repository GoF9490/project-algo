package com.game.algo.algo.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.dto.GameStatusData;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.websocket.data.MessageType;
import com.game.algo.websocket.dto.MessageDataResponse;
import com.game.algo.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Aspect
@Component
@RequiredArgsConstructor
public class GameRoomAspect {

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;


    @Around("@annotation(com.game.algo.algo.annotation.SendGameStatusByWebSocket)")
    public Object sendGameInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        GameRoom gameRoom = (GameRoom) joinPoint.getArgs()[0];
        Object proceed = joinPoint.proceed();
        GameStatusData gameStatusData = GameStatusData.create(gameRoom);

        MessageDataResponse messageDataResponse = MessageDataResponse.create(MessageType.GameStatusData,
                objectMapper.writeValueAsString(gameStatusData));

        gameRoom.getPlayerList().forEach(player -> {
            try {
                webSocketService.sendMessageData(player.getWebSocketSessionId(), messageDataResponse);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return proceed;
    }
}
