package org.codeup.statiocore.util;

import org.slf4j.MDC;

import java.util.Optional;
import java.util.UUID;

public final class Trace {
    private Trace() {}
    public static String currentId() {
        return Optional.ofNullable(MDC.get("traceId"))
                .orElse(UUID.randomUUID().toString());
    }
}