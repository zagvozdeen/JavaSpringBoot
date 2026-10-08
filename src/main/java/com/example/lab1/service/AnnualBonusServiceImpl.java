package com.example.lab1.service;

import com.example.lab1.model.Positions;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class AnnualBonusServiceImpl implements AnnualBonusService {
    private final Clock clock;

    @Override
    public double calculate(Positions position, double salary, double bonus, int workDays) {
        int daysInYear = Year.now(clock).length();
        if (position == null || !Double.isFinite(salary) || salary < 0
                || !Double.isFinite(bonus) || bonus < 0 || workDays < 1 || workDays > daysInYear) {
            throw new IllegalArgumentException("Invalid bonus calculation parameters");
        }
        double result = salary * bonus * daysInYear * position.getPositionCoefficient() / workDays;
        if (!Double.isFinite(result)) {
            throw new IllegalArgumentException("Bonus exceeds the supported range");
        }
        return result;
    }

    @Override
    public double calculateQuarterly(Positions position, double salary, double bonus, int workDays) {
        if (position == null || !position.isManager()) {
            throw new IllegalArgumentException("Quarterly bonus is available only to managers");
        }
        return calculate(position, salary, bonus, workDays) / 4;
    }
}
