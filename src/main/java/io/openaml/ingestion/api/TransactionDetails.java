package io.openaml.ingestion.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TransactionDetails(
        long transactionId,
        String eventId,
        String senderAccount,
        String recipientAccount,
        BigDecimal amount,
        String currency,
        Instant eventTime,
        Instant receivedAt,
        String channel,
        Map<String, Object> metadata,
        boolean late,
        String ingestionStatus,
        String evaluationStatus,
        int evaluationAttempts,
        Instant evaluatedAt,
        List<Long> alertIds) {
}