package com.example.lab1.service;

import com.example.lab1.exception.ValidationFailedException;
import com.example.lab1.model.Request;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidationServiceImpl implements ValidationService {
    private final Validator validator;

    @Override
    public void validate(Request request) {
        if (!validator.validate(request).isEmpty()) {
            throw new ValidationFailedException();
        }
    }
}
