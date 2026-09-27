package cd.edrc.landgis.governance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.DigitalSignatureStatus;
import cd.edrc.landgis.documents.DocumentClassification;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import cd.edrc.landgis.documents.DocumentVersionRecord;
import cd.edrc.landgis.documents.DocumentVersionRecordRepository;
import cd.edrc.landgis.documents.MalwareScanStatus;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DataGovernanceServiceTest {
    private static final Clock FUTURE_CLOCK = Clock.fixed(Instant.parse("2040-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void blocksAutomatedDispositionWhenDocumentHasActiveLegalHold() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.LEGAL_EVIDENCE, "LEGAL_RECORD", true);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(policies.findByRetentionCategory("LEGAL_RECORD")).thenReturn(Optional.of(new RetentionPolicy(
                UUID.randomUUID(),
                "LEGAL_RECORD",
                "Legal evidence",
                1,
                true,
                true,
                true)));
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        RetentionDispositionDecision decision = service.evaluateDocumentDisposition(documentId);

        assertThat(decision.eligibleForAutomatedDisposal()).isFalse();
        assertThat(decision.approvalRequired()).isTrue();
        assertThat(decision.archivalRequired()).isTrue();
        assertThat(decision.blockers()).contains("ACTIVE_LEGAL_HOLD", "PROTECTED_FROM_AUTOMATED_DISPOSAL");
    }

    @Test
    void permitsAutomatedDispositionOnlyWhenPolicyAllowsAndNoBlockersExist() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.PUBLIC, "PUBLIC_RECORD", false);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(holds.existsByTargetTypeAndTargetIdAndStatus("document", documentId, LegalHoldStatus.ACTIVE))
                .thenReturn(false);
        when(policies.findByRetentionCategory("PUBLIC_RECORD")).thenReturn(Optional.of(new RetentionPolicy(
                UUID.randomUUID(),
                "PUBLIC_RECORD",
                "Public projection",
                0,
                false,
                false,
                false)));
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        RetentionDispositionDecision decision = service.evaluateDocumentDisposition(documentId);

        assertThat(decision.eligibleForAutomatedDisposal()).isTrue();
        assertThat(decision.approvalRequired()).isFalse();
        assertThat(decision.archivalRequired()).isFalse();
        assertThat(decision.blockers()).isEmpty();
    }

    @Test
    void blocksSensitiveExportWithoutRedactionAndApproval() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.PROTECTED_PERSONAL, "IDENTITY_RECORD", false);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(holds.existsByTargetTypeAndTargetIdAndStatus("document", documentId, LegalHoldStatus.ACTIVE))
                .thenReturn(false);
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        PrivacyExportDecision decision = service.evaluateDocumentExport(documentId, false, false);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.redactionRequired()).isTrue();
        assertThat(decision.approvalRequired()).isTrue();
        assertThat(decision.blockers())
                .containsExactlyInAnyOrder("DOCUMENT_VERSION_MISSING", "REDACTION_REQUIRED", "APPROVAL_REQUIRED");
    }

    @Test
    void blocksExportWhenDocumentHasNoImmutableVersion() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.PUBLIC, "PUBLIC_RECORD", false);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(holds.existsByTargetTypeAndTargetIdAndStatus("document", documentId, LegalHoldStatus.ACTIVE))
                .thenReturn(false);
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of());
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        PrivacyExportDecision decision = service.evaluateDocumentExport(documentId, true, false);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.blockers()).containsExactly("DOCUMENT_VERSION_MISSING");
    }

    @Test
    void blocksExportWhenLatestDocumentVersionHasPendingMalwareScan() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.STAFF_OPERATIONAL, "SERVICE_RECORD", false);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(holds.existsByTargetTypeAndTargetIdAndStatus("document", documentId, LegalHoldStatus.ACTIVE))
                .thenReturn(false);
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of(
                version(documentId, 1, MalwareScanStatus.PASSED, DigitalSignatureStatus.UNSIGNED),
                version(documentId, 2, MalwareScanStatus.PENDING, DigitalSignatureStatus.UNKNOWN)));
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        PrivacyExportDecision decision = service.evaluateDocumentExport(documentId, true, true);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.blockers()).containsExactly("MALWARE_SCAN_PENDING");
    }

    @Test
    void blocksExportWhenLatestDocumentVersionFailedMalwareScanOrHasInvalidSignature() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.LEGAL_EVIDENCE, "LEGAL_RECORD", false);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(holds.existsByTargetTypeAndTargetIdAndStatus("document", documentId, LegalHoldStatus.ACTIVE))
                .thenReturn(false);
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of(
                version(documentId, 1, MalwareScanStatus.PASSED, DigitalSignatureStatus.VALID),
                version(documentId, 2, MalwareScanStatus.FAILED, DigitalSignatureStatus.INVALID)));
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        PrivacyExportDecision decision = service.evaluateDocumentExport(documentId, true, true);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.blockers()).containsExactlyInAnyOrder("MALWARE_SCAN_FAILED", "DIGITAL_SIGNATURE_INVALID");
    }

    @Test
    void blocksExportWhenLatestDocumentVersionIsQuarantined() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.PUBLIC, "PUBLIC_RECORD", false);
        DocumentVersionRecord quarantinedVersion =
                version(documentId, 1, MalwareScanStatus.PASSED, DigitalSignatureStatus.VALID);
        quarantinedVersion.quarantine(
                "Manual document security review quarantined the evidence",
                UUID.randomUUID(),
                "security@example.test");
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(holds.existsByTargetTypeAndTargetIdAndStatus("document", documentId, LegalHoldStatus.ACTIVE))
                .thenReturn(false);
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of(quarantinedVersion));
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        PrivacyExportDecision decision = service.evaluateDocumentExport(documentId, true, true);

        assertThat(decision.allowed()).isFalse();
        assertThat(decision.blockers()).containsExactly("DOCUMENT_VERSION_QUARANTINED");
    }

    @Test
    void recordsAuditWhenPlacingLegalHold() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.LEGAL_EVIDENCE, "LEGAL_RECORD", false);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        RetentionPolicyRepository policies = Mockito.mock(RetentionPolicyRepository.class);
        LegalHoldRepository holds = Mockito.mock(LegalHoldRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(holds.save(any(LegalHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DataGovernanceService service = new DataGovernanceService(documents, versions, policies, holds, audit, FUTURE_CLOCK);

        LegalHold hold = service.placeDocumentHold(documentId, "court restriction", "fictional-court-2026-001", actor);

        assertThat(hold.status()).isEqualTo(LegalHoldStatus.ACTIVE);
        assertThat(hold.targetType()).isEqualTo("document");
        assertThat(hold.targetId()).isEqualTo(documentId);
        verify(audit).record(
                eq("governance.legal-hold-placed"),
                eq("document"),
                eq(documentId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                eq(document.custodianOrganizationId()),
                anyMap());
    }

    private DocumentRecord document(
            UUID documentId,
            DocumentClassification classification,
            String retentionCategory,
            boolean legalHold) {
        return new DocumentRecord(
                documentId,
                "FICTIONAL_DOCUMENT",
                "parcel",
                UUID.randomUUID(),
                "Fictional governance document",
                classification,
                retentionCategory,
                "restricted-staff",
                UUID.randomUUID(),
                "LAND_TITLE_OFFICER",
                legalHold,
                UUID.randomUUID(),
                "creator@example.test",
                null);
    }

    private DocumentVersionRecord version(
            UUID documentId,
            int versionNumber,
            MalwareScanStatus malwareScanStatus,
            DigitalSignatureStatus digitalSignatureStatus) {
        return new DocumentVersionRecord(
                UUID.randomUUID(),
                documentId,
                versionNumber,
                "sandbox/documents/version-" + versionNumber + ".pdf",
                "version-" + versionNumber + ".pdf",
                "application/pdf",
                128,
                "a".repeat(64),
                malwareScanStatus,
                digitalSignatureStatus,
                UUID.randomUUID(),
                "uploader@example.test");
    }
}
