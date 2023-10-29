package com.game.algo.algo.controller;

import com.game.algo.algo.dto.request.GameRoomCreate;
import com.game.algo.algo.dto.response.*;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.algo.service.GameRoomService;
import com.game.algo.algo.service.PlayerService;
import com.game.algo.global.dto.ResponseData;
import com.game.algo.algo.entity.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@CrossOrigin(origins = {"http://localhost", "http://project-algo.s3-website.ap-northeast-2.amazonaws.com",
        "http://codestates-prac-stackoverflow.s3-website.ap-northeast-2.amazonaws.com"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algo/gameroom")
public class GameRoomController {

    private final PlayerService playerService;
    private final GameRoomService gameRoomService;

    @PostMapping("")
    public ResponseEntity createAndJoinGameRoom(@RequestHeader("Session-Id") String sessionId,
                                                @RequestBody GameRoomCreate gameRoomCreate){

        if (playerService.findByWebSocketSessionId(sessionId).getGameRoom() != null) { // 검증절차. 따로 구성하는게 좋을수도.
            throw new GameLogicException(GameExceptionCode.ALREADY_JOIN);
        }
        Long gameRoomId = gameRoomService.create(gameRoomCreate.getTitle());
        playerService.joinGameRoom(sessionId, gameRoomId);
        GameStatusData gameStatusData = GameStatusData.from(gameRoomService.findById(gameRoomId));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.create(200, gameStatusData));
    }

    @GetMapping("")
    public ResponseEntity findGameRooms(@RequestParam("p") int page,
                                        @RequestParam("start") boolean start) {
        List<GameRoomSimple> simpleList = gameRoomService.findSimpleListByStart(page, start);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, simpleList));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity gameStart(@RequestHeader("Session-Id") String sessionId,
                                    @PathVariable("id") Long gameRoomId) {

        playerService.validSessionIdInGameRoom(sessionId, gameRoomId);
        gameRoomService.gameStart(gameRoomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/over/setting")
    public ResponseEntity overSettingPhase(@RequestHeader("Session-Id") String sessionId,
                                           @PathVariable("id") Long gameRoomId) {

        gameRoomService.endSettingPhase(gameRoomId, sessionId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/over/start")
    public ResponseEntity overStartPhase(@RequestHeader("Session-Id") String sessionId,
                                         @PathVariable("id") Long gameRoomId) {

        gameRoomService.endStartPhase(gameRoomId, sessionId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/over/draw")
    public ResponseEntity overDrawPhase(@RequestHeader("Session-Id") String sessionId,
                              @PathVariable("id") Long gameRoomId) {

        gameRoomService.endDrawPhase(gameRoomId, sessionId);
        DrawBlockData drawBlockData = DrawBlockData.from(playerService.findByWebSocketSessionId(sessionId));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, drawBlockData));
    }

    @PostMapping("/{id}/over/sort")
    public ResponseEntity overSortPhase(@RequestHeader("Session-Id") String sessionId,
                                        @PathVariable("id") Long gameRoomId) {

        gameRoomService.endSortPhase(gameRoomId, sessionId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/over/guess")
    public ResponseEntity overGuessPhase(@RequestHeader("Session-Id") String sessionId,
                                        @PathVariable("id") Long gameRoomId) {

        gameRoomService.endGuessPhase(gameRoomId, sessionId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/over/repeat")
    public ResponseEntity overRepeatPhase(@RequestHeader("Session-Id") String sessionId,
                                         @PathVariable("id") Long gameRoomId) {

        gameRoomService.endRepeatPhase(gameRoomId, sessionId, false);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/over/end")
    public ResponseEntity overEndPhase(@RequestHeader("Session-Id") String sessionId,
                             @PathVariable("id") Long gameRoomId) {

        gameRoomService.endEndPhase(gameRoomId, sessionId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/over/gameover")
    public ResponseEntity overGameOverPhase(@RequestHeader("Session-Id") String sessionId,
                                            @PathVariable("id") Long gameRoomId) {

        gameRoomService.endGameOverPhase(gameRoomId, sessionId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // getData

//    @PostMapping("/{id}/command/update")
//    public ResponseEntity updateCommandInGameRoom(@RequestHeader("Session-Id") String sessionId,
//                                                  @PathVariable("id") Long gameRoomId) {
//
//        playerService.sendGameStatusUpdateCommand(sessionId, gameRoomId);
//        return ResponseEntity.status(HttpStatus.OK).build();
//    }

    @GetMapping("/data")
    public ResponseEntity getGameStatusData(@RequestHeader("Session-Id") String sessionId) {
        Player findPlayer = playerService.findByWebSocketSessionId(sessionId);

        return ResponseEntity.status(HttpStatus.OK)
                .cacheControl(CacheControl.maxAge(1L, TimeUnit.SECONDS))
                .body(ResponseData.create(200, ResponseGameData.from(findPlayer)));
    }
}
