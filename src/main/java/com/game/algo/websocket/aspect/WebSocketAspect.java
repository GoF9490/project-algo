package com.game.algo.websocket.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.websocket.dto.MessageDataResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class WebSocketAspect {

    private final ObjectMapper objectMapper;

    @Around("@annotation(com.game.algo.websocket.annotation.ResponseMessageData)")
    public Object sendGameInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        MessageDataResponse proceed = (MessageDataResponse) joinPoint.proceed();
        String json = objectMapper.writeValueAsString(proceed.getMessage());
        return MessageDataResponse.create(proceed.getType(), json);
    }
}
