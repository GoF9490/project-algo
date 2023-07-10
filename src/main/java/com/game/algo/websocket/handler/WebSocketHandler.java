package com.game.algo.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.algo.algo.service.GameService;
import com.game.algo.websocket.data.MessageDataType;
import com.game.algo.websocket.dto.MessageDataRequest;
import com.game.algo.websocket.dto.PlayerReadyUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandler extends TextWebSocketHandler {

//    private static final Logger LOG = Logger.getGlobal();

    private static final Map<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<>(); // 수정 가능성 높음

    private final GameService gameService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        CLIENTS.put(session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        CLIENTS.remove(session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
//        String input = message.getPayload();
//        LOG.info(num + " : " + input);
////        TextMessage textMessage = new TextMessage("hello. \n it's test." + num);
////        session.sendMessage(textMessage);
//
////        JSONParser jsonParser = new JSONParser();
////        Object parse = jsonParser.parse(input);
//        ObjectMapper objectMapper = new ObjectMapper();
//        TestObject testObject = objectMapper.readValue(input, TestObject.class);
//        System.out.println(testObject.toString());
//
//        String id = session.getId();
//        CLIENTS.entrySet().forEach( arg -> {
//            if (arg.getKey().equals(id)) {
//                try {
//                    arg.getValue().sendMessage(message);
//                } catch (IOException e) {
//                    System.out.println(e.getMessage());
//                }
//            }
//        });

        String id = session.getId();

        String input = message.getPayload();

        ObjectMapper objectMapper = new ObjectMapper();

        MessageDataRequest messageDataRequest = objectMapper.readValue(input, MessageDataRequest.class);

        MessageDataType requestMessageType = messageDataRequest.getType();
        String requestMessage = messageDataRequest.getMessage();

        log.info("MessageData : id:{} / type:{} / message:{}", id, requestMessageType, requestMessage);

        switch (requestMessageType) {
            case PlayerReadyUpdate:
                PlayerReadyUpdate playerReadyUpdate = objectMapper.readValue(requestMessage, PlayerReadyUpdate.class);
                gameService.testLogging(playerReadyUpdate.getName());
                break;
        }

    }
}
