package com.example.service2.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ErrorMessages {
    EMPTY(""),
    VALIDATION("Ошибка валидации"),
    UNSUPPORTED("Не поддерживаемая ошибка"),
    UNKNOWN("Произошла непредвиденная ошибка");

    @Getter(onMethod_ = @JsonValue)
    private final String value;
}
