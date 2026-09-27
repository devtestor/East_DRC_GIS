package cd.edrc.landgis.governance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import cd.edrc.landgis.documents.DocumentContentStore;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import cd.edrc.landgis.documents.DocumentVersionRecord;
import cd.edrc.landgis.documents.DocumentVersionRecordRepository;
import cd.edrc.landgis.documents.MalwareScanStatus;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DocumentExportPackageServiceTest {
    private static final AuthenticatedActor SECURITY_OFFICER = new AuthenticatedActor(
            UUID.fromString("20000000-0000-0000-0000-000000000010"),
            "security-officer@example.test");

    @Test
    void generatesPackageOnlyForApprovedExportRequestAndReturnsTokenOnce() {
        UUID requestId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        DocumentExportRequest exportRequest = approvedRequest(requestId, documentId);
        DocumentRecord document = document(documentId, DocumentClassification.PROTECTED_PERSONAL);
        DocumentVersionRecord version = version(documentId);
        DocumentExportRequestRepository requests = Mockito.mock(DocumentExportRequestRepository.class);
        DocumentExportPackageRepository packages = Mockito.mock(DocumentExportPackageRepository.class);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentContentStore contentStore = Mockito.mock(DocumentContentStore.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(requests.findById(requestId)).thenReturn(Optional.of(exportRequest));
        when(packages.existsByExportRequestId(requestId)).thenReturn(false);
        when(packages.save(any(DocumentExportPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of(version));
        when(contentStore.read(document, version)).thenReturn("redacted source content".getBytes(StandardCharsets.UTF_8));
        DocumentExportPackageService service = new DocumentExportPackageService(
                requests, packages, documents, versions, contentStore, audit);

        DocumentExportPackageResponse response = service.generate(
                requestId,
                new CreateDocumentExportPackageRequest(30),
                SECURITY_OFFICER);

        assertThat(response.exportRequestId()).isEqualTo(requestId);
        assertThat(response.documentId()).isEqualTo(documentId);
        assertThat(response.documentVersionId()).isEqualTo(version.id());
        assertThat(response.packageSha256()).matches("[a-f0-9]{64}");
        assertThat(response.manifestSha256()).matches("[a-f0-9]{64}");
        assertThat(response.deliveryToken()).isNotBlank();
        assertThat(response.packageSizeBytes()).isPositive();
        verify(audit).record(
                eq("governance.document-export-package-generated"),
                eq("document-export-package"),
                eq(response.id().toString()),
                eq(AuditClassification.SECURITY),
                eq(SECURITY_OFFICER.userId()),
                eq(document.custodianOrganizationId()),
                anyMap());
    }

    @Test
    void rejectsPackageGenerationForRequestThatIsNotApproved() {
        UUID requestId = UUID.randomUUID();
        DocumentExportRequest pendingRequest = new DocumentExportRequest(
                requestId,
                UUID.randomUUID(),
                "PROVINCIAL AUDIT",
                true,
                ExportRequestStatus.PENDING_REVIEW,
                true,
                true,
                "APPROVAL_REQUIRED",
                UUID.randomUUID(),
                SECURITY_OFFICER.userId(),
                SECURITY_OFFICER.username());
        DocumentExportRequestRepository requests = Mockito.mock(DocumentExportRequestRepository.class);
        when(requests.findById(requestId)).thenReturn(Optional.of(pendingRequest));
        DocumentExportPackageService service = new DocumentExportPackageService(
                requests,
                Mockito.mock(DocumentExportPackageRepository.class),
                Mockito.mock(DocumentRecordRepository.class),
                Mockito.mock(DocumentVersionRecordRepository.class),
                Mockito.mock(DocumentContentStore.class),
                Mockito.mock(AuditService.class));

        assertThatThrownBy(() -> service.generate(
                requestId,
                new CreateDocumentExportPackageRequest(30),
                SECURITY_OFFICER))
                .isInstanceOf(DocumentExportPackageException.class)
                .hasMessageContaining("approved request");
    }

    @Test
    void downloadsPackageWithUnexpiredTokenAndVerifiesChecksum() {
        UUID requestId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        DocumentExportRequest exportRequest = approvedRequest(requestId, documentId);
        DocumentRecord document = document(documentId, DocumentClassification.LEGAL_EVIDENCE);
        DocumentVersionRecord version = version(documentId);
        DocumentExportRequestRepository requests = Mockito.mock(DocumentExportRequestRepository.class);
        DocumentExportPackageRepository packages = Mockito.mock(DocumentExportPackageRepository.class);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DocumentVersionRecordRepository versions = Mockito.mock(DocumentVersionRecordRepository.class);
        DocumentContentStore contentStore = Mockito.mock(DocumentContentStore.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(requests.findById(requestId)).thenReturn(Optional.of(exportRequest));
        when(packages.existsByExportRequestId(requestId)).thenReturn(false);
        when(packages.save(any(DocumentExportPackage.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(versions.findByDocumentIdOrderByVersionNumberAsc(documentId)).thenReturn(List.of(version));
        when(versions.findById(version.id())).thenReturn(Optional.of(version));
        when(contentStore.read(document, version)).thenReturn("redacted source content".getBytes(StandardCharsets.UTF_8));
        DocumentExportPackageService service = new DocumentExportPackageService(
                requests, packages, documents, versions, contentStore, audit);
        DocumentExportPackageResponse generated = service.generate(
                requestId,
                new CreateDocumentExportPackageRequest(30),
                SECURITY_OFFICER);
        DocumentExportPackage capturedPackage = capturedPackage(packages);
        when(packages.findById(generated.id())).thenReturn(Optional.of(capturedPackage));

        DocumentExportPackageDownload download = service.download(
                generated.id(),
                generated.deliveryToken(),
                SECURITY_OFFICER);

        assertThat(download.filename()).contains(generated.id().toString());
        assertThat(download.mediaType()).isEqualTo("text/plain");
        assertThat(new String(download.content(), StandardCharsets.UTF_8))
                .contains("Sandbox governed export")
                .contains("redacted source content");
        verify(audit).record(
                eq("governance.document-export-package-downloaded"),
                eq("document-export-package"),
                eq(generated.id().toString()),
                eq(AuditClassification.SECURITY),
                eq(SECURITY_OFFICER.userId()),
                eq(document.custodianOrganizationId()),
                anyMap());
    }

    @Test
    void rejectsDownloadWithInvalidToken() {
        UUID requestId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        DocumentExportPackage exportPackage = new DocumentExportPackage(
                UUID.randomUUID(),
                requestId,
                documentId,
                UUID.randomUUID(),
                "sandbox/governed-exports/" + requestId + ".txt",
                "a".repeat(64),
                "b".repeat(64),
                100,
                "c".repeat(64),
                OffsetDateTime.now().plusMinutes(30),
                SECURITY_OFFICER.userId(),
                SECURITY_OFFICER.username());
        DocumentExportPackageRepository packages = Mockito.mock(DocumentExportPackageRepository.class);
        when(packages.findById(exportPackage.id())).thenReturn(Optional.of(exportPackage));
        DocumentExportPackageService service = new DocumentExportPackageService(
                Mockito.mock(DocumentExportRequestRepository.class),
                packages,
                Mockito.mock(DocumentRecordRepository.class),
                Mockito.mock(DocumentVersionRecordRepository.class),
                Mockito.mock(DocumentContentStore.class),
                Mockito.mock(AuditService.class));

        assertThatThrownBy(() -> service.download(exportPackage.id(), "wrong-token", SECURITY_OFFICER))
                .isInstanceOf(DocumentExportPackageException.class)
                .hasMessageContaining("Invalid");
    }

    private DocumentExportPackage capturedPackage(DocumentExportPackageRepository packages) {
        org.mockito.ArgumentCaptor<DocumentExportPackage> captor = org.mockito.ArgumentCaptor.forClass(DocumentExportPackage.class);
        verify(packages).save(captor.capture());
        return captor.getValue();
    }

    private DocumentExportRequest approvedRequest(UUID requestId, UUID documentId) {
        DocumentExportRequest exportRequest = new DocumentExportRequest(
                requestId,
                documentId,
                "PROVINCIAL AUDIT",
                true,
                ExportRequestStatus.PENDING_REVIEW,
                true,
                true,
                "APPROVAL_REQUIRED",
                UUID.randomUUID(),
                SECURITY_OFFICER.userId(),
                SECURITY_OFFICER.username());
        exportRequest.approve(SECURITY_OFFICER.userId(), SECURITY_OFFICER.username(), "Approved redacted export");
        return exportRequest;
    }

    private DocumentRecord document(UUID documentId, DocumentClassification classification) {
        return new DocumentRecord(
                documentId,
                "FICTIONAL_DOCUMENT",
                "parcel",
                UUID.randomUUID(),
                "Fictional export source document",
                classification,
                "LEGAL_RECORD",
                "restricted-staff",
                UUID.randomUUID(),
                "SECURITY_OFFICER",
                false,
                SECURITY_OFFICER.userId(),
                SECURITY_OFFICER.username(),
                null);
    }

    private DocumentVersionRecord version(UUID documentId) {
        return new DocumentVersionRecord(
                UUID.randomUUID(),
                documentId,
                1,
                "sandbox/documents/source.txt",
                "source.txt",
                "text/plain",
                32,
                "d".repeat(64),
                MalwareScanStatus.PASSED,
                DigitalSignatureStatus.UNSIGNED,
                SECURITY_OFFICER.userId(),
                SECURITY_OFFICER.username());
    }
}
