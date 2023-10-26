package com.game.algo.algo.controller;

import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.dto.request.GameRoomCreate;
import com.game.algo.algo.dto.request.GameStart;
import com.game.algo.algo.dto.response.*;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.service.GameRoomService;
import com.game.algo.algo.service.PlayerService;
import com.game.algo.global.dto.ResponseData;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.service.GameService;
import com.game.algo.websocket.service.WebSocketService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algo/gameroom")
public class GameRoomController {

    private final PlayerService playerService;
    private final GameRoomService gameRoomService;

    @PostMapping("/")
    public ResponseEntity createGameRoom(@RequestBody GameRoomCreate gameRoomCreate){

        Long gameRoomId = gameRoomService.create(gameRoomCreate.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.create(200, gameRoomId));
    }

    @GetMapping("/")
    public ResponseEntity findGameRooms(@RequestParam int page,
                                        @RequestParam boolean start) {
        List<GameRoomSimple> simpleList = gameRoomService.findSimpleListByStart(page, start);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, simpleList));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity gameStart(@RequestHeader("session-id") String sessionId,
                          @PathVariable("id") Long gameRoomId) {

        playerService.validSessionIdInGameRoom(sessionId, gameRoomId);
        gameRoomService.gameStart(gameRoomId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{id}/over/setting")
    public ResponseEntity overSettingPhase(@RequestHeader("session-id") String sessionId,
                                           @PathVariable("id") Long gameRoomId) {

        gameRoomService.endSettingPhase(gameRoomId, sessionId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/over/start")
    public ResponseEntity overStartPhase(@RequestHeader("session-id") String sessionId,
                                         @PathVariable("id") Long gameRoomId) {

        gameRoomService.endStartPhase(gameRoomId, sessionId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/over/draw")
    public ResponseEntity overDrawPhase(@RequestHeader("session-id") String sessionId,
                              @PathVariable("id") Long gameRoomId) {

        gameRoomService.endDrawPhase(gameRoomId, sessionId);
        DrawBlockData drawBlockData = DrawBlockData.from(playerService.findByWebSocketSessionId(sessionId));

        return ResponseEntity.ok()
                .body(ResponseData.create(200, drawBlockData));
    }

    @PostMapping("/{id}/over/sort")
    public ResponseEntity overSortPhase(@RequestHeader("session-id") String sessionId,
                                        @PathVariable("id") Long gameRoomId) {

        gameRoomService.endSortPhase(gameRoomId, sessionId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/over/guess")
    public ResponseEntity overGuessPhase(@RequestHeader("session-id") String sessionId,
                                        @PathVariable("id") Long gameRoomId) {

        gameRoomService.endGuessPhase(gameRoomId, sessionId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/over/repeat")
    public ResponseEntity overRepeatPhase(@RequestHeader("session-id") String sessionId,
                                         @PathVariable("id") Long gameRoomId) {

        gameRoomService.endRepeatPhase(gameRoomId, sessionId, false);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/over/end")
    public ResponseEntity overEndPhase(@RequestHeader("session-id") String sessionId,
                             @PathVariable("id") Long gameRoomId) {

        gameRoomService.endEndPhase(gameRoomId, sessionId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/over/over")
    public ResponseEntity overGameOverPhase(@RequestHeader("session-id") String sessionId,
                                            @PathVariable("id") Long gameRoomId) {

        gameRoomService.endGameOverPhase(gameRoomId, sessionId);

        return ResponseEntity.ok().build();
    }

    // getData

    @GetMapping("/data")
    public ResponseEntity getGameStatusData(@RequestHeader("session-id") String sessionId) {
        Player findPlayer = playerService.findByWebSocketSessionId(sessionId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, ResponseGameData.from(findPlayer)));
    }
}
