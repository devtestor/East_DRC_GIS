package cd.edrc.landgis.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DocumentServiceTest {
    @Test
    void createsDocumentMetadataWithFirstVersion() {
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentAccessAuthorizer access = Mockito.mock(DocumentAccessAuthorizer.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.save(any(DocumentRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(versions.save(any(DocumentVersionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DocumentService service = new DocumentService(documents, versions, access, audit);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");

        DocumentResponse response = service.create(new CreateDocumentRequest(
                "SURVEY_PLAN",
                "parcel",
                UUID.randomUUID(),
                "Fictional survey plan",
                DocumentClassification.LEGAL_EVIDENCE,
                "LEGAL_RECORD",
                "workflow-task-and-authorized-staff",
                null,
                null,
                false,
                null,
                "documents/fictional/survey-plan.pdf",
                "survey-plan.pdf",
                "application/pdf",
                128L,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                MalwareScanStatus.PENDING,
                DigitalSignatureStatus.UNSIGNED), actor);

        assertThat(response.id()).isNotNull();
        assertThat(response.classification()).isEqualTo(DocumentClassification.LEGAL_EVIDENCE.name());
        assertThat(response.createdByUserId()).isEqualTo(actor.userId());
        assertThat(response.versions()).hasSize(1);
        assertThat(response.versions().get(0).versionNumber()).isEqualTo(1);
        assertThat(response.versions().get(0).checksumSha256())
                .isEqualTo("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        verify(audit).record(
                eq("document.metadata-created"),
                eq("document"),
                eq(response.id().toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                isNull(),
                anyMap());
    }

    @Test
    void returnsDocumentsByOwnerWithVersions() {
        UUID ownerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = new DocumentRecord(
                documentId,
                "IDENTITY_EVIDENCE",
                "party",
                ownerId,
                "Fictional identity evidence",
                DocumentClassification.PROTECTED_PERSONAL,
                "IDENTITY_RECORD",
                "restricted-staff",
                null,
                null,
                false,
                UUID.randomUUID(),
                "officer@example.test",
                null);
        DocumentVersionRecord version = new DocumentVersionRecord(
                UUID.randomUUID(),
                documentId,
                1,
                "documents/fictional/id.pdf",
                "id.pdf",
                "application/pdf",
                64L,
                "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb",
                MalwareScanStatus.PASSED,
                DigitalSignatureStatus.UNKNOWN,
                UUID.randomUUID(),
                "officer@example.test");
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentAccessAuthorizer access = Mockito.mock(DocumentAccessAuthorizer.class);
        AuditService audit = Mockito.mock(AuditService.class);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(documents.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc("party", ownerId)).thenReturn(List.of(document));
        when(access.canRead(document, actor)).thenReturn(true);
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of(version));
        DocumentService service = new DocumentService(documents, versions, access, audit);

        List<DocumentResponse> response = service.findByOwner("party", ownerId, actor);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).versions()).hasSize(1);
        verify(audit).record(
                eq("document.owner-list-viewed"),
                eq("document-owner"),
                eq("party:" + ownerId),
                eq(AuditClassification.STAFF_OPERATIONAL),
                eq(actor.userId()),
                isNull(),
                anyMap());
    }

    @Test
    void createsDocumentWithCustodianScope() {
        UUID custodianOrganizationId = UUID.randomUUID();
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentAccessAuthorizer access = Mockito.mock(DocumentAccessAuthorizer.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.save(any(DocumentRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(versions.save(any(DocumentVersionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DocumentService service = new DocumentService(documents, versions, access, audit);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");

        DocumentResponse response = service.create(new CreateDocumentRequest(
                "SURVEY_PLAN",
                "parcel",
                UUID.randomUUID(),
                "Fictional survey plan",
                DocumentClassification.LEGAL_EVIDENCE,
                "LEGAL_RECORD",
                "restricted-staff",
                custodianOrganizationId,
                "cadastral_officer",
                false,
                null,
                "documents/fictional/survey-plan-custody.pdf",
                "survey-plan-custody.pdf",
                "application/pdf",
                128L,
                "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd",
                MalwareScanStatus.PENDING,
                DigitalSignatureStatus.UNSIGNED), actor);

        assertThat(response.custodianOrganizationId()).isEqualTo(custodianOrganizationId);
        assertThat(response.custodianRoleCode()).isEqualTo("CADASTRAL_OFFICER");
    }

    @Test
    void appendsImmutableDocumentVersion() {
        UUID documentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DocumentRecord document = new DocumentRecord(
                documentId,
                "SURVEY_PLAN",
                "parcel",
                ownerId,
                "Fictional survey plan",
                DocumentClassification.LEGAL_EVIDENCE,
                "LEGAL_RECORD",
                "workflow-task-and-authorized-staff",
                null,
                null,
                false,
                actor.userId(),
                actor.username(),
                null);
        DocumentVersionRecord versionOne = new DocumentVersionRecord(
                UUID.randomUUID(),
                documentId,
                1,
                "documents/fictional/survey-plan-v1.pdf",
                "survey-plan-v1.pdf",
                "application/pdf",
                128L,
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                MalwareScanStatus.PASSED,
                DigitalSignatureStatus.UNSIGNED,
                actor.userId(),
                actor.username());
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentAccessAuthorizer access = Mockito.mock(DocumentAccessAuthorizer.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(access.canAppendVersion(document, actor)).thenReturn(true);
        when(versions.countByDocumentId(documentId)).thenReturn(1);
        when(versions.save(any(DocumentVersionRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenAnswer(invocation -> List.of(
                versionOne,
                new DocumentVersionRecord(
                        UUID.randomUUID(),
                        documentId,
                        2,
                        "documents/fictional/survey-plan-v2.pdf",
                        "survey-plan-v2.pdf",
                        "application/pdf",
                        192L,
                        "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
                        MalwareScanStatus.PENDING,
                        DigitalSignatureStatus.UNKNOWN,
                        actor.userId(),
                        actor.username())));
        DocumentService service = new DocumentService(documents, versions, access, audit);

        DocumentResponse response = service.addVersion(documentId, new AddDocumentVersionRequest(
                "documents/fictional/survey-plan-v2.pdf",
                "survey-plan-v2.pdf",
                "application/pdf",
                192L,
                "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
                MalwareScanStatus.PENDING,
                DigitalSignatureStatus.UNKNOWN), actor);

        assertThat(response.versions()).hasSize(2);
        assertThat(response.versions().get(0).versionNumber()).isEqualTo(1);
        assertThat(response.versions().get(1).versionNumber()).isEqualTo(2);
        verify(audit).record(
                eq("document.version-appended"),
                eq("document"),
                eq(documentId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                isNull(),
                anyMap());
    }

    @Test
    void auditsSuccessfulDocumentRead() {
        UUID documentId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DocumentRecord document = new DocumentRecord(
                documentId,
                "IDENTITY_EVIDENCE",
                "party",
                UUID.randomUUID(),
                "Fictional identity evidence",
                DocumentClassification.PROTECTED_PERSONAL,
                "IDENTITY_RECORD",
                "restricted-staff",
                null,
                null,
                false,
                actor.userId(),
                actor.username(),
                null);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentAccessAuthorizer access = Mockito.mock(DocumentAccessAuthorizer.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(access.canRead(document, actor)).thenReturn(true);
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of());
        DocumentService service = new DocumentService(documents, versions, access, audit);

        DocumentResponse response = service.get(documentId, actor);

        assertThat(response.id()).isEqualTo(documentId);
        verify(audit).record(
                eq("document.metadata-read"),
                eq("document"),
                eq(documentId.toString()),
                eq(AuditClassification.PROTECTED_PERSONAL),
                eq(actor.userId()),
                isNull(),
                anyMap());
    }

    @Test
    void deniesUnreadableDocumentMetadata() {
        UUID documentId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DocumentRecord document = new DocumentRecord(
                documentId,
                "COURT_ORDER",
                "parcel",
                UUID.randomUUID(),
                "Fictional court order",
                DocumentClassification.LEGAL_EVIDENCE,
                "LEGAL_RECORD",
                "workflow-task-and-authorized-staff",
                null,
                null,
                false,
                UUID.randomUUID(),
                "creator@example.test",
                null);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentAccessAuthorizer access = Mockito.mock(DocumentAccessAuthorizer.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(access.canRead(document, actor)).thenReturn(false);
        DocumentService service = new DocumentService(documents, versions, access, audit);

        assertThatThrownBy(() -> service.get(documentId, actor))
                .isInstanceOf(DocumentAccessDeniedException.class);
        verify(audit).record(
                eq("document.metadata-read-denied"),
                eq("document"),
                eq(documentId.toString()),
                eq(AuditClassification.SECURITY),
                eq(actor.userId()),
                isNull(),
                anyMap());
    }

    @Test
    void deniesVersionAppendWhenActorCannotAppend() {
        UUID documentId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DocumentRecord document = new DocumentRecord(
                documentId,
                "SURVEY_PLAN",
                "parcel",
                UUID.randomUUID(),
                "Fictional survey plan",
                DocumentClassification.LEGAL_EVIDENCE,
                "LEGAL_RECORD",
                "workflow-task-and-authorized-staff",
                null,
                null,
                false,
                UUID.randomUUID(),
                "creator@example.test",
                null);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentAccessAuthorizer access = Mockito.mock(DocumentAccessAuthorizer.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(access.canAppendVersion(document, actor)).thenReturn(false);
        DocumentService service = new DocumentService(documents, versions, access, audit);

        assertThatThrownBy(() -> service.addVersion(documentId, new AddDocumentVersionRequest(
                        "documents/fictional/survey-plan-v2.pdf",
                        "survey-plan-v2.pdf",
                        "application/pdf",
                        192L,
                        "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
                        MalwareScanStatus.PENDING,
                        DigitalSignatureStatus.UNKNOWN), actor))
                .isInstanceOf(DocumentAccessDeniedException.class);
        verify(audit).record(
                eq("document.version-append-denied"),
                eq("document"),
                eq(documentId.toString()),
                eq(AuditClassification.SECURITY),
                eq(actor.userId()),
                isNull(),
                anyMap());
    }
}
