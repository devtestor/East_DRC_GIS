package cd.edrc.landgis.governance;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import cd.edrc.landgis.workflow.WorkflowTaskStatus;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentExportRequestService {
    private final DocumentExportRequestRepository exportRequests;
    private final DocumentRecordRepository documents;
    private final DataGovernanceService dataGovernance;
    private final WorkflowTaskService workflowTasks;
    private final AuditService auditService;

    public DocumentExportRequestService(
            DocumentExportRequestRepository exportRequests,
            DocumentRecordRepository documents,
            DataGovernanceService dataGovernance,
            WorkflowTaskService workflowTasks,
            AuditService auditService) {
        this.exportRequests = exportRequests;
        this.documents = documents;
        this.dataGovernance = dataGovernance;
        this.workflowTasks = workflowTasks;
        this.auditService = auditService;
    }

    @Transactional
    public DocumentExportRequestResponse create(CreateDocumentExportRequest request, AuthenticatedActor actor) {
        DocumentRecord document = documents.findById(request.documentId())
                .orElseThrow(() -> new cd.edrc.landgis.documents.DocumentNotFoundException(request.documentId()));
        PrivacyExportDecision initialDecision = dataGovernance.evaluateDocumentExport(
                request.documentId(),
                request.redactionPlanned(),
                false);
        boolean blockedBeforeReview = initialDecision.blockers().stream()
                .anyMatch(blocker -> !"APPROVAL_REQUIRED".equals(blocker));
        ExportRequestStatus status = blockedBeforeReview
                ? ExportRequestStatus.BLOCKED
                : ExportRequestStatus.PENDING_REVIEW;
        DocumentExportRequest exportRequest = exportRequests.save(new DocumentExportRequest(
                UUID.randomUUID(),
                request.documentId(),
                normalizePurpose(request.purpose()),
                request.redactionPlanned(),
                status,
                initialDecision.redactionRequired(),
                initialDecision.approvalRequired(),
                String.join(",", initialDecision.blockers()),
                null,
                actor.userId(),
                actor.username()));
        if (!blockedBeforeReview) {
            UUID workflowTaskId = workflowTasks.openDocumentExportReviewTask(exportRequest.id(), actor);
            exportRequest.assignWorkflowTask(workflowTaskId);
        }
        auditService.record(
                "governance.document-export-requested",
                "document-export-request",
                exportRequest.id().toString(),
                AuditClassification.SECURITY,
                actor.userId(),
                document.custodianOrganizationId(),
                Map.of(
                        "documentId", document.id().toString(),
                        "status", exportRequest.status().name(),
                        "redactionPlanned", exportRequest.redactionPlanned(),
                        "redactionRequired", exportRequest.redactionRequired(),
                        "approvalRequired", exportRequest.approvalRequired(),
                        "blockers", exportRequest.blockerSummary()));
        return DocumentExportRequestResponse.from(exportRequest);
    }

    @Transactional(readOnly = true)
    public DocumentExportRequestResponse get(UUID requestId) {
        return DocumentExportRequestResponse.from(exportRequest(requestId));
    }

    @Transactional
    public DocumentExportRequestResponse completeReview(
            UUID requestId,
            CompleteDocumentExportReviewRequest request,
            AuthenticatedActor actor) {
        DocumentExportRequest exportRequest = exportRequest(requestId);
        if (exportRequest.status() != ExportRequestStatus.PENDING_REVIEW || exportRequest.workflowTaskId() == null) {
            throw new DocumentExportReviewException("Document export request is not pending review");
        }
        WorkflowTask task = workflowTasks.getTask(exportRequest.workflowTaskId());
        if (!"DOCUMENT_EXPORT_REVIEW".equals(task.workflowType())
                || !"document-export-request".equals(task.targetType())
                || !exportRequest.id().equals(task.targetId())) {
            throw new DocumentExportReviewException("Workflow task does not match export request");
        }
        if (request.decision() == WorkflowDecision.APPROVE && task.status() != WorkflowTaskStatus.APPROVED) {
            throw new DocumentExportReviewException("Workflow task must be approved before export approval is recorded");
        }
        if (request.decision() == WorkflowDecision.REJECT && task.status() != WorkflowTaskStatus.REJECTED) {
            throw new DocumentExportReviewException("Workflow task must be rejected before export rejection is recorded");
        }
        PrivacyExportDecision finalDecision = dataGovernance.evaluateDocumentExport(
                exportRequest.documentId(),
                exportRequest.redactionPlanned(),
                request.decision() == WorkflowDecision.APPROVE);
        if (request.decision() == WorkflowDecision.APPROVE && !finalDecision.allowed()) {
            throw new DocumentExportReviewException("Document export cannot be approved: "
                    + String.join(",", finalDecision.blockers()));
        }
        if (request.decision() == WorkflowDecision.APPROVE) {
            exportRequest.approve(actor.userId(), actor.username(), request.reason());
        } else {
            exportRequest.reject(actor.userId(), actor.username(), request.reason());
        }
        auditService.record(
                "governance.document-export-reviewed",
                "document-export-request",
                exportRequest.id().toString(),
                AuditClassification.SECURITY,
                actor.userId(),
                null,
                Map.of(
                        "documentId", exportRequest.documentId().toString(),
                        "decision", request.decision().name(),
                        "status", exportRequest.status().name(),
                        "workflowTaskId", exportRequest.workflowTaskId().toString(),
                        "redactionRequired", finalDecision.redactionRequired()));
        return DocumentExportRequestResponse.from(exportRequest);
    }

    private DocumentExportRequest exportRequest(UUID requestId) {
        return exportRequests.findById(requestId)
                .orElseThrow(() -> new DocumentExportRequestNotFoundException(requestId));
    }

    private String normalizePurpose(String purpose) {
        return purpose.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
    }
}
