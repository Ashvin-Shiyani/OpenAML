package io.openaml.ingestion.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TransactionRequest(
        @NotBlank
        @Size(max = 128)
        @Pattern(regexp = "[A-Za-z0-9._:\\-]+",
                message = "may only contain letters, digits, '.', '_', ':' and '-'")
        String eventId,

        @NotBlank
        @Size(max = 128)
        String senderAccount,

        @NotBlank
        @Size(max = 128)
        String recipientAccount,

        @NotNull
        @DecimalMin(value = "0.0001")
        @Digits(integer = 15, fraction = 4)
        BigDecimal amount,

        @NotBlank
        @Pattern(regexp = "[A-Z]{3}", message = "must be a 3-letter upper-case ISO-4217 code")
        String currency,

        @NotNull
        Instant eventTime,

        @Size(max = 32)
        String channel,

        Map<String, Object> metadata) {
}