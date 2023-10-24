package com.game.algo.algo.controller;

import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.dto.request.GameRoomCreate;
import com.game.algo.algo.dto.response.GameRoomFind;
import com.game.algo.global.dto.ResponseData;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @PostMapping("/")
    public ResponseEntity createGameRoom(@RequestBody GameRoomCreate gameRoomCreate){

        Player findPlayer = gameService.findPlayerById(gameRoomCreate.getPlayerId());
        Long gameRoomId = gameService.createGameRoom("asdf");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseData.create(200, gameRoomId));
    }

    @PatchMapping("/join/{gameRoomId}")
    public ResponseEntity joinGameRoom(@PathVariable Long gameRoomId,
                                   @RequestBody Long playerId) {

        gameService.joinGameRoom(gameRoomId, playerId);

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/")
    public ResponseEntity findGameRooms(@RequestParam int page,
                                        @RequestParam boolean start) {
        GameRoomFind gameRoomFind = gameService.findGameRoomsNotGameStart(page, GameProperty.FIND_GAME_ROOM_SIZE);
        return null;
    }
}
