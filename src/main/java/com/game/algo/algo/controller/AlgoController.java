package com.game.algo.algo.controller;

import com.game.algo.algo.data.GameProperty;
import com.game.algo.algo.dto.request.CheckVersion;
import com.game.algo.algo.dto.request.GameRoomCreate;
import com.game.algo.algo.dto.response.GameRoomSimple;
import com.game.algo.algo.exception.GameExceptionCode;
import com.game.algo.algo.exception.GameLogicException;
import com.game.algo.global.dto.ResponseData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algo")
public class AlgoController {

    @PostMapping("")
    public ResponseEntity checkVersion(@RequestParam("v") String version){

        if (!GameProperty.VERSION.equals(version)) {
            throw new GameLogicException(GameExceptionCode.INVALID_VERSION);
        }

        return ResponseEntity.ok()
                .body(ResponseData.create(200, GameRoomSimple.create(1L, "hello", 1)));
    }
}
