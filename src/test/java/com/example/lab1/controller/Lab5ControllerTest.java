package com.example.lab1.controller;

import com.example.lab1.model.Request;
import com.example.lab1.service.Service2Client;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.ResourceAccessException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class Lab5ControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @MockitoBean
    private Service2Client service2Client;
    @MockitoBean
    private Clock clock;

    @BeforeEach
    void fixCurrentYear() {
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Yekaterinburg"));
        when(clock.instant()).thenReturn(Instant.parse("2026-06-01T12:00:00Z"));
    }

    @Test
    void returnsBothBonusesForManagerAndForwardsTheRequest() throws Exception {
        send(bonusRequest()).andExpect(status().isOk())
                .andExpect(jsonPath("$.annualBonus").value(520000.0))
                .andExpect(jsonPath("$.quarterlyBonus").value(130000.0))
                .andExpect(jsonPath("$.uid").value("lab5-1"));
        ArgumentCaptor<Request> request = ArgumentCaptor.forClass(Request.class);
        verify(service2Client).forward(request.capture(), anyLong());
        assertEquals(100000.0, request.getValue().getSalary());
        assertEquals("service-1", request.getValue().getSource());
    }

    @Test
    void usesLeapYearInHttpResponse() throws Exception {
        when(clock.instant()).thenReturn(Instant.parse("2024-06-01T12:00:00Z"));
        send(bonusRequest().put("workDays", 366)).andExpect(status().isOk())
                .andExpect(jsonPath("$.annualBonus").value(520000.0))
                .andExpect(jsonPath("$.quarterlyBonus").value(130000.0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"DEV", "HR"})
    void returnsOnlyAnnualBonusForNonManagers(String position) throws Exception {
        send(bonusRequest().put("position", position)).andExpect(status().isOk())
                .andExpect(jsonPath("$.annualBonus").value(position.equals("DEV") ? 440000.0 : 240000.0))
                .andExpect(jsonPath("$.quarterlyBonus").value(nullValue()));
    }

    @Test
    void keepsRequestsWithoutBonusFieldsWorking() throws Exception {
        send(baseRequest()).andExpect(status().isOk())
                .andExpect(jsonPath("$.annualBonus").value(nullValue()))
                .andExpect(jsonPath("$.quarterlyBonus").value(nullValue()));
        verify(service2Client).forward(any(), anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {"position", "salary", "bonus", "workDays"})
    void rejectsIncompleteBonusParameters(String field) throws Exception {
        ObjectNode request = bonusRequest();
        request.remove(field);
        expectValidationFailure(request);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 366})
    void rejectsWorkDaysOutsideCurrentYear(int workDays) throws Exception {
        expectValidationFailure(bonusRequest().put("workDays", workDays));
    }

    @Test
    void rejectsNegativeAmountsAndUnknownPosition() throws Exception {
        expectValidationFailure(bonusRequest().put("salary", -1));
        expectValidationFailure(bonusRequest().put("bonus", -1));
        expectValidationFailure(bonusRequest().put("position", "UNKNOWN"));
    }

    @Test
    void doesNotReturnCalculatedBonusesWhenForwardingFails() throws Exception {
        doThrow(new ResourceAccessException("Connection refused")).when(service2Client).forward(any(), anyLong());
        send(bonusRequest()).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("UnknownException"))
                .andExpect(jsonPath("$.annualBonus").value(nullValue()))
                .andExpect(jsonPath("$.quarterlyBonus").value(nullValue()));
    }

    private void expectValidationFailure(ObjectNode request) throws Exception {
        send(request).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ValidationException"))
                .andExpect(jsonPath("$.annualBonus").value(nullValue()));
        verifyNoInteractions(service2Client);
    }

    private ResultActions send(ObjectNode request) throws Exception {
        return mockMvc.perform(post("/feedback").contentType(MediaType.APPLICATION_JSON).content(request.toString()));
    }

    private ObjectNode bonusRequest() {
        return baseRequest().put("position", "TL").put("salary", 100000).put("bonus", 2).put("workDays", 365);
    }

    private ObjectNode baseRequest() {
        return mapper.createObjectNode().put("uid", "lab5-1").put("operationUid", "operation-5")
                .put("systemName", "ERP").put("systemTime", "2026-10-08T10:00:00.000Z").put("communicationId", 100);
    }
}
