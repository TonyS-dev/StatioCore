package org.codeup.statiocore.util;

import java.util.Optional;
import java.util.UUID;

public final class Trace {
    private Trace() {}
    public static String currentId() {
        return Optional.ofNullable(org.slf4j.MDC.get("traceId"))
                .orElse(UUID.randomUUID().toString());
    }
}