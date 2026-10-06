package com.example.lab1.service;

import com.example.lab1.model.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Service
public class Service2Client {
    private final RestTemplate restTemplate;
    private final String url;

    public Service2Client(RestTemplateBuilder builder, @Value("${service2.url}") String url) {
        this.restTemplate = builder.connectTimeout(Duration.ofSeconds(2))
                .readTimeout(Duration.ofSeconds(5)).build();
        this.url = url;
    }

    public void forward(Request request, long receivedAt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service1-Received-At", Long.toString(receivedAt));
        log.info("Request forwarded to service 2: {}", request);
        var response = restTemplate.postForEntity(url, new HttpEntity<>(request, headers), String.class);
        log.info("Service 2 response: HTTP {}, {}", response.getStatusCode().value(), response.getBody());
    }
}
