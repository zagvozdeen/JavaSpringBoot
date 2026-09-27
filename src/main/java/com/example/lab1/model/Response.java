package com.example.lab1.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Response {
    private final String uid;
    private final String operationUid;
    private final String systemTime;
    private final String code;
    private final String errorCode;
    private final String errorMessage;
}
