package cd.edrc.landgis.disputes;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.DocumentResponse;
import cd.edrc.landgis.documents.DocumentService;
import cd.edrc.landgis.parcels.ParcelNotFoundException;
import cd.edrc.landgis.parcels.ParcelRepository;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.UnsupportedWorkflowTaskException;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DisputeCaseService {
    private final ParcelRepository parcels;
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final WorkflowTaskService workflowTasks;
    private final DocumentService documents;

    DisputeCaseService(
            ParcelRepository parcels,
            JdbcTemplate jdbcTemplate,
            AuditService auditService,
            WorkflowTaskService workflowTasks) {
        this(parcels, jdbcTemplate, auditService, workflowTasks, null);
    }

    @Autowired
    public DisputeCaseService(
            ParcelRepository parcels,
            JdbcTemplate jdbcTemplate,
            AuditService auditService,
            WorkflowTaskService workflowTasks,
            DocumentService documents) {
        this.parcels = parcels;
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.workflowTasks = workflowTasks;
        this.documents = documents;
    }

    @Transactional
    public DisputeCaseResponse create(UUID parcelId, CreateDisputeCaseRequest request, AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse response = jdbcTemplate.queryForObject(
                """
                INSERT INTO disputes.cases (parcel_id, case_type, status, summary, source, authority_reference, opened_by_user_id, opened_by)
                VALUES (?, ?, 'OPEN', ?, ?, ?, ?, ?)
                RETURNING id, parcel_id, case_type, status, summary, source, authority_reference,
                          opened_by_user_id, opened_by, opened_at, updated_at
                """,
                mapper(),
                parcelId,
                request.caseType().name(),
                request.summary().trim(),
                request.source().trim(),
                blankToNull(request.authorityReference()),
                actor.userId(),
                actor.username());
        auditService.record(
                "dispute-case.opened",
                "dispute-case",
                response.id().toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of(
                        "parcelId", parcelId.toString(),
                        "caseType", response.caseType(),
                        "legalBoundary", "Operational dispute record; no adjudication or title determination"));
        return response;
    }

    @Transactional(readOnly = true)
    public List<DisputeCaseResponse> listByParcel(UUID parcelId) {
        requireParcel(parcelId);
        return jdbcTemplate.query(
                """
                SELECT id, parcel_id, case_type, status, summary, source, authority_reference,
                       opened_by_user_id, opened_by, opened_at, updated_at
                FROM disputes.cases
                WHERE parcel_id = ?
                ORDER BY opened_at DESC
                """,
                mapper(),
                parcelId);
    }

    @Transactional
    public DisputeHearingResponse scheduleHearing(
            UUID parcelId,
            UUID caseId,
            ScheduleDisputeHearingRequest request,
            AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        requireOneOfStatuses(disputeCase, DisputeCaseStatus.UNDER_REVIEW, DisputeCaseStatus.REOPENED);
        DisputeHearingResponse hearing = jdbcTemplate.queryForObject(
                """
                INSERT INTO disputes.hearings (case_id, scheduled_for, venue, notes, status, scheduled_by_user_id, scheduled_by)
                VALUES (?, ?, ?, ?, 'SCHEDULED', ?, ?)
                RETURNING id, case_id, scheduled_for, venue, notes, status, scheduled_by_user_id, scheduled_by, scheduled_at, held_at
                """,
                hearingMapper(), caseId, request.scheduledFor(), request.venue().trim(),
                blankToNull(request.notes()), actor.userId(), actor.username());
        updateStatus(caseId, DisputeCaseStatus.HEARING, actor);
        auditService.record(
                "dispute-case.hearing-scheduled", "dispute-case", caseId.toString(),
                AuditClassification.LEGAL_EVIDENCE, actor.userId(), null,
                java.util.Map.of("parcelId", parcelId.toString(), "hearingId", hearing.id().toString(), "venue", hearing.venue()));
        return hearing;
    }

    @Transactional(readOnly = true)
    public List<DisputeHearingResponse> listHearings(UUID parcelId, UUID caseId) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        return jdbcTemplate.query(
                """
                SELECT id, case_id, scheduled_for, venue, notes, status, scheduled_by_user_id, scheduled_by, scheduled_at, held_at
                FROM disputes.hearings WHERE case_id = ? ORDER BY scheduled_for ASC
                """,
                hearingMapper(), disputeCase.id());
    }

    @Transactional
    public DisputeDecisionResponse requestDecision(UUID parcelId, UUID caseId, AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        requireOneOfStatuses(disputeCase, DisputeCaseStatus.HEARING, DisputeCaseStatus.UNDER_REVIEW);
        UUID taskId = workflowTasks.openDisputeCaseDecisionTask(caseId, actor);
        auditService.record(
                "dispute-case.decision-requested", "dispute-case", caseId.toString(),
                AuditClassification.LEGAL_EVIDENCE, actor.userId(), null,
                java.util.Map.of("parcelId", parcelId.toString(), "taskId", taskId.toString()));
        return new DisputeDecisionResponse(disputeCase, "RECORD_DISPUTE_DECISION", "OPEN", taskId);
    }

    @Transactional
    public DisputeDecisionResponse decideDecisionTask(
            UUID parcelId, UUID taskId, DecideWorkflowTaskRequest request, AuthenticatedActor actor) {
        WorkflowTask existingTask = workflowTasks.getTask(taskId);
        requireTask(existingTask, "DISPUTE_CASE_DECISION", "RECORD_DISPUTE_DECISION");
        DisputeCaseResponse current = getForParcel(parcelId, existingTask.targetId());
        requireOneOfStatuses(current, DisputeCaseStatus.HEARING, DisputeCaseStatus.UNDER_REVIEW);
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        DisputeCaseResponse updated = request.decision() == WorkflowDecision.APPROVE
                ? recordDecision(existingTask.targetId(), request.reason(), actor)
                : current;
        auditService.record(
                request.decision() == WorkflowDecision.APPROVE ? "dispute-case.decision-recorded" : "dispute-case.decision-rejected",
                "dispute-case", existingTask.targetId().toString(), AuditClassification.LEGAL_EVIDENCE,
                actor.userId(), null,
                java.util.Map.of("parcelId", parcelId.toString(), "taskId", taskId.toString(), "decision", request.decision().name()));
        return new DisputeDecisionResponse(updated, task.requestedAction(), task.status().name(), task.id());
    }

    @Transactional
    public DisputeDecisionResponse requestReopen(UUID parcelId, UUID caseId, AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        requireOneOfStatuses(disputeCase, DisputeCaseStatus.DECIDED, DisputeCaseStatus.RESOLVED, DisputeCaseStatus.CLOSED);
        UUID taskId = workflowTasks.openDisputeCaseReopenTask(caseId, actor);
        auditService.record(
                "dispute-case.reopen-requested", "dispute-case", caseId.toString(),
                AuditClassification.LEGAL_EVIDENCE, actor.userId(), null,
                java.util.Map.of("parcelId", parcelId.toString(), "taskId", taskId.toString()));
        return new DisputeDecisionResponse(disputeCase, "REOPEN_DISPUTE_CASE", "OPEN", taskId);
    }

    @Transactional
    public DisputeDecisionResponse decideReopenTask(
            UUID parcelId, UUID taskId, DecideWorkflowTaskRequest request, AuthenticatedActor actor) {
        WorkflowTask existingTask = workflowTasks.getTask(taskId);
        requireTask(existingTask, "DISPUTE_CASE_REOPEN", "REOPEN_DISPUTE_CASE");
        DisputeCaseResponse current = getForParcel(parcelId, existingTask.targetId());
        requireOneOfStatuses(current, DisputeCaseStatus.DECIDED, DisputeCaseStatus.RESOLVED, DisputeCaseStatus.CLOSED);
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        DisputeCaseResponse updated = request.decision() == WorkflowDecision.APPROVE
                ? updateStatus(existingTask.targetId(), DisputeCaseStatus.REOPENED, actor)
                : current;
        auditService.record(
                request.decision() == WorkflowDecision.APPROVE ? "dispute-case.reopened" : "dispute-case.reopen-rejected",
                "dispute-case", existingTask.targetId().toString(), AuditClassification.LEGAL_EVIDENCE,
                actor.userId(), null,
                java.util.Map.of("parcelId", parcelId.toString(), "taskId", taskId.toString(), "decision", request.decision().name()));
        return new DisputeDecisionResponse(updated, task.requestedAction(), task.status().name(), task.id());
    }

    @Transactional
    public DisputeAppealResponse createAppeal(
            UUID parcelId, UUID caseId, CreateDisputeAppealRequest request, AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        requireOneOfStatuses(disputeCase, DisputeCaseStatus.DECIDED, DisputeCaseStatus.RESOLVED, DisputeCaseStatus.CLOSED);
        DisputeAppealResponse appeal = jdbcTemplate.queryForObject(
                """
                INSERT INTO disputes.appeals (case_id, appeal_type, grounds, status, filed_by_user_id, filed_by)
                VALUES (?, ?, ?, 'OPEN', ?, ?)
                RETURNING id, case_id, appeal_type, grounds, status, filed_by_user_id, filed_by, filed_at
                """,
                appealMapper(), caseId, request.appealType().name(), request.grounds().trim(), actor.userId(), actor.username());
        auditService.record(
                "dispute-case.appeal-opened", "dispute-case", caseId.toString(), AuditClassification.LEGAL_EVIDENCE,
                actor.userId(), null,
                java.util.Map.of("parcelId", parcelId.toString(), "appealId", appeal.id().toString(), "appealType", appeal.appealType()));
        return appeal;
    }

    @Transactional(readOnly = true)
    public List<DisputeAppealResponse> listAppeals(UUID parcelId, UUID caseId) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        return jdbcTemplate.query(
                "SELECT id, case_id, appeal_type, grounds, status, filed_by_user_id, filed_by, filed_at FROM disputes.appeals WHERE case_id = ? ORDER BY filed_at DESC",
                appealMapper(), disputeCase.id());
    }

    @Transactional
    public DisputeDocumentResponse linkDocument(
            UUID parcelId,
            UUID caseId,
            LinkDisputeDocumentRequest request,
            AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        DocumentResponse document = documents.get(request.documentId(), actor);
        DisputeDocumentResponse response = jdbcTemplate.queryForObject(
                """
                INSERT INTO disputes.case_documents (case_id, document_id, relationship, summary, linked_by_user_id, linked_by)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id, case_id, document_id, relationship, summary, linked_by_user_id, linked_by, linked_at
                """,
                documentMapper(),
                disputeCase.id(),
                document.id(),
                request.relationship().name(),
                request.summary().trim(),
                actor.userId(),
                actor.username());
        auditService.record(
                "dispute-case.document-linked",
                "dispute-case",
                caseId.toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of(
                        "parcelId", parcelId.toString(),
                        "documentId", document.id().toString(),
                        "relationship", response.relationship()));
        return response;
    }

    @Transactional(readOnly = true)
    public List<DisputeDocumentResponse> listDocuments(UUID parcelId, UUID caseId, AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        List<DisputeDocumentResponse> links = jdbcTemplate.query(
                """
                SELECT id, case_id, document_id, relationship, summary, linked_by_user_id, linked_by, linked_at
                FROM disputes.case_documents
                WHERE case_id = ?
                ORDER BY linked_at ASC
                """,
                documentMapper(),
                disputeCase.id());
        links.forEach(link -> documents.get(link.documentId(), actor));
        return links;
    }

    @Transactional
    public DisputeCaseReviewResponse requestReview(UUID parcelId, UUID caseId, AuthenticatedActor actor) {
        requireParcel(parcelId);
        DisputeCaseResponse disputeCase = getForParcel(parcelId, caseId);
        requireStatus(disputeCase, DisputeCaseStatus.OPEN);
        UUID taskId = workflowTasks.openDisputeCaseReviewTask(caseId, actor);
        auditService.record(
                "dispute-case.review-requested",
                "dispute-case",
                caseId.toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of("parcelId", parcelId.toString(), "taskId", taskId.toString()));
        return new DisputeCaseReviewResponse(disputeCase, "START_DISPUTE_REVIEW", "OPEN", taskId);
    }

    @Transactional
    public DisputeCaseReviewResponse decideReviewTask(
            UUID parcelId,
            UUID taskId,
            DecideWorkflowTaskRequest request,
            AuthenticatedActor actor) {
        WorkflowTask existingTask = workflowTasks.getTask(taskId);
        if (!"DISPUTE_CASE_REVIEW".equals(existingTask.workflowType())
                || !"dispute-case".equals(existingTask.targetType())
                || !"START_DISPUTE_REVIEW".equals(existingTask.requestedAction())) {
            throw new UnsupportedWorkflowTaskException(taskId);
        }
        DisputeCaseResponse current = getForParcel(parcelId, existingTask.targetId());
        requireStatus(current, DisputeCaseStatus.OPEN);
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        DisputeCaseResponse updated = request.decision() == WorkflowDecision.APPROVE
                ? updateStatus(existingTask.targetId(), DisputeCaseStatus.UNDER_REVIEW, actor)
                : current;
        auditService.record(
                request.decision() == WorkflowDecision.APPROVE
                        ? "dispute-case.review-approved"
                        : "dispute-case.review-rejected",
                "dispute-case",
                existingTask.targetId().toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of("parcelId", parcelId.toString(), "taskId", taskId.toString(), "decision", request.decision().name()));
        return new DisputeCaseReviewResponse(updated, task.requestedAction(), task.status().name(), task.id());
    }

    private DisputeCaseResponse getForParcel(UUID parcelId, UUID caseId) {
        List<DisputeCaseResponse> matches = jdbcTemplate.query(
                """
                SELECT id, parcel_id, case_type, status, summary, source, authority_reference,
                       opened_by_user_id, opened_by, opened_at, updated_at
                FROM disputes.cases
                WHERE parcel_id = ? AND id = ?
                """,
                mapper(),
                parcelId,
                caseId);
        if (matches.isEmpty()) {
            throw new DisputeCaseNotFoundException(caseId);
        }
        return matches.getFirst();
    }

    private DisputeCaseResponse updateStatus(UUID caseId, DisputeCaseStatus status, AuthenticatedActor actor) {
        return jdbcTemplate.queryForObject(
                """
                UPDATE disputes.cases SET status = ?, updated_at = now(), version = version + 1
                WHERE id = ?
                RETURNING id, parcel_id, case_type, status, summary, source, authority_reference,
                          opened_by_user_id, opened_by, opened_at, updated_at
                """,
                mapper(),
                status.name(),
                caseId);
    }

    private void requireStatus(DisputeCaseResponse disputeCase, DisputeCaseStatus expected) {
        if (!expected.name().equals(disputeCase.status())) {
            throw new IllegalStateException("Dispute case must be " + expected.name() + "");
        }
    }

    private void requireOneOfStatuses(DisputeCaseResponse disputeCase, DisputeCaseStatus... expected) {
        for (DisputeCaseStatus status : expected) {
            if (status.name().equals(disputeCase.status())) {
                return;
            }
        }
        throw new IllegalStateException("Dispute case is not in an allowed lifecycle state: " + disputeCase.status());
    }

    private void requireTask(WorkflowTask task, String workflowType, String action) {
        if (!workflowType.equals(task.workflowType()) || !"dispute-case".equals(task.targetType()) || !action.equals(task.requestedAction())) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
    }

    private DisputeCaseResponse recordDecision(UUID caseId, String summary, AuthenticatedActor actor) {
        return jdbcTemplate.queryForObject(
                """
                UPDATE disputes.cases
                SET status = 'DECIDED', decision_summary = ?, decided_by_user_id = ?, decided_by = ?, decided_at = now(), updated_at = now(), version = version + 1
                WHERE id = ?
                RETURNING id, parcel_id, case_type, status, summary, source, authority_reference, opened_by_user_id, opened_by, opened_at, updated_at
                """,
                mapper(), summary, actor.userId(), actor.username(), caseId);
    }

    private void requireParcel(UUID parcelId) {
        if (!parcels.existsById(parcelId)) {
            throw new ParcelNotFoundException(parcelId);
        }
    }

    private RowMapper<DisputeCaseResponse> mapper() {
        return DisputeCaseService::map;
    }

    private RowMapper<DisputeDocumentResponse> documentMapper() {
        return (resultSet, rowNum) -> new DisputeDocumentResponse(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("case_id", UUID.class),
                resultSet.getObject("document_id", UUID.class),
                resultSet.getString("relationship"),
                resultSet.getString("summary"),
                resultSet.getObject("linked_by_user_id", UUID.class),
                resultSet.getString("linked_by"),
                resultSet.getObject("linked_at", java.time.OffsetDateTime.class));
    }

    private RowMapper<DisputeHearingResponse> hearingMapper() {
        return (resultSet, rowNum) -> new DisputeHearingResponse(
                resultSet.getObject("id", UUID.class), resultSet.getObject("case_id", UUID.class),
                resultSet.getObject("scheduled_for", java.time.OffsetDateTime.class), resultSet.getString("venue"),
                resultSet.getString("notes"), resultSet.getString("status"),
                resultSet.getObject("scheduled_by_user_id", UUID.class), resultSet.getString("scheduled_by"),
                resultSet.getObject("scheduled_at", java.time.OffsetDateTime.class),
                resultSet.getObject("held_at", java.time.OffsetDateTime.class));
    }

    private RowMapper<DisputeAppealResponse> appealMapper() {
        return (resultSet, rowNum) -> new DisputeAppealResponse(
                resultSet.getObject("id", UUID.class), resultSet.getObject("case_id", UUID.class),
                resultSet.getString("appeal_type"), resultSet.getString("grounds"), resultSet.getString("status"),
                resultSet.getObject("filed_by_user_id", UUID.class), resultSet.getString("filed_by"),
                resultSet.getObject("filed_at", java.time.OffsetDateTime.class));
    }

    private static DisputeCaseResponse map(ResultSet resultSet, int rowNum) throws SQLException {
        return new DisputeCaseResponse(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("parcel_id", UUID.class),
                resultSet.getString("case_type"),
                resultSet.getString("status"),
                resultSet.getString("summary"),
                resultSet.getString("source"),
                resultSet.getString("authority_reference"),
                resultSet.getObject("opened_by_user_id", UUID.class),
                resultSet.getString("opened_by"),
                resultSet.getObject("opened_at", java.time.OffsetDateTime.class),
                resultSet.getObject("updated_at", java.time.OffsetDateTime.class));
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
