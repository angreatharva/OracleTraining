package com.example.portfoliomicroservice.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED;

    @JsonCreator
    public static AccountStatus from(String value) {
        if (value == null) return null;
        return AccountStatus.valueOf(value.trim().toUpperCase());
    }
}
