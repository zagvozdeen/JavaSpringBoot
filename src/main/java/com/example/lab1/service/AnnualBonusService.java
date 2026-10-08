package com.example.lab1.service;

import com.example.lab1.model.Positions;

public interface AnnualBonusService {
    double calculate(Positions position, double salary, double bonus, int workDays);

    double calculateQuarterly(Positions position, double salary, double bonus, int workDays);
}
