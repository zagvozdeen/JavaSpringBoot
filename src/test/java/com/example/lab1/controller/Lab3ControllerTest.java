package com.example.lab1.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.example.lab1.service.ModifyOperationUidResponseService;
import com.example.lab1.service.Service2Client;
import com.example.lab1.service.FeedbackService;
import com.example.lab1.service.AnnualBonusServiceImpl;
import java.time.Clock;
import com.example.lab1.service.ValidationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Lab3ControllerTest {
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

    @ParameterizedTest
    @ValueSource(strings = {"ERP", "CRM", "WMS"})
    void acceptsSystemsAndLogsTheFullSuccessfulFlow(String system) throws Exception {
        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().put("systemName", system).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationUid").value("operation-1"))
                .andExpect(jsonPath("$.code").value("success"));

        assertTrue(hasLog(Level.INFO, "Request received:"));
        assertTrue(hasLog(Level.INFO, "systemName=" + system));
        assertTrue(hasLog(Level.INFO, "Response created:"));
        assertTrue(hasLog(Level.INFO, "Request validation passed"));
        assertTrue(hasLog(Level.INFO, "Response systemTime modified:"));
        assertTrue(hasLog(Level.INFO, "Response sent:"));
        assertFalse(hasLog(Level.INFO, "Response operationUid modified:"));
        assertEquals(1, logs.list.stream()
                .filter(event -> event.getFormattedMessage().startsWith("Response systemTime modified:"))
                .count());
    }

    @Test
    void allowsNullOptionalSystem() throws Exception {
        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().putNull("systemName").toString()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"OTHER", "Enterprise Resource Planning", "erp"})
    void rejectsUnknownSystemsAndLogsTheParsingError(String system) throws Exception {
        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().put("systemName", system).toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ValidationException"))
                .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"));

        assertTrue(hasLog(Level.ERROR, "Invalid JSON:"));
        assertTrue(hasLog(Level.ERROR, system));
        assertFalse(hasLog(Level.INFO, "Response systemTime modified:"));
    }

    @Test
    void logsEveryInvalidFieldAtErrorLevel() throws Exception {
        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()
                                .put("uid", "")
                                .put("operationUid", "x".repeat(33))
                                .put("systemTime", " ")
                                .put("communicationId", 0)
                                .toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ValidationException"));

        for (String field : new String[]{"uid", "operationUid", "systemTime", "communicationId"}) {
            assertTrue(logs.list.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                    && event.getFormattedMessage().startsWith("Validation error: " + field + ": ")
                    && event.getFormattedMessage().length() > ("Validation error: " + field + ": ").length()));
        }
        assertTrue(hasLog(Level.INFO, "Response sent with HTTP 400:"));
        assertFalse(hasLog(Level.INFO, "Response systemTime modified:"));
    }

    @Test
    void logsUnsupportedUidBeforeReturningItsError() throws Exception {
        mockMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().put("uid", "123").toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("UnsupportedCodeException"));

        assertTrue(hasLog(Level.ERROR, "Unsupported uid: 123"));
        assertFalse(hasLog(Level.INFO, "Response systemTime modified:"));
    }

    @Test
    void operationUidServiceCanReplaceTheSelectedTimeService() throws Exception {
        MockMvc alternate = MockMvcBuilders.standaloneSetup(new MyController(new FeedbackService(
                new ValidationServiceImpl(), new ModifyOperationUidResponseService(), List.of(), service2Client, new AnnualBonusServiceImpl(Clock.systemUTC())))).build();

        String body = alternate.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("1"))
                .andExpect(jsonPath("$.code").value("success"))
                .andReturn().getResponse().getContentAsString();

        String operationUid = objectMapper.readTree(body).get("operationUid").asText();
        assertDoesNotThrow(() -> UUID.fromString(operationUid));
        assertTrue(hasLog(Level.INFO, "Response operationUid modified: operation-1 -> " + operationUid));
        assertFalse(hasLog(Level.INFO, "Response systemTime modified:"));
    }

    @Test
    void logsUnexpectedFailuresAtErrorLevel() throws Exception {
        MockMvc failing = MockMvcBuilders.standaloneSetup(new MyController(new FeedbackService(
                new ValidationServiceImpl(), response -> {
                    throw new IllegalStateException("Service unavailable");
                }, List.of(), service2Client, new AnnualBonusServiceImpl(Clock.systemUTC())))).build();

        failing.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("UnknownException"));

        assertTrue(logs.list.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                && event.getFormattedMessage().equals("Unexpected request processing error")
                && event.getThrowableProxy() != null
                && event.getThrowableProxy().getMessage().equals("Service unavailable")));
    }

    private boolean hasLog(Level level, String message) {
        return logs.list.stream().anyMatch(event -> event.getLevel() == level
                && event.getFormattedMessage().contains(message));
    }

    private ObjectNode validRequest() {
        return objectMapper.createObjectNode()
                .put("uid", "1")
                .put("operationUid", "operation-1")
                .put("systemTime", "2026-09-27T10:00:00Z")
                .put("communicationId", 100);
    }
}
