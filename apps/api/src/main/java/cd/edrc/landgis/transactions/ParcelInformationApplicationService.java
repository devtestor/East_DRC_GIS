package cd.edrc.landgis.transactions;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.CreateDocumentRequest;
import cd.edrc.landgis.documents.DigitalSignatureStatus;
import cd.edrc.landgis.documents.DocumentClassification;
import cd.edrc.landgis.documents.DocumentResponse;
import cd.edrc.landgis.documents.DocumentService;
import cd.edrc.landgis.documents.MalwareScanStatus;
import cd.edrc.landgis.notifications.NotificationService;
import cd.edrc.landgis.parcels.ParcelNotFoundException;
import cd.edrc.landgis.parcels.ParcelRepository;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.UnsupportedWorkflowTaskException;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelInformationApplicationService {
    private static final BigDecimal INFORMATION_REQUEST_FEE = new BigDecimal("10.00");
    private static final String CURRENCY = "USD";

    private final ParcelRepository parcels;
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final WorkflowTaskService workflowTasks;
    private final DocumentService documents;
    private final NotificationService notifications;

    ParcelInformationApplicationService(
            ParcelRepository parcels,
            JdbcTemplate jdbcTemplate,
            AuditService auditService,
            WorkflowTaskService workflowTasks,
            DocumentService documents) {
        this(parcels, jdbcTemplate, auditService, workflowTasks, documents, null);
    }

    @Autowired
    public ParcelInformationApplicationService(
            ParcelRepository parcels,
            JdbcTemplate jdbcTemplate,
            AuditService auditService,
            WorkflowTaskService workflowTasks,
            DocumentService documents,
            NotificationService notifications) {
        this.parcels = parcels;
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.workflowTasks = workflowTasks;
        this.documents = documents;
        this.notifications = notifications;
    }

    @Transactional
    public ParcelInformationApplicationResponse create(
            CreateParcelInformationApplicationRequest request,
            AuthenticatedActor actor) {
        requireParcel(request.parcelId());
        UUID applicationId = UUID.randomUUID();
        ParcelInformationApplicationResponse application = jdbcTemplate.queryForObject(
                """
                INSERT INTO transactions.applications
                    (id, parcel_id, application_type, status, applicant_user_id, applicant_actor, purpose)
                VALUES (?, ?, 'PARCEL_INFORMATION_REQUEST', 'PAYMENT_PENDING', ?, ?, ?)
                RETURNING id, parcel_id, application_type, status, applicant_user_id, applicant_actor,
                          purpose, decision_reason, generated_document_id, submitted_at, decided_at
                """,
                applicationMapper(), applicationId, request.parcelId(), actor.userId(), actor.username(), request.purpose().trim());
        ParcelInformationApplicationResponse.InvoiceResponse invoice = jdbcTemplate.queryForObject(
                """
                INSERT INTO payments.invoices (application_id, invoice_number, amount, currency, status, provider)
                VALUES (?, ?, ?, ?, 'PENDING', 'SANDBOX')
                RETURNING id, invoice_number, amount, currency, status, provider, payment_reference, paid_at
                """,
                invoiceMapper(), applicationId, "INV-" + applicationId, INFORMATION_REQUEST_FEE, CURRENCY);
        auditService.record(
                "parcel-information-application.submitted",
                "application",
                applicationId.toString(),
                AuditClassification.FINANCIAL,
                actor.userId(), null,
                java.util.Map.of("parcelId", request.parcelId().toString(), "invoiceId", invoice.id().toString(), "amount", invoice.amount()));
        notifyApplicant(actor.userId(), applicationId, "APPLICATION_SUBMITTED", "Demande recue",
                "Votre demande d'information parcellaire a ete recue.", actor);
        return withInvoice(application, invoice);
    }

    @Transactional(readOnly = true)
    public ParcelInformationApplicationResponse get(UUID applicationId, AuthenticatedActor actor) {
        ParcelInformationApplicationResponse application = find(applicationId);
        requireApplicantOrStaff(application, actor);
        return withInvoice(application, invoice(applicationId));
    }

    @Transactional(readOnly = true)
    public List<ParcelInformationApplicationResponse> listMine(AuthenticatedActor actor) {
        return jdbcTemplate.query(
                """
                SELECT id, parcel_id, application_type, status, applicant_user_id, applicant_actor,
                       purpose, decision_reason, generated_document_id, submitted_at, decided_at
                FROM transactions.applications
                WHERE applicant_user_id = ?
                ORDER BY submitted_at DESC
                LIMIT 100
                """,
                applicationMapper(), actor.userId()).stream()
                .map(application -> withInvoice(application, invoice(application.id())))
                .toList();
    }

    @Transactional
    public ParcelInformationApplicationResponse requestCorrection(
            UUID applicationId, String reason, AuthenticatedActor actor) {
        ParcelInformationApplicationResponse application = find(applicationId);
        if (!application.applicantUserId().equals(actor.userId())) {
            throw new IllegalArgumentException("Only the applicant can request a correction");
        }
        requireStatus(application, ParcelInformationApplicationStatus.REJECTED);
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Correction reason is required");
        }
        ParcelInformationApplicationResponse updated = jdbcTemplate.queryForObject(
                """
                UPDATE transactions.applications
                SET status = 'CORRECTION_REQUESTED', decision_reason = ?, updated_at = now(), version = version + 1
                WHERE id = ? AND applicant_user_id = ? AND status = 'REJECTED'
                RETURNING id, parcel_id, application_type, status, applicant_user_id, applicant_actor,
                          purpose, decision_reason, generated_document_id, submitted_at, decided_at
                """,
                applicationMapper(), reason.trim(), applicationId, actor.userId());
        auditService.record("parcel-information-application.correction-requested", "application",
                applicationId.toString(), AuditClassification.LEGAL_EVIDENCE, actor.userId(), null,
                java.util.Map.of("reasonProvided", true));
        notifyApplicant(actor.userId(), applicationId, "CORRECTION_REQUESTED", "Correction demandee",
                "Votre demande a ete renvoyee pour correction et attend une nouvelle instruction.", actor);
        return withInvoice(updated, invoice(applicationId));
    }

    @Transactional
    public ParcelInformationApplicationResponse confirmSandboxPayment(
            UUID applicationId, PaymentConfirmationRequest request, AuthenticatedActor actor) {
        ParcelInformationApplicationResponse application = find(applicationId);
        requireApplicantOrStaff(application, actor);
        ParcelInformationApplicationResponse.InvoiceResponse currentInvoice = invoice(applicationId);
        if ("PAID".equals(currentInvoice.status())) {
            return withInvoice(application, currentInvoice);
        }
        ParcelInformationApplicationResponse.InvoiceResponse paid = jdbcTemplate.queryForObject(
                """
                UPDATE payments.invoices
                SET status = 'PAID', payment_reference = ?, paid_at = now(), updated_at = now(), version = version + 1
                WHERE application_id = ? AND status = 'PENDING'
                RETURNING id, invoice_number, amount, currency, status, provider, payment_reference, paid_at
                """,
                invoiceMapper(), request.paymentReference().trim(), applicationId);
        jdbcTemplate.update(
                "UPDATE transactions.applications SET status = 'UNDER_REVIEW', updated_at = now(), version = version + 1 WHERE id = ? AND status = 'PAYMENT_PENDING'",
                applicationId);
        auditService.record(
                "parcel-information-application.payment-confirmed",
                "application",
                applicationId.toString(),
                AuditClassification.FINANCIAL,
                actor.userId(), null,
                java.util.Map.of("invoiceId", paid.id().toString(), "provider", paid.provider(), "sandbox", true));
        notifyApplicant(application.applicantUserId(), applicationId, "PAYMENT_CONFIRMED", "Paiement confirme",
                "Le paiement de votre demande a ete confirme. Elle attend une revue humaine.", actor);
        return withInvoice(find(applicationId), paid);
    }

    @Transactional
    public ParcelInformationReviewResponse requestReview(UUID applicationId, AuthenticatedActor actor) {
        ParcelInformationApplicationResponse application = find(applicationId);
        requireStaff(actor);
        requireStatus(application, ParcelInformationApplicationStatus.UNDER_REVIEW);
        UUID taskId = workflowTasks.openParcelInformationReviewTask(applicationId, actor);
        auditService.record("parcel-information-application.review-requested", "application", applicationId.toString(),
                AuditClassification.STAFF_OPERATIONAL, actor.userId(), null,
                java.util.Map.of("parcelId", application.parcelId().toString(), "taskId", taskId.toString()));
        return new ParcelInformationReviewResponse(application, "APPROVE_PARCEL_INFORMATION_REQUEST", "OPEN", taskId);
    }

    @Transactional
    public ParcelInformationReviewResponse decideReviewTask(
            UUID taskId, DecideWorkflowTaskRequest request, AuthenticatedActor actor) {
        WorkflowTask existing = workflowTasks.getTask(taskId);
        if (!"PARCEL_INFORMATION_REQUEST_REVIEW".equals(existing.workflowType())
                || !"parcel-information-application".equals(existing.targetType())
                || !"APPROVE_PARCEL_INFORMATION_REQUEST".equals(existing.requestedAction())) {
            throw new UnsupportedWorkflowTaskException(taskId);
        }
        ParcelInformationApplicationResponse current = find(existing.targetId());
        requireStatus(current, ParcelInformationApplicationStatus.UNDER_REVIEW);
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        ParcelInformationApplicationResponse updated = request.decision() == WorkflowDecision.APPROVE
                ? approve(existing.targetId(), request.reason(), actor)
                : reject(existing.targetId(), request.reason(), actor);
        auditService.record(
                request.decision() == WorkflowDecision.APPROVE ? "parcel-information-application.approved" : "parcel-information-application.rejected",
                "application", existing.targetId().toString(), AuditClassification.LEGAL_EVIDENCE,
                actor.userId(), null,
                java.util.Map.of("taskId", taskId.toString(), "decision", request.decision().name(), "legalBoundary", "Information-service output only; not an official title"));
        notifyApplicant(current.applicantUserId(), current.id(),
                request.decision() == WorkflowDecision.APPROVE ? "APPLICATION_APPROVED" : "APPLICATION_REJECTED",
                request.decision() == WorkflowDecision.APPROVE ? "Demande approuvee" : "Demande rejetee",
                request.decision() == WorkflowDecision.APPROVE
                        ? "Votre rapport d'information parcellaire est disponible dans votre espace securise."
                        : "Votre demande d'information parcellaire a ete rejetee. Consultez le motif dans votre dossier.",
                actor);
        return new ParcelInformationReviewResponse(withInvoice(updated, invoice(existing.targetId())), task.requestedAction(), task.status().name(), task.id());
    }

    private void notifyApplicant(
            UUID recipientUserId,
            UUID applicationId,
            String type,
            String title,
            String message,
            AuthenticatedActor actor) {
        if (notifications == null) {
            return;
        }
        notifications.createForUser(
                recipientUserId, type, title, message, "application", applicationId,
                type + ":" + applicationId, actor);
    }

    private ParcelInformationApplicationResponse approve(UUID applicationId, String reason, AuthenticatedActor actor) {
        String documentSeed = "parcel-information:" + applicationId + ":" + reason;
        String checksum = HexFormat.of().formatHex(sha256(documentSeed));
        DocumentResponse document = documents.create(new CreateDocumentRequest(
                "PARCEL_INFORMATION_REPORT", "application", applicationId, "Parcel information report",
                DocumentClassification.STAFF_OPERATIONAL, "SERVICE_RECORD", "application-applicant-and-authorized-staff",
                null, null, false, null, "generated/applications/" + applicationId + ".pdf",
                "parcel-information-report.pdf", "application/pdf", 0, checksum,
                MalwareScanStatus.NOT_REQUIRED, DigitalSignatureStatus.UNSIGNED), actor);
        return jdbcTemplate.queryForObject(
                """
                UPDATE transactions.applications
                SET status = 'APPROVED', decision_reason = ?, generated_document_id = ?, decided_at = now(), decided_by_user_id = ?, decided_by = ?, updated_at = now(), version = version + 1
                WHERE id = ?
                RETURNING id, parcel_id, application_type, status, applicant_user_id, applicant_actor, purpose, decision_reason, generated_document_id, submitted_at, decided_at
                """,
                applicationMapper(), reason, document.id(), actor.userId(), actor.username(), applicationId);
    }

    private ParcelInformationApplicationResponse reject(UUID applicationId, String reason, AuthenticatedActor actor) {
        return jdbcTemplate.queryForObject(
                """
                UPDATE transactions.applications
                SET status = 'REJECTED', decision_reason = ?, decided_at = now(), decided_by_user_id = ?, decided_by = ?, updated_at = now(), version = version + 1
                WHERE id = ?
                RETURNING id, parcel_id, application_type, status, applicant_user_id, applicant_actor, purpose, decision_reason, generated_document_id, submitted_at, decided_at
                """,
                applicationMapper(), reason, actor.userId(), actor.username(), applicationId);
    }

    private ParcelInformationApplicationResponse find(UUID applicationId) {
        List<ParcelInformationApplicationResponse> matches = jdbcTemplate.query(
                "SELECT id, parcel_id, application_type, status, applicant_user_id, applicant_actor, purpose, decision_reason, generated_document_id, submitted_at, decided_at FROM transactions.applications WHERE id = ?",
                applicationMapper(), applicationId);
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Application not found: " + applicationId);
        }
        return matches.getFirst();
    }

    private ParcelInformationApplicationResponse.InvoiceResponse invoice(UUID applicationId) {
        return jdbcTemplate.queryForObject(
                "SELECT id, invoice_number, amount, currency, status, provider, payment_reference, paid_at FROM payments.invoices WHERE application_id = ?",
                invoiceMapper(), applicationId);
    }

    private ParcelInformationApplicationResponse withInvoice(ParcelInformationApplicationResponse application,
            ParcelInformationApplicationResponse.InvoiceResponse invoice) {
        return new ParcelInformationApplicationResponse(application.id(), application.parcelId(), application.applicationType(),
                application.status(), application.applicantUserId(), application.applicantActor(), application.purpose(),
                application.decisionReason(), application.generatedDocumentId(), application.submittedAt(), application.decidedAt(), invoice);
    }

    private void requireApplicantOrStaff(ParcelInformationApplicationResponse application, AuthenticatedActor actor) {
        if (!application.applicantUserId().equals(actor.userId()) && !hasStaffRole(actor)) {
            throw new IllegalArgumentException("Actor is not authorized to access this application");
        }
    }

    private boolean hasStaffRole(AuthenticatedActor actor) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM identity.organization_memberships membership JOIN identity.roles role ON role.id = membership.role_id WHERE membership.user_id = ? AND membership.status = 'ACTIVE' AND role.code IN ('LAND_TITLE_OFFICER', 'CADASTRAL_OFFICER', 'SECURITY_OFFICER')",
                Integer.class, actor.userId());
        return count != null && count > 0;
    }

    private void requireStaff(AuthenticatedActor actor) {
        if (!hasStaffRole(actor)) {
            throw new IllegalArgumentException("Staff role required");
        }
    }

    private void requireStatus(ParcelInformationApplicationResponse application, ParcelInformationApplicationStatus expected) {
        if (!expected.name().equals(application.status())) {
            throw new IllegalStateException("Application must be " + expected.name());
        }
    }

    private void requireParcel(UUID parcelId) {
        if (!parcels.existsById(parcelId)) {
            throw new ParcelNotFoundException(parcelId);
        }
    }

    private RowMapper<ParcelInformationApplicationResponse> applicationMapper() {
        return (resultSet, rowNum) -> new ParcelInformationApplicationResponse(
                resultSet.getObject("id", UUID.class), resultSet.getObject("parcel_id", UUID.class),
                resultSet.getString("application_type"), resultSet.getString("status"),
                resultSet.getObject("applicant_user_id", UUID.class), resultSet.getString("applicant_actor"),
                resultSet.getString("purpose"), resultSet.getString("decision_reason"),
                resultSet.getObject("generated_document_id", UUID.class),
                resultSet.getObject("submitted_at", java.time.OffsetDateTime.class),
                resultSet.getObject("decided_at", java.time.OffsetDateTime.class), null);
    }

    private RowMapper<ParcelInformationApplicationResponse.InvoiceResponse> invoiceMapper() {
        return (resultSet, rowNum) -> new ParcelInformationApplicationResponse.InvoiceResponse(
                resultSet.getObject("id", UUID.class), resultSet.getString("invoice_number"),
                resultSet.getBigDecimal("amount"), resultSet.getString("currency"), resultSet.getString("status"),
                resultSet.getString("provider"), resultSet.getString("payment_reference"),
                resultSet.getObject("paid_at", java.time.OffsetDateTime.class));
    }

    private byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
