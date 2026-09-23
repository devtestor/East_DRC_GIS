package cd.edrc.landgis.integrations.payment;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentCallbackService {
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;

    public PaymentCallbackService(JdbcTemplate jdbcTemplate, AuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public PaymentCallbackResponse receive(String provider, PaymentCallbackRequest request, AuthenticatedActor actor) {
        String normalizedProvider = provider.trim().toUpperCase(java.util.Locale.ROOT);
        String checksum = checksum(request.payload());
        UUID callbackId;
        try {
            callbackId = jdbcTemplate.queryForObject(
                    "INSERT INTO payments.provider_callbacks (provider, provider_event_id, invoice_id, event_status, payload_checksum) VALUES (?, ?, ?, 'RECEIVED', ?) RETURNING id",
                    (ResultSet resultSet, int rowNum) -> resultSet.getObject("id", UUID.class),
                    normalizedProvider, request.providerEventId().trim(), request.invoiceId(), checksum);
        } catch (DuplicateKeyException duplicate) {
            PaymentCallbackResponse existing = jdbcTemplate.queryForObject(
                    "SELECT id, event_status FROM payments.provider_callbacks WHERE provider = ? AND provider_event_id = ?",
                    (resultSet, rowNum) -> new PaymentCallbackResponse(resultSet.getObject("id", UUID.class), resultSet.getString("event_status"), "DUPLICATE", true),
                    normalizedProvider, request.providerEventId().trim());
            return existing;
        }

        String reconciliation = reconcile(normalizedProvider, request);
        String eventStatus = "MATCHED".equals(reconciliation) && "SUCCESS".equalsIgnoreCase(request.status()) ? "APPLIED" : "IGNORED";
        if ("APPLIED".equals(eventStatus)) {
            jdbcTemplate.update(
                    "UPDATE payments.invoices SET status = 'PAID', payment_reference = ?, paid_at = COALESCE(paid_at, now()), updated_at = now(), version = version + 1 WHERE id = ? AND status IN ('PENDING', 'FAILED')",
                    request.providerReference().trim(), request.invoiceId());
        }
        jdbcTemplate.update("UPDATE payments.provider_callbacks SET event_status = ?, processed_at = now() WHERE id = ?", eventStatus, callbackId);
        auditService.record("payment.callback-processed", "payment-callback", callbackId.toString(),
                AuditClassification.FINANCIAL, actor == null ? null : actor.userId(), null,
                java.util.Map.of("provider", normalizedProvider, "status", eventStatus, "reconciliation", reconciliation));
        return new PaymentCallbackResponse(callbackId, eventStatus, reconciliation, false);
    }

    private String reconcile(String provider, PaymentCallbackRequest request) {
        String result = jdbcTemplate.queryForObject(
                """
                INSERT INTO payments.reconciliation_entries
                    (provider, provider_reference, invoice_id, expected_amount, received_amount, currency, status)
                SELECT ?, ?, id, amount, ?, ?,
                       CASE WHEN amount = ? AND currency = ? THEN 'MATCHED'
                            WHEN currency <> ? THEN 'CURRENCY_MISMATCH'
                            ELSE 'AMOUNT_MISMATCH' END
                FROM payments.invoices WHERE id = ?
                ON CONFLICT (provider, provider_reference) DO UPDATE SET status = payments.reconciliation_entries.status
                RETURNING status
                """,
                (resultSet, rowNum) -> resultSet.getString("status"), provider, request.providerReference(),
                request.amount(), request.currency(), request.amount(), request.currency(),
                request.currency(), request.invoiceId());
        return result;
    }

    private String checksum(String payload) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
