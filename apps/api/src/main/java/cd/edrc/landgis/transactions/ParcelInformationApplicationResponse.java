package cd.edrc.landgis.transactions;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ParcelInformationApplicationResponse(
        UUID id,
        UUID parcelId,
        String applicationType,
        String status,
        UUID applicantUserId,
        String applicantActor,
        String purpose,
        String decisionReason,
        UUID generatedDocumentId,
        OffsetDateTime submittedAt,
        OffsetDateTime decidedAt,
        InvoiceResponse invoice) {
    public record InvoiceResponse(
            UUID id,
            String invoiceNumber,
            BigDecimal amount,
            String currency,
            String status,
            String provider,
            String paymentReference,
            OffsetDateTime paidAt) {
    }
}
