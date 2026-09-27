package com.example.lab1.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorCodes {
    EMPTY(""),
    VALIDATION_EXCEPTION("ValidationException"),
    UNSUPPORTED_EXCEPTION("UnsupportedCodeException"),
    UNKNOWN_EXCEPTION("UnknownException");

    @Getter(onMethod_ = @JsonValue)
    private final String value;
}
