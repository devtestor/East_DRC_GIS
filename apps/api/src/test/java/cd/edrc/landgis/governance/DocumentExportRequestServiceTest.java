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
import cd.edrc.landgis.documents.DocumentClassification;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DocumentExportRequestServiceTest {
    private static final AuthenticatedActor REQUESTER = new AuthenticatedActor(
            UUID.fromString("20000000-0000-0000-0000-000000000001"),
            "requester@example.test");
    private static final AuthenticatedActor REVIEWER = new AuthenticatedActor(
            UUID.fromString("20000000-0000-0000-0000-000000000002"),
            "security@example.test");

    @Test
    void blocksSensitiveExportWhenRedactionIsNotPlanned() {
        UUID documentId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.PROTECTED_PERSONAL);
        DocumentExportRequestRepository requests = Mockito.mock(DocumentExportRequestRepository.class);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DataGovernanceService governance = Mockito.mock(DataGovernanceService.class);
        WorkflowTaskService workflow = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(governance.evaluateDocumentExport(documentId, false, false)).thenReturn(new PrivacyExportDecision(
                false,
                true,
                true,
                List.of("REDACTION_REQUIRED", "APPROVAL_REQUIRED")));
        when(requests.save(any(DocumentExportRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        DocumentExportRequestService service = new DocumentExportRequestService(requests, documents, governance, workflow, audit);

        DocumentExportRequestResponse response = service.create(
                new CreateDocumentExportRequest(documentId, "Court-approved case bundle", false),
                REQUESTER);

        assertThat(response.status()).isEqualTo(ExportRequestStatus.BLOCKED.name());
        assertThat(response.workflowTaskId()).isNull();
        assertThat(response.blockers()).contains("REDACTION_REQUIRED", "APPROVAL_REQUIRED");
        verify(audit).record(
                eq("governance.document-export-requested"),
                eq("document-export-request"),
                eq(response.id().toString()),
                eq(AuditClassification.SECURITY),
                eq(REQUESTER.userId()),
                eq(document.custodianOrganizationId()),
                anyMap());
    }

    @Test
    void opensSecurityWorkflowWhenSensitiveExportHasRedactionPlan() {
        UUID documentId = UUID.randomUUID();
        UUID workflowTaskId = UUID.randomUUID();
        DocumentRecord document = document(documentId, DocumentClassification.LEGAL_EVIDENCE);
        DocumentExportRequestRepository requests = Mockito.mock(DocumentExportRequestRepository.class);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DataGovernanceService governance = Mockito.mock(DataGovernanceService.class);
        WorkflowTaskService workflow = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document));
        when(governance.evaluateDocumentExport(documentId, true, false)).thenReturn(new PrivacyExportDecision(
                false,
                true,
                true,
                List.of("APPROVAL_REQUIRED")));
        when(requests.save(any(DocumentExportRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workflow.openDocumentExportReviewTask(any(UUID.class), eq(REQUESTER))).thenReturn(workflowTaskId);
        DocumentExportRequestService service = new DocumentExportRequestService(requests, documents, governance, workflow, audit);

        DocumentExportRequestResponse response = service.create(
                new CreateDocumentExportRequest(documentId, "Provincial audit extract", true),
                REQUESTER);

        assertThat(response.status()).isEqualTo(ExportRequestStatus.PENDING_REVIEW.name());
        assertThat(response.workflowTaskId()).isEqualTo(workflowTaskId);
        assertThat(response.redactionRequired()).isTrue();
        assertThat(response.approvalRequired()).isTrue();
    }

    @Test
    void recordsApprovalOnlyAfterWorkflowTaskIsApprovedAndPolicyAllowsExport() {
        UUID documentId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        UUID workflowTaskId = UUID.randomUUID();
        DocumentExportRequest exportRequest = new DocumentExportRequest(
                requestId,
                documentId,
                "PROVINCIAL AUDIT EXTRACT",
                true,
                ExportRequestStatus.PENDING_REVIEW,
                true,
                true,
                "APPROVAL_REQUIRED",
                workflowTaskId,
                REQUESTER.userId(),
                REQUESTER.username());
        WorkflowTask approvedTask = new WorkflowTask(
                workflowTaskId,
                "DOCUMENT_EXPORT_REVIEW",
                "document-export-request",
                requestId,
                "APPROVE_DOCUMENT_EXPORT",
                REQUESTER.userId(),
                REQUESTER.username(),
                "SECURITY_OFFICER");
        approvedTask.claim(REVIEWER.userId(), REVIEWER.username());
        approvedTask.approve("Redaction plan accepted", REVIEWER.userId(), REVIEWER.username());
        DocumentExportRequestRepository requests = Mockito.mock(DocumentExportRequestRepository.class);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DataGovernanceService governance = Mockito.mock(DataGovernanceService.class);
        WorkflowTaskService workflow = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(requests.findById(requestId)).thenReturn(Optional.of(exportRequest));
        when(workflow.getTask(workflowTaskId)).thenReturn(approvedTask);
        when(governance.evaluateDocumentExport(documentId, true, true)).thenReturn(new PrivacyExportDecision(
                true,
                true,
                true,
                List.of()));
        DocumentExportRequestService service = new DocumentExportRequestService(requests, documents, governance, workflow, audit);

        DocumentExportRequestResponse response = service.completeReview(
                requestId,
                new CompleteDocumentExportReviewRequest(WorkflowDecision.APPROVE, "Security officer approved redacted export"),
                REVIEWER);

        assertThat(response.status()).isEqualTo(ExportRequestStatus.APPROVED.name());
        assertThat(response.decidedByUserId()).isEqualTo(REVIEWER.userId());
        assertThat(response.decidedAt()).isNotNull();
        verify(audit).record(
                eq("governance.document-export-reviewed"),
                eq("document-export-request"),
                eq(requestId.toString()),
                eq(AuditClassification.SECURITY),
                eq(REVIEWER.userId()),
                eq(null),
                anyMap());
    }

    @Test
    void rejectsApprovalWhenWorkflowTaskIsNotApproved() {
        UUID requestId = UUID.randomUUID();
        UUID workflowTaskId = UUID.randomUUID();
        DocumentExportRequest exportRequest = new DocumentExportRequest(
                requestId,
                UUID.randomUUID(),
                "PROVINCIAL AUDIT EXTRACT",
                true,
                ExportRequestStatus.PENDING_REVIEW,
                true,
                true,
                "APPROVAL_REQUIRED",
                workflowTaskId,
                REQUESTER.userId(),
                REQUESTER.username());
        WorkflowTask openTask = new WorkflowTask(
                workflowTaskId,
                "DOCUMENT_EXPORT_REVIEW",
                "document-export-request",
                requestId,
                "APPROVE_DOCUMENT_EXPORT",
                REQUESTER.userId(),
                REQUESTER.username(),
                "SECURITY_OFFICER");
        DocumentExportRequestRepository requests = Mockito.mock(DocumentExportRequestRepository.class);
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        DataGovernanceService governance = Mockito.mock(DataGovernanceService.class);
        WorkflowTaskService workflow = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(requests.findById(requestId)).thenReturn(Optional.of(exportRequest));
        when(workflow.getTask(workflowTaskId)).thenReturn(openTask);
        DocumentExportRequestService service = new DocumentExportRequestService(requests, documents, governance, workflow, audit);

        assertThatThrownBy(() -> service.completeReview(
                requestId,
                new CompleteDocumentExportReviewRequest(WorkflowDecision.APPROVE, "Premature approval"),
                REVIEWER))
                .isInstanceOf(DocumentExportReviewException.class);
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
                REQUESTER.userId(),
                REQUESTER.username(),
                null);
    }
}
