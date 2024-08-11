package com.quotes.premium.operation;

import java.util.HashMap;
import java.util.Map;

public class OperationRegistry {
    private static final Map<String, Operation> operations = new HashMap<>();

    static {
        OperationRegistry.operations.put("round", new RoundingOperation());
        OperationRegistry.operations.put("multiplicative", new AdditionOperation());
        // Add more operations as needed
    }

    public static Operation getOperation(final String name) {
        return OperationRegistry.operations.get(name);
    }
}

