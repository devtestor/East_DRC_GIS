package cd.edrc.landgis.workflow;

import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowTaskService {
    private final WorkflowTaskRepository tasks;
    private final WorkflowTaskEvidenceLinkRepository evidenceLinks;
    private final WorkflowRoleScopeAuthorizer roleScopes;
    private final WorkflowEvidenceValidator evidenceValidator;

    public WorkflowTaskService(
            WorkflowTaskRepository tasks,
            WorkflowTaskEvidenceLinkRepository evidenceLinks,
            WorkflowRoleScopeAuthorizer roleScopes,
            WorkflowEvidenceValidator evidenceValidator) {
        this.tasks = tasks;
        this.evidenceLinks = evidenceLinks;
        this.roleScopes = roleScopes;
        this.evidenceValidator = evidenceValidator;
    }

    @Transactional
    public UUID openParcelTransitionTask(UUID parcelId, String requestedTransition, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                parcelId,
                requestedTransition,
                requestedBy.userId(),
                requestedBy.username(),
                "CADASTRAL_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openRegisteredDeviceLifecycleTask(UUID deviceRecordId, String requestedAction, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "REGISTERED_DEVICE_LIFECYCLE",
                "registered-device",
                deviceRecordId,
                requestedAction,
                requestedBy.userId(),
                requestedBy.username(),
                "SECURITY_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openParcelGeometryApprovalTask(UUID geometryVersionId, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "PARCEL_GEOMETRY_APPROVAL",
                "parcel-geometry-version",
                geometryVersionId,
                "APPROVE_CURRENT_GEOMETRY",
                requestedBy.userId(),
                requestedBy.username(),
                "CADASTRAL_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openOwnershipInterestReviewTask(UUID ownershipInterestId, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "OWNERSHIP_INTEREST_REVIEW",
                "ownership-interest",
                ownershipInterestId,
                "VERIFY_OWNERSHIP_INTEREST",
                requestedBy.userId(),
                requestedBy.username(),
                "LAND_TITLE_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openParcelRestrictionReleaseTask(UUID restrictionId, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "PARCEL_RESTRICTION_RELEASE",
                "parcel-restriction",
                restrictionId,
                "RELEASE_RESTRICTION",
                requestedBy.userId(),
                requestedBy.username(),
                "LAND_TITLE_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openDisputeCaseReviewTask(UUID disputeCaseId, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "DISPUTE_CASE_REVIEW",
                "dispute-case",
                disputeCaseId,
                "START_DISPUTE_REVIEW",
                requestedBy.userId(),
                requestedBy.username(),
                "LAND_TITLE_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openDisputeCaseDecisionTask(UUID disputeCaseId, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "DISPUTE_CASE_DECISION",
                "dispute-case",
                disputeCaseId,
                "RECORD_DISPUTE_DECISION",
                requestedBy.userId(),
                requestedBy.username(),
                "LAND_TITLE_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openDisputeCaseReopenTask(UUID disputeCaseId, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "DISPUTE_CASE_REOPEN",
                "dispute-case",
                disputeCaseId,
                "REOPEN_DISPUTE_CASE",
                requestedBy.userId(),
                requestedBy.username(),
                "LAND_TITLE_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional
    public UUID openParcelInformationReviewTask(UUID applicationId, AuthenticatedActor requestedBy) {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "PARCEL_INFORMATION_REQUEST_REVIEW",
                "parcel-information-application",
                applicationId,
                "APPROVE_PARCEL_INFORMATION_REQUEST",
                requestedBy.userId(),
                requestedBy.username(),
                "LAND_TITLE_OFFICER");
        return tasks.save(task).id();
    }

    @Transactional(readOnly = true)
    public List<WorkflowTaskResponse> listOpenTasks() {
        return tasks.findByStatusOrderByCreatedAtAsc(WorkflowTaskStatus.OPEN).stream()
                .map(WorkflowTaskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkflowTask getTask(UUID taskId) {
        return tasks.findById(taskId).orElseThrow(() -> new WorkflowTaskNotFoundException(taskId));
    }

    @Transactional
    public WorkflowTaskResponse claimTask(UUID taskId, AuthenticatedActor actor) {
        WorkflowTask task = getTask(taskId);
        roleScopes.requireClaimScope(actor, task);
        task.claim(actor.userId(), actor.username());
        return WorkflowTaskResponse.from(task);
    }

    @Transactional
    public WorkflowTask decideTask(UUID taskId, DecideWorkflowTaskRequest request, AuthenticatedActor decidedBy) {
        WorkflowTask task = getTask(taskId);
        if (request.decision() == WorkflowDecision.APPROVE) {
            task.validateApprovalBy(decidedBy.userId(), decidedBy.username());
            requireEvidence(taskId);
            task.approve(request.reason(), decidedBy.userId(), decidedBy.username());
        } else {
            task.reject(request.reason(), decidedBy.userId(), decidedBy.username());
        }
        return task;
    }

    @Transactional
    public WorkflowTaskEvidenceResponse addEvidence(
            UUID taskId,
            AddWorkflowTaskEvidenceRequest request,
            AuthenticatedActor actor) {
        tasks.findById(taskId).orElseThrow(() -> new WorkflowTaskNotFoundException(taskId));
        if (request.referenceId() == null
                && (request.externalReference() == null || request.externalReference().isBlank())) {
            throw new IllegalArgumentException("Evidence reference requires referenceId or externalReference");
        }
        evidenceValidator.validate(request);
        WorkflowTaskEvidenceLink evidence = new WorkflowTaskEvidenceLink(
                UUID.randomUUID(),
                taskId,
                request.evidenceType(),
                request.referenceType(),
                request.referenceId(),
                request.externalReference(),
                request.summary(),
                actor.userId(),
                actor.username());
        return WorkflowTaskEvidenceResponse.from(evidenceLinks.save(evidence));
    }

    @Transactional(readOnly = true)
    public List<WorkflowTaskEvidenceResponse> listEvidence(UUID taskId) {
        tasks.findById(taskId).orElseThrow(() -> new WorkflowTaskNotFoundException(taskId));
        return evidenceLinks.findByTaskIdOrderByAddedAtAsc(taskId).stream()
                .map(WorkflowTaskEvidenceResponse::from)
                .toList();
    }

    private void requireEvidence(UUID taskId) {
        if (evidenceLinks.countByTaskId(taskId) == 0) {
            throw new WorkflowEvidenceRequiredException(taskId);
        }
    }
}
