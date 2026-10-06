package com.example.lab1.service;

import com.example.lab1.model.Request;
import com.example.lab1.model.Systems;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Order(1)
public class ModifySystemNameRequestService implements ModifyRequestService {
    @Override
    public void modify(Request request) {
        Systems previous = request.getSystemName();
        request.setSystemName(Systems.SERVICE_1);
        log.info("Request systemName modified: {} -> {}", previous, request.getSystemName());
    }
}
