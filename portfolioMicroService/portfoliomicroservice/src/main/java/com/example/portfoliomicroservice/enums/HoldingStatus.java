package com.example.portfoliomicroservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum HoldingStatus {
    ACTIVE,
    MATURED,
    CLOSED;

    @JsonCreator
    public static HoldingStatus from(String value) {
        if (value == null) return null;
        return HoldingStatus.valueOf(value.trim().toUpperCase());
    }
}
