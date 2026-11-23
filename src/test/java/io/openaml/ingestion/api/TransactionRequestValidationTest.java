package io.openaml.ingestion.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void createValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    /** Builds a request where only eventId, amount and currency vary; everything else is valid. */
    private static TransactionRequest request(String eventId, String amount, String currency) {
        return new TransactionRequest(
                eventId,
                "ACCOUNT-A",
                "ACCOUNT-B",
                amount == null ? null : new BigDecimal(amount),
                currency,
                Instant.parse("2027-01-15T10:30:00Z"),
                "mobile",
                Map.of());
    }

    /** Returns the names of the fields that failed validation. */
    private static Set<String> invalidFields(TransactionRequest request) {
        Set<ConstraintViolation<TransactionRequest>> violations = validator.validate(request);
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void validRequestPasses() {
        assertThat(invalidFields(request("TX-1042", "2400.00", "CAD"))).isEmpty();
    }

    @Test
    void missingEventIdIsRejected() {
        assertThat(invalidFields(request(null, "2400.00", "CAD"))).containsExactly("eventId");
    }

    @Test
    void eventIdWithSpacesIsRejected() {
        assertThat(invalidFields(request("TX 1042", "2400.00", "CAD"))).containsExactly("eventId");
    }

    @Test
    void zeroAndNegativeAmountsAreRejected() {
        assertThat(invalidFields(request("TX-1", "0", "CAD"))).containsExactly("amount");
        assertThat(invalidFields(request("TX-2", "-50", "CAD"))).containsExactly("amount");
    }

    @Test
    void moreThanFourDecimalsIsRejected() {
        assertThat(invalidFields(request("TX-1", "10.12345", "CAD"))).containsExactly("amount");
    }

    @Test
    void lowercaseCurrencyIsRejected() {
        assertThat(invalidFields(request("TX-1", "10.00", "cad"))).containsExactly("currency");
    }

    @Test
    void optionalFieldsMayBeMissing() {
        TransactionRequest noOptionals = new TransactionRequest(
                "TX-1", "ACCOUNT-A", "ACCOUNT-B", new BigDecimal("10"), "CAD",
                Instant.parse("2027-01-15T10:30:00Z"), null, null);
        assertThat(invalidFields(noOptionals)).isEmpty();
    }
}