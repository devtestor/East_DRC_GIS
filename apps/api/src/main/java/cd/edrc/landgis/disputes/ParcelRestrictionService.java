package cd.edrc.landgis.disputes;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelRestrictionService {
    private final ParcelRepository parcels;
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final WorkflowTaskService workflowTasks;

    ParcelRestrictionService(
            ParcelRepository parcels,
            JdbcTemplate jdbcTemplate,
            AuditService auditService) {
        this(parcels, jdbcTemplate, auditService, null);
    }

    @Autowired
    ParcelRestrictionService(
            ParcelRepository parcels,
            JdbcTemplate jdbcTemplate,
            AuditService auditService,
            WorkflowTaskService workflowTasks) {
        this.parcels = parcels;
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.workflowTasks = workflowTasks;
    }

    @Transactional
    public ParcelRestrictionResponse create(
            UUID parcelId,
            CreateParcelRestrictionRequest request,
            AuthenticatedActor actor) {
        requireParcel(parcelId);
        ParcelRestrictionResponse response = jdbcTemplate.queryForObject(
                """
                INSERT INTO disputes.parcel_restrictions (
                    parcel_id,
                    restriction_type,
                    status,
                    source,
                    authority_reference,
                    summary,
                    blocks_ownership_changes,
                    effective_to,
                    created_by_user_id,
                    created_by
                )
                VALUES (?, ?, 'ACTIVE', ?, ?, ?, ?, ?, ?, ?)
                RETURNING
                    id,
                    parcel_id,
                    restriction_type,
                    status,
                    source,
                    authority_reference,
                    summary,
                    blocks_ownership_changes,
                    effective_from,
                    effective_to,
                    created_by_user_id,
                    created_by,
                    created_at
                """,
                mapper(),
                parcelId,
                request.restrictionType().name(),
                request.source().trim(),
                blankToNull(request.authorityReference()),
                request.summary().trim(),
                request.blocksOwnershipChanges(),
                request.effectiveTo(),
                actor.userId(),
                actor.username());
        auditService.record(
                "parcel-restriction.applied",
                "parcel",
                parcelId.toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of(
                        "restrictionId", response.id().toString(),
                        "restrictionType", response.restrictionType(),
                        "blocksOwnershipChanges", response.blocksOwnershipChanges()));
        return response;
    }

    @Transactional(readOnly = true)
    public List<ParcelRestrictionResponse> listByParcel(UUID parcelId) {
        requireParcel(parcelId);
        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    parcel_id,
                    restriction_type,
                    status,
                    source,
                    authority_reference,
                    summary,
                    blocks_ownership_changes,
                    effective_from,
                    effective_to,
                    created_by_user_id,
                    created_by,
                    created_at
                FROM disputes.parcel_restrictions
                WHERE parcel_id = ?
                ORDER BY created_at DESC
                """,
                mapper(),
                parcelId);
    }

    @Transactional
    public ParcelRestrictionReleaseResponse requestRelease(
            UUID parcelId,
            UUID restrictionId,
            AuthenticatedActor actor) {
        requireParcel(parcelId);
        ParcelRestrictionResponse restriction = getForParcel(parcelId, restrictionId);
        requireActive(restriction);
        UUID taskId = workflowTasks.openParcelRestrictionReleaseTask(restrictionId, actor);
        auditService.record(
                "parcel-restriction.release-requested",
                "parcel-restriction",
                restrictionId.toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of(
                        "parcelId", parcelId.toString(),
                        "restrictionType", restriction.restrictionType(),
                        "taskId", taskId.toString(),
                        "legalBoundary", "Operational restriction workflow; not a court decision"));
        return new ParcelRestrictionReleaseResponse(restriction, "RELEASE_RESTRICTION", "OPEN", taskId);
    }

    @Transactional
    public ParcelRestrictionReleaseResponse decideReleaseTask(
            UUID parcelId,
            UUID taskId,
            DecideWorkflowTaskRequest request,
            AuthenticatedActor actor) {
        WorkflowTask existingTask = workflowTasks.getTask(taskId);
        requireSupportedReleaseTask(existingTask);
        ParcelRestrictionResponse current = getForParcel(parcelId, existingTask.targetId());
        requireActive(current);
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        ParcelRestrictionResponse restriction = request.decision() == WorkflowDecision.APPROVE
                ? release(existingTask.targetId(), actor)
                : current;
        auditService.record(
                request.decision() == WorkflowDecision.APPROVE
                        ? "parcel-restriction.release-approved"
                        : "parcel-restriction.release-rejected",
                "parcel-restriction",
                existingTask.targetId().toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of(
                        "parcelId", parcelId.toString(),
                        "taskId", task.id().toString(),
                        "decision", request.decision().name(),
                        "reason", request.reason()));
        return new ParcelRestrictionReleaseResponse(
                restriction,
                task.requestedAction(),
                task.status().name(),
                task.id());
    }

    @Transactional(readOnly = true)
    public void requireNoOwnershipChangeBlock(UUID parcelId) {
        requireParcel(parcelId);
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM disputes.parcel_restrictions
                WHERE parcel_id = ?
                  AND status = 'ACTIVE'
                  AND blocks_ownership_changes = true
                  AND effective_from <= now()
                  AND (effective_to IS NULL OR effective_to > now())
                """,
                Integer.class,
                parcelId);
        if (count != null && count > 0) {
            throw new ActiveParcelRestrictionException(parcelId);
        }
    }

    private void requireParcel(UUID parcelId) {
        if (!parcels.existsById(parcelId)) {
            throw new ParcelNotFoundException(parcelId);
        }
    }

    private ParcelRestrictionResponse getForParcel(UUID parcelId, UUID restrictionId) {
        List<ParcelRestrictionResponse> matches = jdbcTemplate.query(
                """
                SELECT id, parcel_id, restriction_type, status, source, authority_reference, summary,
                       blocks_ownership_changes, effective_from, effective_to, created_by_user_id, created_by, created_at
                FROM disputes.parcel_restrictions
                WHERE parcel_id = ? AND id = ?
                """,
                mapper(),
                parcelId,
                restrictionId);
        if (matches.isEmpty()) {
            throw new ParcelRestrictionNotFoundException(restrictionId);
        }
        return matches.getFirst();
    }

    private void requireActive(ParcelRestrictionResponse restriction) {
        if (!RestrictionStatus.ACTIVE.name().equals(restriction.status())) {
            throw new IllegalStateException("Only an ACTIVE restriction can be released");
        }
    }

    private ParcelRestrictionResponse release(UUID restrictionId, AuthenticatedActor actor) {
        return jdbcTemplate.queryForObject(
                """
                UPDATE disputes.parcel_restrictions
                SET status = 'RELEASED', released_by_user_id = ?, released_by = ?, released_at = now(), version = version + 1
                WHERE id = ? AND status = 'ACTIVE'
                RETURNING id, parcel_id, restriction_type, status, source, authority_reference, summary,
                          blocks_ownership_changes, effective_from, effective_to, created_by_user_id, created_by, created_at
                """,
                mapper(),
                actor.userId(),
                actor.username(),
                restrictionId);
    }

    private void requireSupportedReleaseTask(WorkflowTask task) {
        if (!"PARCEL_RESTRICTION_RELEASE".equals(task.workflowType())
                || !"parcel-restriction".equals(task.targetType())
                || !"RELEASE_RESTRICTION".equals(task.requestedAction())) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
    }

    private RowMapper<ParcelRestrictionResponse> mapper() {
        return ParcelRestrictionService::map;
    }

    private static ParcelRestrictionResponse map(ResultSet resultSet, int rowNum) throws SQLException {
        return new ParcelRestrictionResponse(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("parcel_id", UUID.class),
                resultSet.getString("restriction_type"),
                resultSet.getString("status"),
                resultSet.getString("source"),
                resultSet.getString("authority_reference"),
                resultSet.getString("summary"),
                resultSet.getBoolean("blocks_ownership_changes"),
                resultSet.getObject("effective_from", java.time.OffsetDateTime.class),
                resultSet.getObject("effective_to", java.time.OffsetDateTime.class),
                resultSet.getObject("created_by_user_id", UUID.class),
                resultSet.getString("created_by"),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
