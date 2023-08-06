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

@Aspect
@Component
@RequiredArgsConstructor
public class GameRoomAspect { // 필요성 재검토 요망

    private final WebSocketService webSocketService;
    private final ObjectMapper objectMapper;


    @Around("@annotation(com.game.algo.algo.annotation.SendGameStatusByWebSocket)")
    public Object sendGameInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        GameRoom gameRoom = (GameRoom) joinPoint.getArgs()[0];
        Object proceed = joinPoint.proceed();
        GameStatusData gameStatusData = GameStatusData.create(gameRoom);

//        webSocketService.sendGameStatusDataToPlayers(gameRoom.getPlayerList(), gameStatusData);
        return proceed;
    }
}
