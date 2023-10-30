package com.game.algo.algo.event;

import com.game.algo.algo.data.GameStatusUpdateCommand;
import com.game.algo.websocket.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameEventHandler {

    private final WebSocketService webSocketService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void sendUpdateCommand(GameStatusUpdateCommand gameStatusUpdateCommand) {
        gameStatusUpdateCommand.getSessionIdList().forEach(sessionId -> {
            try {
                webSocketService.updateCommand(sessionId);
            } catch (Exception e) {
                
            }
        });
    }
}
