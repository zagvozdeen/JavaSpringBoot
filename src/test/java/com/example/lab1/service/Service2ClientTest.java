package com.example.lab1.service;

import com.example.lab1.model.Request;
import com.example.lab1.model.Systems;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class Service2ClientTest {
    private Service2Client client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
        client = new Service2Client(new RestTemplateBuilder(customizer), "http://localhost:8084/feedback");
        server = customizer.getServer();
    }

    @Test
    void sendsTheModifiedJsonAndReceiptHeaderExactlyOnce() {
        server.expect(once(), requestTo("http://localhost:8084/feedback"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header("X-Service1-Received-At", "1791280800000"))
                .andExpect(content().json("""
                        {"uid":"1","operationUid":"operation-1","systemName":"Service 1",
                         "systemTime":"2026-10-06T10:00:00.000Z","source":"service-1",
                         "communicationId":100,"templateId":8,"productCode":1500,"smsCode":10}
                        """))
                .andRespond(withSuccess("{\"code\":\"success\"}", MediaType.APPLICATION_JSON));

        client.forward(request(), 1791280800000L);

        server.verify();
    }

    @Test
    void propagatesTransportErrorsWithoutRetrying() {
        server.expect(once(), requestTo("http://localhost:8084/feedback"))
                .andRespond(withException(new IOException("Connection refused")));

        assertThrows(ResourceAccessException.class, () -> client.forward(request(), 1791280800000L));

        server.verify();
    }

    private Request request() {
        Request request = new Request();
        request.setUid("1");
        request.setOperationUid("operation-1");
        request.setSystemName(Systems.SERVICE_1);
        request.setSystemTime("2026-10-06T10:00:00.000Z");
        request.setSource("service-1");
        request.setCommunicationId(100);
        request.setTemplateId(8);
        request.setProductCode(1500);
        request.setSmsCode(10);
        return request;
    }
}
