package com.example.service2.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.service2.model.Request;
import com.example.service2.model.Systems;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Service2ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final Logger logger = (Logger) LoggerFactory.getLogger(MyController.class);
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
    void acceptsModifiedRequestAndLogsTimeSinceService1ReceivedIt() throws Exception {
        long service1ReceivedAt = System.currentTimeMillis() - 100;
        long before = System.currentTimeMillis();

        mockMvc.perform(post("/feedback")
                        .header("X-Service1-Received-At", service1ReceivedAt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("lab4-1"))
                .andExpect(jsonPath("$.operationUid").value("operation-1"))
                .andExpect(jsonPath("$.code").value("success"));

        long after = System.currentTimeMillis();
        ILoggingEvent receipt = receiptLog();
        assertEquals("lab4-1", receipt.getArgumentArray()[0]);
        assertEquals(Systems.SERVICE_1, receipt.getArgumentArray()[1]);
        assertEquals("lab4.http", receipt.getArgumentArray()[2]);
        long elapsed = ((Number) receipt.getArgumentArray()[3]).longValue();
        assertTrue(elapsed >= before - service1ReceivedAt);
        assertTrue(elapsed <= after - service1ReceivedAt);
        assertTrue(receipt.getFormattedMessage().contains("systemName=Service 1"));
        assertTrue(logs.list.stream().anyMatch(event -> event.getFormattedMessage()
                .startsWith("Request received: Request(uid=lab4-1, operationUid=operation-1, systemName=Service 1")));
    }

    @Test
    void acceptsDirectRequestWithoutTimingHeader() throws Exception {
        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().put("systemName", "ERP").toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));

        assertTrue(receiptLog().getFormattedMessage().contains("systemName=ERP"));
        assertTrue(receiptLog().getFormattedMessage().endsWith("elapsedMs=null"));
        assertFalse(logs.list.stream().anyMatch(event -> event.getLevel() == Level.WARN));
    }

    @Test
    void ignoresMalformedTimingHeaderAndLogsWarning() throws Exception {
        mockMvc.perform(post("/feedback")
                        .header("X-Service1-Received-At", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().toString()))
                .andExpect(status().isOk());

        assertTrue(logs.list.stream().anyMatch(event -> event.getLevel() == Level.WARN
                && event.getFormattedMessage().equals("Invalid X-Service1-Received-At header: invalid")));
        assertTrue(receiptLog().getFormattedMessage().endsWith("elapsedMs=null"));
    }

    @Test
    void validatesModifiedRequestsWithTimingHeader() throws Exception {
        mockMvc.perform(post("/feedback")
                        .header("X-Service1-Received-At", System.currentTimeMillis())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().put("communicationId", 0).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("failed"))
                .andExpect(jsonPath("$.errorCode").value("ValidationException"));
    }

    @Test
    void preservesSystemWireNamesWhenSerializingRequests() throws Exception {
        for (String system : new String[]{"ERP", "CRM", "WMS", "Service 1"}) {
            Request request = objectMapper.treeToValue(validRequest().put("systemName", system), Request.class);
            assertEquals(system, objectMapper.readTree(objectMapper.writeValueAsString(request)).get("systemName").asText());
        }
    }

    private ILoggingEvent receiptLog() {
        return logs.list.stream()
                .filter(event -> event.getFormattedMessage().startsWith("Service 2 received:"))
                .findFirst().orElseThrow();
    }

    private ObjectNode validRequest() {
        return objectMapper.createObjectNode()
                .put("uid", "lab4-1")
                .put("operationUid", "operation-1")
                .put("systemName", "Service 1")
                .put("systemTime", "2026-10-06T10:00:00Z")
                .put("source", "lab4.http")
                .put("communicationId", 100);
    }
}
