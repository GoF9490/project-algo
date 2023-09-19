package com.game.algo.algo.controller;

import com.game.algo.algo.dto.request.PlayerCreate;
import com.game.algo.algo.dto.response.PlayerSimple;
import com.game.algo.global.dto.ResponseData;
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
@RequestMapping("/api/player")
public class PlayerController {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity create(@RequestBody PlayerCreate playerCreate) {
        Long playerId = gameService.createPlayer(playerCreate.getName(), playerCreate.getSessionId());
        PlayerSimple playerSimple = PlayerSimple.from(gameService.findPlayerById(playerId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseData(200, playerSimple));
    }
}
