package com.example.lab1.service;

import org.springframework.validation.BindingResult;

public interface ValidationService {
    void validate(BindingResult bindingResult);
}
