package com.game.algo.algo.controller;

import com.game.algo.algo.dto.messagetype.GameRoomCreate;
import com.game.algo.algo.dto.ResponseData;
import com.game.algo.algo.entity.GameRoom;
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

    @PostMapping("/create")
    public ResponseEntity gameCreate(@RequestBody GameRoomCreate gameRoomCreate){
        Player findPlayer = gameService.findPlayerById(gameRoomCreate.getPlayerId());
        Long gameRoomId = gameService.createGameRoom();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData(200, gameRoomId));
    }

    @PatchMapping("/join/{gameRoomId}")
    public ResponseEntity joinGame(@PathVariable Long gameRoomId,
                                   @RequestBody Long playerId) {

        gameService.joinGameRoom(gameRoomId, playerId);

        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }
}
