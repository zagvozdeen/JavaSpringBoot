package com.example.service2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Systems {
    ERP("Enterprise Resource Planning"),
    CRM("Customer Relationship Management"),
    WMS("Warehouse Management System"),
    @JsonProperty("Service 1")
    SERVICE_1("Service 1");

    private final String description;

    @Override
    public String toString() {
        return this == SERVICE_1 ? "Service 1" : name();
    }
}
