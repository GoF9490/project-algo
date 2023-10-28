package com.game.algo.global.dto;

import lombok.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ResponseData<T> {

    private Integer code;

    private T data;

    public static <T> ResponseData<T> create(int code, T data) {
        return new ResponseData<>(code, data);
    }
}
