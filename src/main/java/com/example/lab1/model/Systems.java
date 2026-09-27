package com.example.lab1.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Systems {
    ERP("Enterprise Resource Planning"),
    CRM("Customer Relationship Management"),
    WMS("Warehouse Management System");

    private final String description;
}
