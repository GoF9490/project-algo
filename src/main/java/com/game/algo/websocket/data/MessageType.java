package com.game.algo.websocket.data;

public enum MessageType {

    Version,
    SessionId,
    PlayerCreate,
    SetSessionId,
    PlayerSimple,
    GameRoomCreate,
    CreateRoomSuccess,
    JoinRoomSuccess,
    JoinRoomFail, // 재검토 필요
    GameRoomJoin,
    GameRoomFind,
    GameRoomExit,
    PlayerReadyUpdate,
    GameStart,
    StartBlockDraw,
    BlockDraw,
    JokerUpdate, // 핸들러에서 받아서 업데이트
    BlockGuess,
    GuessRepeat,
    NextPhase,
    WaitForSec,
    DrawBlockData, // draw 페이즈 끝날때 만들어서 보내기
    GameStatusData,
    OwnerBlockData,
    Exception;
}
