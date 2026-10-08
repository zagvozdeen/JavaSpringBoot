package com.example.lab1.controller;

import com.example.lab1.service.ModifySystemTimeResponseService;
import com.example.lab1.service.Service2Client;
import com.example.lab1.service.FeedbackService;
import com.example.lab1.service.AnnualBonusServiceImpl;
import java.time.Clock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private Service2Client service2Client;

    @Test
    void returnsSuccessWithCopiedIdentifiersAndCurrentUtcTime() throws Exception {
        Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String body = send(validRequest()
                .put("systemName", "ERP")
                .put("source", "mobile")
                .put("templateId", 8)
                .put("productCode", 1500)
                .put("smsCode", 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uid").value("1"))
                .andExpect(jsonPath("$.operationUid").value("operation-1"))
                .andExpect(jsonPath("$.code").value("success"))
                .andExpect(jsonPath("$.errorCode").value(""))
                .andExpect(jsonPath("$.errorMessage").value(""))
                .andReturn().getResponse().getContentAsString();

        String systemTime = objectMapper.readTree(body).get("systemTime").asText();
        Instant timestamp = Instant.parse(systemTime);
        assertTrue(systemTime.endsWith("Z"));
        assertFalse(timestamp.isBefore(before));
        assertFalse(timestamp.isAfter(Instant.now()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 100000})
    void acceptsBoundaryValuesAndMissingOptionalFields(int communicationId) throws Exception {
        send(validRequest()
                .put("uid", "u".repeat(32))
                .put("operationUid", "o".repeat(32))
                .put("communicationId", communicationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("success"));
    }

    @ParameterizedTest
    @CsvSource({
            "uid,missing", "uid,null", "uid,blank",
            "operationUid,missing", "operationUid,null", "operationUid,blank",
            "systemTime,missing", "systemTime,null", "systemTime,blank"
    })
    void rejectsMissingNullAndBlankRequiredStrings(String field, String value) throws Exception {
        ObjectNode request = validRequest();
        switch (value) {
            case "missing" -> request.remove(field);
            case "null" -> request.putNull(field);
            default -> request.put(field, "   ");
        }
        expectValidationFailure(send(request));
    }

    @ParameterizedTest
    @ValueSource(strings = {"uid", "operationUid"})
    void rejectsIdentifiersLongerThan32Characters(String field) throws Exception {
        expectValidationFailure(send(validRequest().put(field, "x".repeat(33))));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 100001})
    void rejectsCommunicationIdsOutsideTheRange(int communicationId) throws Exception {
        expectValidationFailure(send(validRequest().put("communicationId", communicationId)));
    }

    @Test
    void rejectsMissingCommunicationId() throws Exception {
        ObjectNode request = validRequest();
        request.remove("communicationId");
        expectValidationFailure(send(request));
    }

    @Test
    void rejectsSeveralInvalidFieldsTogether() throws Exception {
        expectValidationFailure(send(validRequest()
                .put("uid", "")
                .put("operationUid", "x".repeat(33))
                .put("systemTime", " ")
                .put("communicationId", 0)));
    }

    @Test
    void rejectsUnsupportedUidWithItsOwnErrorCode() throws Exception {
        send(validRequest().put("uid", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.uid").value("123"))
                .andExpect(jsonPath("$.operationUid").value("operation-1"))
                .andExpect(jsonPath("$.code").value("failed"))
                .andExpect(jsonPath("$.errorCode").value("UnsupportedCodeException"))
                .andExpect(jsonPath("$.errorMessage").value("Не поддерживаемая ошибка"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{", "{\"communicationId\":\"invalid\"}", "null"})
    void returnsStructuredValidationErrorForUnreadableJson(String json) throws Exception {
        expectValidationFailure(mockMvc.perform(post("/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)));
    }

    @Test
    void returnsServerErrorWithoutLeakingUnexpectedExceptionDetails() throws Exception {
        MockMvc failingServiceMvc = MockMvcBuilders.standaloneSetup(new MyController(new FeedbackService(request -> {
            throw new IllegalStateException("Internal details");
        }, new ModifySystemTimeResponseService(), List.of(), service2Client, new AnnualBonusServiceImpl(Clock.systemUTC())))).build();

        failingServiceMvc.perform(post("/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest().toString()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("failed"))
                .andExpect(jsonPath("$.errorCode").value("UnknownException"))
                .andExpect(jsonPath("$.errorMessage").value("Произошла непредвиденная ошибка"));
    }

    private ObjectNode validRequest() {
        return objectMapper.createObjectNode()
                .put("uid", "1")
                .put("operationUid", "operation-1")
                .put("systemTime", "2026-09-27T10:00:00Z")
                .put("communicationId", 100);
    }

    private ResultActions send(ObjectNode request) throws Exception {
        return mockMvc.perform(post("/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request.toString()));
    }

    private void expectValidationFailure(ResultActions result) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("failed"))
                .andExpect(jsonPath("$.errorCode").value("ValidationException"))
                .andExpect(jsonPath("$.errorMessage").value("Ошибка валидации"));
    }
}
