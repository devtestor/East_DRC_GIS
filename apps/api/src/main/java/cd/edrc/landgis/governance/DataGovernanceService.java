package cd.edrc.landgis.governance;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.DocumentClassification;
import cd.edrc.landgis.documents.DocumentNotFoundException;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataGovernanceService {
    private static final List<DocumentClassification> SENSITIVE_EXPORT_CLASSIFICATIONS = List.of(
            DocumentClassification.PROTECTED_PERSONAL,
            DocumentClassification.LEGAL_EVIDENCE,
            DocumentClassification.FINANCIAL,
            DocumentClassification.SECURITY);

    private final DocumentRecordRepository documents;
    private final RetentionPolicyRepository retentionPolicies;
    private final LegalHoldRepository legalHolds;
    private final AuditService auditService;
    private final Clock clock;

    @Autowired
    public DataGovernanceService(
            DocumentRecordRepository documents,
            RetentionPolicyRepository retentionPolicies,
            LegalHoldRepository legalHolds,
            AuditService auditService) {
        this(documents, retentionPolicies, legalHolds, auditService, Clock.systemUTC());
    }

    DataGovernanceService(
            DocumentRecordRepository documents,
            RetentionPolicyRepository retentionPolicies,
            LegalHoldRepository legalHolds,
            AuditService auditService,
            Clock clock) {
        this.documents = documents;
        this.retentionPolicies = retentionPolicies;
        this.legalHolds = legalHolds;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public LegalHold placeDocumentHold(
            UUID documentId,
            String holdReason,
            String authorityReference,
            AuthenticatedActor actor) {
        DocumentRecord document = document(documentId);
        LegalHold hold = legalHolds.save(new LegalHold(
                UUID.randomUUID(),
                "document",
                document.id(),
                requireText(holdReason, "holdReason"),
                requireText(authorityReference, "authorityReference"),
                actor.userId(),
                actor.username()));
        auditService.record(
                "governance.legal-hold-placed",
                "document",
                document.id().toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                document.custodianOrganizationId(),
                Map.of(
                        "holdReason", hold.holdReason(),
                        "authorityReference", hold.authorityReference(),
                        "retentionCategory", document.retentionCategory()));
        return hold;
    }

    @Transactional
    public LegalHold releaseDocumentHold(UUID documentId, String releaseReason, AuthenticatedActor actor) {
        DocumentRecord document = document(documentId);
        LegalHold hold = legalHolds.findByTargetTypeAndTargetIdAndStatus("document", document.id(), LegalHoldStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("No active legal hold exists for document " + document.id()));
        hold.release(actor.userId(), actor.username(), requireText(releaseReason, "releaseReason"));
        auditService.record(
                "governance.legal-hold-released",
                "document",
                document.id().toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                document.custodianOrganizationId(),
                Map.of("releaseReason", releaseReason, "retentionCategory", document.retentionCategory()));
        return hold;
    }

    @Transactional(readOnly = true)
    public RetentionDispositionDecision evaluateDocumentDisposition(UUID documentId) {
        DocumentRecord document = document(documentId);
        RetentionPolicy policy = retentionPolicies.findByRetentionCategory(document.retentionCategory())
                .orElse(null);
        List<String> blockers = new ArrayList<>();
        boolean activeHold = document.legalHold()
                || legalHolds.existsByTargetTypeAndTargetIdAndStatus("document", document.id(), LegalHoldStatus.ACTIVE);
        if (activeHold) {
            blockers.add("ACTIVE_LEGAL_HOLD");
        }
        if (policy == null) {
            blockers.add("MISSING_RETENTION_POLICY");
            return new RetentionDispositionDecision(false, true, true, List.copyOf(blockers));
        }
        OffsetDateTime minimumRetainUntil = document.createdAt().plusDays(policy.minimumRetentionDays());
        if (OffsetDateTime.now(clock).isBefore(minimumRetainUntil)) {
            blockers.add("MINIMUM_RETENTION_PERIOD_NOT_MET");
        }
        if (policy.protectedFromAutomatedDisposal()) {
            blockers.add("PROTECTED_FROM_AUTOMATED_DISPOSAL");
        }
        return new RetentionDispositionDecision(
                blockers.isEmpty() && !policy.disposalRequiresApproval(),
                policy.disposalRequiresApproval(),
                policy.archivalRequired(),
                List.copyOf(blockers));
    }

    @Transactional(readOnly = true)
    public PrivacyExportDecision evaluateDocumentExport(UUID documentId, boolean redactionPlanned, boolean approved) {
        DocumentRecord document = document(documentId);
        List<String> blockers = new ArrayList<>();
        boolean sensitive = SENSITIVE_EXPORT_CLASSIFICATIONS.contains(document.classification());
        boolean activeHold = document.legalHold()
                || legalHolds.existsByTargetTypeAndTargetIdAndStatus("document", document.id(), LegalHoldStatus.ACTIVE);
        if (activeHold) {
            blockers.add("ACTIVE_LEGAL_HOLD");
        }
        if (sensitive && !redactionPlanned) {
            blockers.add("REDACTION_REQUIRED");
        }
        if (sensitive && !approved) {
            blockers.add("APPROVAL_REQUIRED");
        }
        boolean redactionRequired = sensitive || document.classification() == DocumentClassification.STAFF_OPERATIONAL;
        boolean approvalRequired = sensitive || activeHold;
        return new PrivacyExportDecision(blockers.isEmpty(), redactionRequired, approvalRequired, List.copyOf(blockers));
    }

    private DocumentRecord document(UUID documentId) {
        return documents.findById(documentId).orElseThrow(() -> new DocumentNotFoundException(documentId));
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
