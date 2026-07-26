package com.example.bankmicroservice.exceptions;

import java.util.Arrays;

public class InvalidEnumValueException extends RuntimeException {

    public InvalidEnumValueException(String fieldName, String value, Class<? extends Enum<?>> enumType) {
        super("Invalid " + fieldName + " '" + value + "'. Allowed values: " +
                Arrays.toString(enumType.getEnumConstants()));
    }
}
