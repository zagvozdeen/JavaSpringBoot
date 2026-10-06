package com.example.lab1.controller;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.lab1.model.Request;
import com.example.lab1.model.Systems;
import com.example.lab1.service.Service2Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Lab4ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Service2Client service2Client;

    private final Logger logger = (Logger) LoggerFactory.getLogger("com.example.lab1");
    private ListAppender<ILoggingEvent> logs;

    @BeforeEach
    void captureLogs() {
        logs = new ListAppender<>();
        logs.start();
        logger.addAppender(logs);
    }

    @AfterEach
    void releaseLogs() {
        logger.detachAppender(logs);
        logs.stop();
    }

    @Test
    void modifiesBothFieldsInOrderAndForwardsOnceWithReceiptTime() throws Exception {
        ObjectNode original = validRequest();
        long before = System.currentTimeMillis();

        mockMvc.perform(post("/feedback").contentType(MediaType.APPLICATION_JSON).content(original.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("1"))
                .andExpect(jsonPath("$.operationUid").value("operation-1"))
                .andExpect(jsonPath("$.code").value("success"));

        ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);
        ArgumentCaptor<Long> receivedAt = ArgumentCaptor.forClass(Long.class);
        verify(service2Client).forward(request.capture(), receivedAt.capture());
        verifyNoMoreInteractions(service2Client);
        assertEquals(original.put("systemName", "Service 1").put("source", "service-1"),
                objectMapper.valueToTree(request.getValue()));
        assertTrue(receivedAt.getValue() >= before);
        assertTrue(receivedAt.getValue() <= System.currentTimeMillis());
        assertEquals(List.of("Request systemName modified: ERP -> SERVICE_1",
                        "Request source modified: mobile -> service-1"),
                logs.list.stream().map(ILoggingEvent::getFormattedMessage)
                        .filter(message -> message.startsWith("Request systemName modified:")
                                || message.startsWith("Request source modified:"))
                        .toList());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "123"})
    void doesNotForwardRejectedRequests(String uid) throws Exception {
        mockMvc.perform(post("/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().put("uid", uid).toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service2Client);
    }

    @Test
    void doesNotForwardAnUnknownSystem() throws Exception {
        mockMvc.perform(post("/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().put("systemName", "UNKNOWN").toString()))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service2Client);
    }

    @Test
    void returnsTheExistingErrorWhenService2IsUnavailable() throws Exception {
        doThrow(new ResourceAccessException("Connection refused"))
                .when(service2Client).forward(any(), anyLong());

        mockMvc.perform(post("/feedback").contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.uid").value("1"))
                .andExpect(jsonPath("$.operationUid").value("operation-1"))
                .andExpect(jsonPath("$.code").value("failed"))
                .andExpect(jsonPath("$.errorCode").value("UnknownException"))
                .andExpect(jsonPath("$.errorMessage").value("Произошла непредвиденная ошибка"));

        verify(service2Client).forward(any(), anyLong());
        verifyNoMoreInteractions(service2Client);
    }

    @Test
    void preservesTheSystemsJsonContract() throws Exception {
        for (Systems system : Systems.values()) {
            String json = system == Systems.SERVICE_1 ? "\"Service 1\"" : "\"" + system.name() + "\"";
            assertEquals(json, objectMapper.writeValueAsString(system));
            assertEquals(system, objectMapper.readValue(json, Systems.class));
        }
    }

    private ObjectNode validRequest() {
        return objectMapper.createObjectNode()
                .put("uid", "1")
                .put("operationUid", "operation-1")
                .put("systemName", "ERP")
                .put("systemTime", "2026-10-06T10:00:00.000Z")
                .put("source", "mobile")
                .put("communicationId", 100)
                .put("templateId", 8)
                .put("productCode", 1500)
                .put("smsCode", 10);
    }
}
