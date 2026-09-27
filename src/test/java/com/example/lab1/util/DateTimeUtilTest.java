package com.example.lab1.util;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class DateTimeUtilTest {
    @Test
    void usesUtcAndCreatesIndependentFormatters() {
        var first = DateTimeUtil.getCustomFormat();
        var second = DateTimeUtil.getCustomFormat();
        assertNotSame(first, second);
        first.setTimeZone(TimeZone.getTimeZone("Asia/Yekaterinburg"));
        assertEquals("1970-01-01T00:00:00.000Z", second.format(new Date(0)));
    }
}
