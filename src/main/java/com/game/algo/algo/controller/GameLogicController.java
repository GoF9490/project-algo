package com.game.algo.algo.controller;

import com.game.algo.algo.dto.request.GameStart;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.service.GameRoomService;
import com.game.algo.algo.service.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algo/logic")
public class GameLogicController {

    private final PlayerService playerService;
    private final GameRoomService gameRoomService;
}
