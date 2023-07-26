package com.game.algo.websocket.data;

public enum MessageType {

    SessionId,
    PlayerCreate,
    PlayerSimple,
    GameRoomCreate,
    CreateRoomSuccess,
    JoinRoomSuccess,
    JoinRoomFail,
    GameRoomJoin,
    GameStatusData,
    Exception;
}
