package com.example.bankmicroservice.utils;

public final class MaskingUtils {

    private MaskingUtils() {
    }

    public static String mask(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int visible = Math.min(4, value.length());
        return "*".repeat(value.length() - visible) + value.substring(value.length() - visible);
    }
}
