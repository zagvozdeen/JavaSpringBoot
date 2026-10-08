package com.example.lab1.service;

import com.example.lab1.model.Positions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AnnualBonusServiceImplTest {
    @Test
    @DisplayName("Годовая премия: пример из методички")
    void calculatesTheExampleFromTheAssignment() {
        assertEquals(360493.8271604938, service(2026).calculate(Positions.HR, 100000, 2, 243), 0.000001);
    }

    @ParameterizedTest(name = "Год {0}: {1} дней")
    @CsvSource({"2026,365", "2024,366", "2000,366", "2100,365"})
    void usesTheCurrentYearIncludingLeapYearRules(int year, int days) {
        assertEquals(days * 120.0, service(year).calculate(Positions.HR, 100, 1, 1), 0.000001);
        assertEquals(120, service(year).calculate(Positions.HR, 100, 1, days), 0.000001);
        assertThrows(IllegalArgumentException.class,
                () -> service(year).calculate(Positions.HR, 100, 1, days + 1));
    }

    @ParameterizedTest
    @CsvSource({"TL,260", "PO,280", "TPM,300", "CTO,350"})
    void calculatesQuarterlyBonusForEveryManager(Positions position, double annual) {
        AnnualBonusService service = service(2026);
        assertEquals(annual, service.calculate(position, 100, 1, 365), 0.000001);
        assertEquals(annual / 4, service.calculateQuarterly(position, 100, 1, 365), 0.000001);
    }

    @ParameterizedTest
    @EnumSource(value = Positions.class, names = {"DEV", "HR"})
    @DisplayName("Квартальная премия запрещена для DEV и HR")
    void rejectsQuarterlyBonusForNonManagers(Positions position) {
        assertThrows(IllegalArgumentException.class,
                () -> service(2026).calculateQuarterly(position, 100, 1, 365));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.POSITIVE_INFINITY})
    void rejectsInvalidSalaryAndBonus(double value) {
        AnnualBonusService service = service(2026);
        assertThrows(IllegalArgumentException.class, () -> service.calculate(Positions.TL, value, 1, 243));
        assertThrows(IllegalArgumentException.class, () -> service.calculate(Positions.TL, 100, value, 243));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 366})
    void rejectsInvalidWorkDays(int days) {
        assertThrows(IllegalArgumentException.class,
                () -> service(2026).calculate(Positions.TL, 100, 1, days));
    }

    @Test
    void permitsZeroSalaryOrBonus() {
        assertEquals(0, service(2026).calculate(Positions.DEV, 0, 2, 243));
        assertEquals(0, service(2026).calculateQuarterly(Positions.TL, 100, 0, 243));
    }

    @Test
    void rejectsMissingPositionAndOverflow() {
        assertThrows(IllegalArgumentException.class, () -> service(2026).calculate(null, 100, 1, 243));
        assertThrows(IllegalArgumentException.class, () -> service(2026).calculateQuarterly(null, 100, 1, 243));
        assertThrows(IllegalArgumentException.class,
                () -> service(2026).calculate(Positions.CTO, Double.MAX_VALUE, 2, 1));
    }

    private AnnualBonusService service(int year) {
        return new AnnualBonusServiceImpl(Clock.fixed(Instant.parse(year + "-06-01T12:00:00Z"), ZoneOffset.UTC));
    }
}
