package cd.edrc.landgis.integrations.payment;

import java.util.UUID;

public record PaymentCallbackResponse(UUID callbackId, String status, String reconciliationStatus, boolean duplicate) {
}
