package com.game.algo.websocket.data;

public enum MessageType {

    Version,
    SessionId,
    WaitTimeForPhase,
    PlayerCreate,
    PlayerSimple,
    GameRoomCreate,
    CreateRoomSuccess,
    JoinRoomSuccess,
    JoinRoomFail, // 재검토 필요
    GameRoomJoin,
    PlayerReadyUpdate,
    GameStart,
    StartBlockDraw,
    BlockDraw,
    NextPhase,
    WaitForSec,
    GameStatusData,
    OwnerBlockData,
    Exception;
}
