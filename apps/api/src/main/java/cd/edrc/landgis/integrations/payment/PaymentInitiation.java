package cd.edrc.landgis.integrations.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentInitiation(UUID invoiceId, BigDecimal amount, String currency, String callbackReference) {
}
