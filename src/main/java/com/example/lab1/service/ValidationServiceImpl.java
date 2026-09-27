package com.example.lab1.service;

import com.example.lab1.exception.ValidationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

@Slf4j
@Service
public class ValidationServiceImpl implements ValidationService {
    @Override
    public void validate(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    log.error("Validation error: {}: {}", error.getField(), error.getDefaultMessage()));
            throw new ValidationFailedException();
        }
        log.info("Request validation passed");
    }
}
