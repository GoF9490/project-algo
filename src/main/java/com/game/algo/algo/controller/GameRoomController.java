package com.game.algo.algo.controller;

import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.dto.request.GameRoomCreate;
import com.game.algo.algo.dto.response.GameRoomFind;
import com.game.algo.algo.dto.response.GameRoomSimple;
import com.game.algo.algo.service.GameRoomService;
import com.game.algo.algo.service.PlayerService;
import com.game.algo.global.dto.ResponseData;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GameRoomController {

    private final PlayerService playerService;
    private final GameRoomService gameRoomService;

    @PostMapping("/")
    public ResponseEntity createGameRoom(@RequestBody GameRoomCreate gameRoomCreate){

        Long gameRoomId = gameRoomService.create(gameRoomCreate.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.create(200, gameRoomId));
    }

    @PatchMapping("/join/{gameRoomId}")
    public ResponseEntity joinGameRoom(@PathVariable Long gameRoomId,
                                   @RequestBody Long playerId) {

        playerService.joinGameRoom(gameRoomId, playerId);

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/")
    public ResponseEntity findGameRooms(@RequestParam int page,
                                        @RequestParam boolean start) {
        List<GameRoomSimple> simpleListByStart = gameRoomService.findSimpleListByStart(page, start);
        return ResponseEntity.status(HttpStatus.OK)
                .body(simpleListByStart);
    }
}
