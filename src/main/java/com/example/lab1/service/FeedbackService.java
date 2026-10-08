package com.example.lab1.service;

import com.example.lab1.exception.UnsupportedCodeException;
import com.example.lab1.exception.ValidationFailedException;
import com.example.lab1.model.Codes;
import com.example.lab1.model.ErrorCodes;
import com.example.lab1.model.ErrorMessages;
import com.example.lab1.model.Request;
import com.example.lab1.model.Response;
import com.example.lab1.util.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FeedbackService {
    private final ValidationService validationService;
    private final ModifyResponseService modifyResponseService;
    private final List<ModifyRequestService> modifyRequestServices;
    private final Service2Client service2Client;
    private final AnnualBonusService annualBonusService;

    public FeedbackService(ValidationService validationService,
                        @Qualifier("ModifySystemTimeResponseService") ModifyResponseService modifyResponseService,
                        List<ModifyRequestService> modifyRequestServices, Service2Client service2Client,
                        AnnualBonusService annualBonusService) {
        this.validationService = validationService;
        this.modifyResponseService = modifyResponseService;
        this.modifyRequestServices = modifyRequestServices;
        this.service2Client = service2Client;
        this.annualBonusService = annualBonusService;
    }

    public ResponseEntity<Response> feedback(Request request, BindingResult bindingResult) {
        long receivedAt = System.currentTimeMillis();
        log.info("Request received: {}", request);
        Response response = createResponse(request);
        try {
            validationService.validate(bindingResult);
            if ("123".equals(request.getUid())) {
                log.error("Unsupported uid: {}", request.getUid());
                throw new UnsupportedCodeException();
            }
            enrichBonus(request, response);
            modifyRequestServices.forEach(service -> service.modify(request));
            service2Client.forward(request, receivedAt);
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

    public ResponseEntity<Response> invalidJson(HttpMessageNotReadableException exception) {
        log.error("Invalid JSON: {}", exception.getMostSpecificCause().getMessage());
        return failure(createResponse(null), HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_EXCEPTION, ErrorMessages.VALIDATION);
    }

    private void enrichBonus(Request request, Response response) {
        if (request.getPosition() == null && request.getSalary() == null
                && request.getBonus() == null && request.getWorkDays() == null) {
            return;
        }
        try {
            if (request.getPosition() == null || request.getSalary() == null
                    || request.getBonus() == null || request.getWorkDays() == null) {
                throw new IllegalArgumentException("position, salary, bonus and workDays are required together");
            }
            response.setAnnualBonus(annualBonusService.calculate(request.getPosition(), request.getSalary(),
                    request.getBonus(), request.getWorkDays()));
            if (request.getPosition().isManager()) {
                response.setQuarterlyBonus(annualBonusService.calculateQuarterly(request.getPosition(),
                        request.getSalary(), request.getBonus(), request.getWorkDays()));
            }
            log.info("Bonus calculated: uid={}, annualBonus={}, quarterlyBonus={}",
                    request.getUid(), response.getAnnualBonus(), response.getQuarterlyBonus());
        } catch (IllegalArgumentException exception) {
            log.error("Validation error: bonus calculation: {}", exception.getMessage());
            throw new ValidationFailedException();
        }
    }

    private Response createResponse(Request request) {
        Response response = new Response(
                request == null ? "" : request.getUid(),
                request == null ? "" : request.getOperationUid(),
                DateTimeUtil.getCustomFormat().format(new Date()),
                Codes.SUCCESS,
                ErrorCodes.EMPTY,
                ErrorMessages.EMPTY, null, null);
        log.info("Response created: {}", response);
        return response;
    }

    private ResponseEntity<Response> failure(Response response, HttpStatus status, ErrorCodes errorCode, ErrorMessages errorMessage) {
        response.setAnnualBonus(null);
        response.setQuarterlyBonus(null);
        response.setCode(Codes.FAILED);
        response.setErrorCode(errorCode);
        response.setErrorMessage(errorMessage);
        log.info("Response sent with HTTP {}: {}", status.value(), response);
        return ResponseEntity.status(status).body(response);
    }
}
