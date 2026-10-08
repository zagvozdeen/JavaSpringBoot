package com.example.lab1.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Positions {
    DEV(2.2, false),
    HR(1.2, false),
    TL(2.6, true),
    PO(2.8, true),
    TPM(3.0, true),
    CTO(3.5, true);

    private final double positionCoefficient;
    private final boolean isManager;
}
