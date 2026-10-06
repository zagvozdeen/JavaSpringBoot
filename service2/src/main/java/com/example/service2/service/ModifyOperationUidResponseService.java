package com.example.service2.service;

import com.example.service2.model.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Qualifier("ModifyOperationUidResponseService")
public class ModifyOperationUidResponseService implements ModifyResponseService {
    @Override
    public Response modify(Response response) {
        String previous = response.getOperationUid();
        response.setOperationUid(UUID.randomUUID().toString());
        log.info("Response operationUid modified: {} -> {}", previous, response.getOperationUid());
        return response;
    }
}
