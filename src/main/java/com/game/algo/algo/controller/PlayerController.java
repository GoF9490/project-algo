package com.game.algo.algo.controller;

import com.game.algo.algo.dto.request.*;
import com.game.algo.algo.dto.response.DrawBlockData;
import com.game.algo.algo.dto.response.GameStatusData;
import com.game.algo.algo.dto.response.PlayerSimple;
import com.game.algo.algo.service.GameRoomService;
import com.game.algo.algo.service.PlayerService;
import com.game.algo.global.dto.ResponseData;
import com.game.algo.global.property.GlobalProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@CrossOrigin(origins = {"http://localhost", "http://project-algo.s3-website.ap-northeast-2.amazonaws.com",
        "http://codestates-prac-stackoverflow.s3-website.ap-northeast-2.amazonaws.com"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PATCH})
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/algo/player")
public class PlayerController {

    private final PlayerService playerService;
    private final GameRoomService gameRoomService;

    @PostMapping("")
    public ResponseEntity create(@RequestHeader("Session-Id") String sessionId,
                                 @RequestBody @Valid PlayerCreate playerCreate) {

        Long playerId = playerService.create(playerCreate.getName(), sessionId);

        return ResponseEntity.created(URI.create(GlobalProperty.URI + "/simple"))
                .body(ResponseData.create(200, playerId));
    }

    /**
     * 보안이 취약함. 추후에 SessionId 와 WebSocketSessionId 를 분리해 사용해야 할듯.
     * 방안 1
     *   플레이어를 만들때 별도의 SessionId를 생성 후, WebSocket 커넥션 할때 SessionId - WebSocketSessionId 를 키 벨류로 묶기.
     *
     * 방안 2
     *   플레이어를 만들때 일회용 패스워드를 생성, WebSocketSessionId 세팅 후 해당 메서드를 호출할 때,
     *   쿼리파라미터로 패스워드를 같이 받아서 알맞은 플레이어의 SessionId를 불러오도록 함. 이후 패스워드 폐기.
     */
    @GetMapping("/{id}/session")
    public ResponseEntity getWebSessionId(@PathVariable("id") long id) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, playerService.findById(id).getWebSocketSessionId()));
    }

    @GetMapping("/simple")
    public ResponseEntity getSimple(@RequestHeader("Session-Id") String sessionId) {

        PlayerSimple simple = PlayerSimple.from(playerService.findByWebSocketSessionId(sessionId));
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, simple));
    }

    @PostMapping("/join")
    public ResponseEntity joinGameRoom(@RequestHeader("Session-Id") String sessionId,
                                       @RequestBody GameRoomJoin join) {

        playerService.joinGameRoom(sessionId, join.getGameRoomId());
        GameStatusData gameStatusData = GameStatusData.from(gameRoomService.findById(join.getGameRoomId()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, gameStatusData));
    }

    @PostMapping("/exit")
    public ResponseEntity exitGameRoom(@RequestHeader("Session-Id") String sessionId) {

        playerService.exitGameRoom(sessionId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/ready")
    public ResponseEntity updatePlayerReady(@RequestHeader("Session-Id") String sessionId) {

        playerService.reversePlayerReady(sessionId);
        return ResponseEntity.status(HttpStatus.OK)
                .build();
    }

    /**
     * drawBlock 로직 두개를 하나로 묶는 방안 검토하기.
     */
    @PostMapping("/draw/start")
    public ResponseEntity drawBlockAtStart(@RequestHeader("Session-Id") String sessionId,
                                           @RequestBody StartBlockDraw blockDraw) {

        playerService.drawBlockAtStart(sessionId, blockDraw.getWhiteBlockCount(), blockDraw.getBlackBlockCount());
        gameRoomService.endStartPhase(blockDraw.getGameRoomId(), sessionId);

        return ResponseEntity.status(HttpStatus.OK).build();

        // endStartPhase(startBlockDraw.getGameRoomId(), playerOrderNum);
    }

    @PostMapping("/draw")
    public ResponseEntity drawBlockAtDrawPhase(@RequestHeader("Session-Id") String sessionId,
                                               @RequestBody BlockDraw blockDraw) {

        playerService.drawBlockAtDrawPhase(sessionId, blockDraw.getBlockColor());
        gameRoomService.endDrawPhase(blockDraw.getGameRoomId(), sessionId);
        DrawBlockData drawBlockData = DrawBlockData.from(playerService.findByWebSocketSessionId(sessionId));

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseData.create(200, drawBlockData));

        // endDrawPhase(blockDraw.getGameRoomId(), playerOrderNum);
    }

    @PostMapping("/joker")
    public ResponseEntity updateJoker(@RequestHeader("Session-Id") String sessionId,
                                      @RequestBody JokerUpdate jokerUpdate) {

        playerService.updatePlayerJoker(sessionId, jokerUpdate.getIndex(), jokerUpdate.getBlockColor());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/guess")
    public ResponseEntity guessBlock(@RequestHeader("Session-Id") String sessionId,
                                     @RequestBody BlockGuess blockGuess) {

        playerService.guessBlock(sessionId, blockGuess.getTargetPlayerId(), blockGuess.getBlockIndex(), blockGuess.getBlockNum());
        gameRoomService.endGuessPhase(blockGuess.getGameRoomId(), sessionId);

        return ResponseEntity.status(HttpStatus.OK).build();

        // endGuessPhase(blockGuess.getGameRoomId(), playerOrderNum);
    }

    @PostMapping("/repeat")
    public ResponseEntity choiceRepeatGuess(@RequestHeader("Session-Id") String sessionId,
                                            @RequestBody GuessRepeat guessRepeat) {

        gameRoomService.endRepeatPhase(guessRepeat.getGameRoomId(), sessionId, guessRepeat.isRepeatGuess());
        return ResponseEntity.status(HttpStatus.OK).build();

        // endRepeatPhase(guessRepeat.getGameRoomId(), findPlayer.getOrderNumber(), guessRepeat.isRepeatGuess());
    }
}
