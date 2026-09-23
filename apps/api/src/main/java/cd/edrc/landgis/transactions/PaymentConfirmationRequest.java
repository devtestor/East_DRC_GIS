package cd.edrc.landgis.transactions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PaymentConfirmationRequest(
        @NotBlank @Size(max = 120) String paymentReference) {
}
