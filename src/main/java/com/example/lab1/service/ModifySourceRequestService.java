package com.example.lab1.service;

import com.example.lab1.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(2)
public class ModifySourceRequestService implements ModifyRequestService {
    @Override
    public void modify(Request request) {
        String previous = request.getSource();
        request.setSource("service-1");
        log.info("Request source modified: {} -> {}", previous, request.getSource());
    }
}
