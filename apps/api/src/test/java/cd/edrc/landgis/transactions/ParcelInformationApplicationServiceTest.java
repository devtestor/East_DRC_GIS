package cd.edrc.landgis.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.DocumentService;
import cd.edrc.landgis.parcels.ParcelRepository;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class ParcelInformationApplicationServiceTest {
    @Test
    void submitsApplicationWithPendingSandboxInvoice() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        DocumentService documents = Mockito.mock(DocumentService.class);
        UUID parcelId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "citizen@example.test");
        ParcelInformationApplicationResponse application = application(applicationId, parcelId, actor, "PAYMENT_PENDING");
        ParcelInformationApplicationResponse.InvoiceResponse invoice = invoice(applicationId, "PENDING");
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.queryForObject(anyString(), ArgumentMatchers.<RowMapper<ParcelInformationApplicationResponse>>any(),
                any(), eq(parcelId), eq(actor.userId()), eq(actor.username()), eq("Need parcel information")))
                .thenReturn(application);
        when(jdbc.queryForObject(anyString(), ArgumentMatchers.<RowMapper<ParcelInformationApplicationResponse.InvoiceResponse>>any(),
                any(), anyString(), eq(new BigDecimal("10.00")), eq("USD"))).thenReturn(invoice);
        ParcelInformationApplicationService service = new ParcelInformationApplicationService(
                parcels, jdbc, audit, workflows, documents);

        ParcelInformationApplicationResponse response = service.create(
                new CreateParcelInformationApplicationRequest(parcelId, " Need parcel information "), actor);

        assertThat(response.status()).isEqualTo("PAYMENT_PENDING");
        assertThat(response.invoice().status()).isEqualTo("PENDING");
        assertThat(response.invoice().amount()).isEqualByComparingTo("10.00");
        verify(audit).record(eq("parcel-information-application.submitted"), eq("application"),
                anyString(), eq(AuditClassification.FINANCIAL), eq(actor.userId()), eq(null), any());
    }

    @Test
    void sandboxPaymentConfirmationIsIdempotentForAlreadyPaidInvoice() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        DocumentService documents = Mockito.mock(DocumentService.class);
        UUID parcelId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "citizen@example.test");
        ParcelInformationApplicationResponse application = application(applicationId, parcelId, actor, "UNDER_REVIEW");
        ParcelInformationApplicationResponse.InvoiceResponse paid = invoice(applicationId, "PAID");
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<ParcelInformationApplicationResponse>>any(), eq(applicationId)))
                .thenReturn(List.of(application));
        when(jdbc.queryForObject(anyString(), ArgumentMatchers.<RowMapper<ParcelInformationApplicationResponse.InvoiceResponse>>any(), eq(applicationId)))
                .thenReturn(paid);
        ParcelInformationApplicationService service = new ParcelInformationApplicationService(
                parcels, jdbc, audit, workflows, documents);

        ParcelInformationApplicationResponse response = service.confirmSandboxPayment(
                applicationId, new PaymentConfirmationRequest("sandbox-duplicate"), actor);

        assertThat(response.invoice().status()).isEqualTo("PAID");
        Mockito.verify(jdbc, Mockito.never()).update(anyString(), eq(applicationId));
    }

    private static ParcelInformationApplicationResponse application(
            UUID applicationId, UUID parcelId, AuthenticatedActor actor, String status) {
        return new ParcelInformationApplicationResponse(
                applicationId, parcelId, "PARCEL_INFORMATION_REQUEST", status, actor.userId(), actor.username(),
                "Need parcel information", null, null, OffsetDateTime.now(), null, null);
    }

    private static ParcelInformationApplicationResponse.InvoiceResponse invoice(UUID applicationId, String status) {
        return new ParcelInformationApplicationResponse.InvoiceResponse(
                UUID.randomUUID(), "INV-" + applicationId, new BigDecimal("10.00"), "USD", status,
                "SANDBOX", status.equals("PAID") ? "sandbox-payment" : null,
                status.equals("PAID") ? OffsetDateTime.now() : null);
    }
}
