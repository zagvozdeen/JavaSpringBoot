package com.example.lab1.service;

import com.example.lab1.model.Codes;
import com.example.lab1.model.ErrorCodes;
import com.example.lab1.model.ErrorMessages;
import com.example.lab1.model.Response;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModifyResponseServiceTest {
    @Test
    void updatesOnlySystemTimeWithCurrentUtcTimestamp() {
        Response response = response();
        Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        assertSame(response, new ModifySystemTimeResponseService().modify(response));

        Instant timestamp = Instant.parse(response.getSystemTime());
        assertFalse(timestamp.isBefore(before));
        assertFalse(timestamp.isAfter(Instant.now()));
        assertTrue(response.getSystemTime().matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}Z"));
        assertEquals("operation-1", response.getOperationUid());
        assertUnchangedOtherFields(response);
    }

    @Test
    void updatesOnlyOperationUidWithANewRandomUuid() {
        Response response = response();
        ModifyResponseService service = new ModifyOperationUidResponseService();

        assertSame(response, service.modify(response));
        UUID first = UUID.fromString(response.getOperationUid());
        assertEquals(4, first.version());
        service.modify(response);
        assertNotEquals(first, UUID.fromString(response.getOperationUid()));
        assertEquals("2000-01-01T00:00:00.000Z", response.getSystemTime());
        assertUnchangedOtherFields(response);
    }

    private Response response() {
        return new Response("1", "operation-1", "2000-01-01T00:00:00.000Z",
                Codes.SUCCESS, ErrorCodes.EMPTY, ErrorMessages.EMPTY);
    }

    private void assertUnchangedOtherFields(Response response) {
        assertEquals("1", response.getUid());
        assertEquals(Codes.SUCCESS, response.getCode());
        assertEquals(ErrorCodes.EMPTY, response.getErrorCode());
        assertEquals(ErrorMessages.EMPTY, response.getErrorMessage());
    }
}
