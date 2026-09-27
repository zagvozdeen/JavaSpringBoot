package com.example.lab1.controller;

import com.example.lab1.exception.UnsupportedCodeException;
import com.example.lab1.exception.ValidationFailedException;
import com.example.lab1.model.Codes;
import com.example.lab1.model.ErrorCodes;
import com.example.lab1.model.ErrorMessages;
import com.example.lab1.model.Request;
import com.example.lab1.model.Response;
import com.example.lab1.service.ModifyResponseService;
import com.example.lab1.service.ValidationService;
import com.example.lab1.util.DateTimeUtil;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@Slf4j
@RestController
public class MyController {
    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;

    public MyController(ValidationService validationService,
                        @Qualifier("ModifySystemTimeResponseService") ModifyResponseService modifyResponseService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
    }

    @PostMapping("/feedback")
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request, BindingResult bindingResult) {
        log.info("Request received: {}", request);
        Response response = createResponse(request);
        try {
            validationService.validate(bindingResult);
            if ("123".equals(request.getUid())) {
                log.error("Unsupported uid: {}", request.getUid());
                throw new UnsupportedCodeException();
            }
            response = modifyResponseService.modify(response);
            log.info("Response sent: {}", response);
            return ResponseEntity.ok(response);
        } catch (ValidationFailedException exception) {
            return failure(response, HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION);
        } catch (UnsupportedCodeException exception) {
            return failure(response, HttpStatus.BAD_REQUEST, ErrorCodes.UNSUPPORTED_EXCEPTION, ErrorMessages.UNSUPPORTED);
        } catch (Exception exception) {
            log.error("Unexpected request processing error", exception);
            return failure(response, HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.UNKNOWN_EXCEPTION, ErrorMessages.UNKNOWN);
        }
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Response> invalidJson(HttpMessageNotReadableException exception) {
        log.error("Invalid JSON: {}", exception.getMostSpecificCause().getMessage());
        return failure(createResponse(null), HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION);
    }

    private Response createResponse(Request request) {
        Response response = new Response(
                request == null ? "" : request.getUid(),
                request == null ? "" : request.getOperationUid(),
                DateTimeUtil.getCustomFormat().format(new Date()),
                Codes.SUCCESS,
                ErrorCodes.EMPTY,
                ErrorMessages.EMPTY);
        log.info("Response created: {}", response);
        return response;
    }

    private ResponseEntity<Response> failure(Response response, HttpStatus status, ErrorCodes errorCode, ErrorMessages errorMessage) {
        response.setCode(Codes.FAILED);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        log.info("Response sent with HTTP {}: {}", status.value(), response);
        return ResponseEntity.status(status).body(response);
    }
}
