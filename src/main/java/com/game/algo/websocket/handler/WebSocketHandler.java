package com.game.algo.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@Component
public class WebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOG = Logger.getGlobal();

    private static final Map<String, WebSocketSession> CLIENTS = new ConcurrentHashMap<>(); // 수정 가능성 높음

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
    }
}
