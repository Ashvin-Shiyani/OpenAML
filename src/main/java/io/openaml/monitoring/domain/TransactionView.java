package io.openaml.monitoring.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionView(
        long id,
        String externalId,
        String sender,
        String recipient,
        BigDecimal amount,
        String currency,
        Instant eventTime) {
}