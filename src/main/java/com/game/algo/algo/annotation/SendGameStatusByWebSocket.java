package com.game.algo.algo.annotation;

import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * 해당 어노테이션이 붙는 메서드의 첫번째 인자를 GameRoom으로 배치해야 합니다.
 * 어노테이션이 붙은 메서드가 끝난 후, 메서듸의 첫번째 인자를 토대로 GameRoomAspect의 메서드가 호출되어,
 * GameRoom의 Player들에게 변경된 정보를 WebSocket을 통해 전달합니다.
 *
 * Please set GameRoom as the first arg of the method.
 * After the annotated method ends, the changed information is delivered to the Players of GameRoom through WebSocket.
 */
@Inherited // 상속시 유지
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SendGameStatusByWebSocket {
}
