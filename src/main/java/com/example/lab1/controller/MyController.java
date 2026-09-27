package com.example.lab1.controller;

import com.example.lab1.exception.UnsupportedCodeException;
import com.example.lab1.exception.ValidationFailedException;
import com.example.lab1.model.Request;
import com.example.lab1.model.Response;
import com.example.lab1.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class MyController {
    private final ValidationService validationService;

    @PostMapping("/feedback")
    public ResponseEntity<Response> feedback(@RequestBody Request request) {
        try {
            validationService.validate(request);
            if ("123".equals(request.getUid())) {
                throw new UnsupportedCodeException();
            }
            return response(request, HttpStatus.OK, "", "");
        } catch (ValidationFailedException exception) {
            return response(request, HttpStatus.BAD_REQUEST, "ValidationException", "Ошибка валидации");
        } catch (UnsupportedCodeException exception) {
            return response(request, HttpStatus.BAD_REQUEST, "UnsupportedCodeException", "Не поддерживаемая ошибка");
        } catch (Exception exception) {
            return response(request, HttpStatus.INTERNAL_SERVER_ERROR, "UnknownException", "Произошла непредвиденная ошибка");
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> invalidJson() {
        return response(null, HttpStatus.BAD_REQUEST, "ValidationException", "Ошибка валидации");
    }

    private ResponseEntity<Response> response(Request request, HttpStatus status, String errorCode, String errorMessage) {
        return ResponseEntity.status(status).body(new Response(
                request == null ? "" : request.getUid(),
                request == null ? "" : request.getOperationUid(),
                Instant.now().toString(),
                status == HttpStatus.OK ? "success" : "failed",
                errorCode,
                errorMessage));
    }
}
