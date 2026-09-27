package com.example.lab1.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
    private String uid;
    private String operationUid;
    private String systemTime;
    private Codes code;
    private ErrorCodes errorCode;
    private ErrorMessages errorMessage;
}
