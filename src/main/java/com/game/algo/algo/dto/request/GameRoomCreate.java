package com.game.algo.algo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomCreate {

    private Long playerId;

    @Size(min = 3, max = 15 ,message = "DisplayName의 길이는 3이상 15이하여야 합니다.")
    @Pattern(regexp = "^[ㄱ-ㅎ|ㅏ-ㅣ|가-핳|a-z|A-Z|0-9]+$", message = "한글, 숫자, 알파벳만 사용 가능합니다.")
    private String title;
}
