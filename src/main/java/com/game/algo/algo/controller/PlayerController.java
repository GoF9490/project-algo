package com.game.algo.algo.controller;

import com.game.algo.algo.dto.request.GameRoomJoin;
import com.game.algo.algo.dto.request.PlayerCreate;
import com.game.algo.algo.dto.request.PlayerReadyUpdate;
import com.game.algo.algo.dto.response.PlayerSimple;
import com.game.algo.algo.service.PlayerService;
import com.game.algo.global.dto.ResponseData;
import com.game.algo.algo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algo/player")
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody PlayerCreate playerCreate) {
        Long playerId = playerService.create(playerCreate.getName(), playerCreate.getSessionId());
        PlayerSimple playerSimple = PlayerSimple.from(playerService.findById(playerId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.create(200, playerSimple));
    }

    @PatchMapping("/join")
    public ResponseEntity joinGameRoom(@RequestHeader("session-id") String sessionId,
                                       @RequestBody GameRoomJoin join) {

        playerService.joinGameRoom(sessionId, join.getGameRoomId());
        return ResponseEntity.status(HttpStatus.OK).build();

//        sendGameStatusData(findGameRoom);
    }

    @PatchMapping("/exit")
    public ResponseEntity exitGameRoom(@RequestHeader("session-id") String sessionId) {
        playerService.exitGameRoom(sessionId);
        return ResponseEntity.status(HttpStatus.OK).build();

//        sendGameStatusData(gameService.findGameRoomById(gameRoomId));
    }

    @PatchMapping("/ready")
    public ResponseEntity updatePlayerReady(@RequestHeader("session-id") String sessionId,
                                            @RequestBody Boolean ready) {
        playerService.updatePlayerReady(sessionId, ready);
        return ResponseEntity.status(HttpStatus.OK).build();

//        sendGameStatusData(findGameRoom);
    }
}
