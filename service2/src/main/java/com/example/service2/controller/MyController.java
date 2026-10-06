package com.example.service2.controller;

import com.example.service2.exception.UnsupportedCodeException;
import com.example.service2.exception.ValidationFailedException;
import com.example.service2.model.Codes;
import com.example.service2.model.ErrorCodes;
import com.example.service2.model.ErrorMessages;
import com.example.service2.model.Request;
import com.example.service2.model.Response;
import com.example.service2.service.ModifyResponseService;
import com.example.service2.service.ValidationService;
import com.example.service2.util.DateTimeUtil;
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
import org.springframework.web.bind.annotation.RequestHeader;
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
    public ResponseEntity<Response> feedback(@Valid @RequestBody Request request, BindingResult bindingResult,
                                            @RequestHeader(value = "X-Service1-Received-At", required = false) String service1ReceivedAt) {
        long receivedAt = System.currentTimeMillis();
        log.info("Request received: {}", request);
        logReceipt(request, receivedAt, service1ReceivedAt);
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

    private void logReceipt(Request request, long receivedAt, String service1ReceivedAt) {
        Long elapsedMillis = null;
        if (service1ReceivedAt != null) {
            try {
                elapsedMillis = receivedAt - Long.parseLong(service1ReceivedAt);
            } catch (NumberFormatException exception) {
                log.warn("Invalid X-Service1-Received-At header: {}", service1ReceivedAt);
            }
        }
        log.info("Service 2 received: uid={}, systemName={}, source={}, elapsedMs={}",
                request.getUid(), request.getSystemName(), request.getSource(), elapsedMillis);
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
