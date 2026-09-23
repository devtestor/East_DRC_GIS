package cd.edrc.landgis.integrations.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCallbackRequest(
        @NotBlank String providerEventId,
        @NotNull UUID invoiceId,
        @NotBlank String status,
        @NotBlank String providerReference,
        @NotNull BigDecimal amount,
        @NotBlank String currency,
        @NotBlank String payload) {
}
