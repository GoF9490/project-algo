package com.game.algo.global.dto;

import com.game.algo.algo.exception.GameExceptionCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.Set;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private int code;
    private String message;

    public static ErrorResponse of(BindingResult bindingResult) {
        return new ErrorResponse(400, bindingResult.getFieldError().getDefaultMessage());
    }

    public static ErrorResponse of(Set<ConstraintViolation<?>> violations) {
        return new ErrorResponse(400, new ArrayList<>(violations).get(0).getMessage());
    }

    public static ErrorResponse of(GameExceptionCode exceptionCode) {
        return new ErrorResponse(exceptionCode.getStatus(), exceptionCode.getMessage());
    }

    public static ErrorResponse of(HttpStatus httpStatus) {
        return new ErrorResponse(httpStatus.value(), httpStatus.getReasonPhrase());
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message) {
        return new ErrorResponse(httpStatus.value(), message);
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse(400, message);
    }
}
