package com.example.bankmicroservice.utils;

import com.example.bankmicroservice.exceptions.InvalidEnumValueException;

import java.util.Locale;

public final class EnumParser {

    private EnumParser() {
    }

    public static <E extends Enum<E>> E parse(String value, Class<E> type, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new InvalidEnumValueException(fieldName, value, type);
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidEnumValueException(fieldName, value, type);
        }
    }
}
