package com.game.algo.algo.controller;

import com.game.algo.algo.dto.GameManagerCreate;
import com.game.algo.algo.dto.ResponseData;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/game")
public class GameController {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity gameCreate(@RequestBody GameManagerCreate gameManagerCreate){
        Player findPlayer = gameService.findPlayerById(gameManagerCreate.getPlayerId());
        Long gameManagerId = gameService.createGameManager(findPlayer);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData(200, gameManagerId));
    }
}
