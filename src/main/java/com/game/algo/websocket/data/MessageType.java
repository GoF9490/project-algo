package com.game.algo.websocket.data;

public enum MessageType {

    SessionId,
    PlayerCreate,
    PlayerSimple,
    GameRoomCreate,
    CreateRoomSuccess,
    JoinRoomSuccess,
    JoinRoomFail, // 재검토 필요
    GameRoomJoin,
    PlayerReadyUpdate,
    GameStatusData,
    Exception;
}
