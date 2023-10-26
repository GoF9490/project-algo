package com.game.algo.algo.controller;

import com.game.algo.algo.data.BlockColor;
import com.game.algo.algo.dto.request.*;
import com.game.algo.algo.dto.response.PlayerSimple;
import com.game.algo.algo.entity.GameRoom;
import com.game.algo.algo.entity.Player;
import com.game.algo.algo.service.GameRoomService;
import com.game.algo.algo.service.PlayerService;
import com.game.algo.global.dto.ResponseData;
import com.game.algo.algo.service.GameService;
import com.game.algo.global.property.GlobalProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algo/player")
public class PlayerController {

    private final PlayerService playerService;
    private final GameRoomService gameRoomService;

    @PostMapping("/")
    public ResponseEntity create(@RequestHeader("session-id") String sessionId,
                                 @RequestBody String name) {

        Long playerId = playerService.create(name, sessionId);
        return ResponseEntity.created(URI.create(GlobalProperty.URI + "/simple")).build();
    }

    @GetMapping("/simple")
    public ResponseEntity getSimple(@RequestHeader("session-id") String sessionId) {

        PlayerSimple simple = PlayerSimple.from(playerService.findByWebSocketSessionId(sessionId));
        return ResponseEntity.ok().body(ResponseData.create(200, simple));
    }

    @PostMapping("/join")
    public ResponseEntity joinGameRoom(@RequestHeader("session-id") String sessionId,
                                       @RequestBody GameRoomJoin join) {

        playerService.joinGameRoom(sessionId, join.getGameRoomId());
        return ResponseEntity.ok().build();

//        sendGameStatusData(findGameRoom);
    }

    @PostMapping("/exit")
    public ResponseEntity exitGameRoom(@RequestHeader("session-id") String sessionId) {

        playerService.exitGameRoom(sessionId);
        return ResponseEntity.ok().build();

//        sendGameStatusData(gameService.findGameRoomById(gameRoomId));
    }

    @PostMapping("/ready")
    public ResponseEntity updatePlayerReady(@RequestHeader("session-id") String sessionId,
                                            @RequestBody Boolean ready) {

        playerService.updatePlayerReady(sessionId, ready);
        return ResponseEntity.ok().build();

//        sendGameStatusData(findGameRoom);
    }

    // GameLogic를 따로 만드는 편이 좋을까
    @PostMapping("/draw/start")
    public ResponseEntity drawBlockAtStart(@RequestHeader("session-id") String sessionId,
                                           @RequestBody StartBlockDraw blockDraw) {

        playerService.drawBlockAtStart(sessionId, blockDraw.getWhiteBlockCount(), blockDraw.getBlackBlockCount());
        gameRoomService.endStartPhase(blockDraw.getGameRoomId(), sessionId);

        return ResponseEntity.ok().build();

        // endStartPhase(startBlockDraw.getGameRoomId(), playerOrderNum);
    }

    @PostMapping("/draw")
    public ResponseEntity drawBlockAtDrawPhase(@RequestHeader("session-id") String sessionId,
                                               @RequestBody BlockDraw blockDraw) {

        playerService.drawBlockAtDrawPhase(sessionId, blockDraw.getBlockColor());
        gameRoomService.endDrawPhase(blockDraw.getGameRoomId(), sessionId);

        return ResponseEntity.ok().build();

        // endDrawPhase(blockDraw.getGameRoomId(), playerOrderNum);
    }

    @PostMapping("/joker")
    public ResponseEntity updateJoker(@RequestHeader("session-id") String sessionId,
                                      @RequestBody JokerUpdate jokerUpdate) {

        playerService.updatePlayerJoker(sessionId, jokerUpdate.getIndex(), jokerUpdate.getBlockColor());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/guess")
    public ResponseEntity guessBlock(@RequestHeader("session-id") String sessionId,
                                     @RequestBody BlockGuess blockGuess) {

        playerService.guessBlock(sessionId, blockGuess.getTargetPlayerId(), blockGuess.getBlockIndex(), blockGuess.getBlockNum());
        gameRoomService.endGuessPhase(blockGuess.getGameRoomId(), sessionId);

        return ResponseEntity.ok().build();

        // endGuessPhase(blockGuess.getGameRoomId(), playerOrderNum);
    }

    @PostMapping("/repeat")
    public ResponseEntity choiceRepeatGuess(@RequestHeader("session-id") String sessionId,
                                            @RequestBody GuessRepeat guessRepeat) {

        gameRoomService.endRepeatPhase(guessRepeat.getGameRoomId(), sessionId, guessRepeat.isRepeatGuess());
        return ResponseEntity.ok().build();

        // endRepeatPhase(guessRepeat.getGameRoomId(), findPlayer.getOrderNumber(), guessRepeat.isRepeatGuess());
    }
}
