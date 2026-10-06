package com.example.service2.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Codes {
    SUCCESS("success"),
    FAILED("failed");

    @Getter(onMethod_ = @JsonValue)
    private final String value;
}
