package cd.edrc.landgis.parcels;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.UnsupportedWorkflowTaskException;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParcelGeometryService {
    private static final String WORKFLOW_TYPE = "PARCEL_GEOMETRY_APPROVAL";
    private static final String TARGET_TYPE = "parcel-geometry-version";
    private static final String REQUESTED_ACTION = "APPROVE_CURRENT_GEOMETRY";

    private final ParcelRepository parcels;
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final WorkflowTaskService workflowTasks;

    ParcelGeometryService(
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
    public ParcelGeometryVersionResponse createDraftGeometry(UUID parcelId, CreateParcelGeometryVersionRequest request) {
        requireParcel(parcelId);
        ParcelGeometryVersionResponse response;
        try {
            response = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO parcels.parcel_geometry_versions (
                        id,
                        parcel_id,
                        geometry,
                        geometry_status,
                        source
                    )
                    VALUES (
                        ?,
                        ?,
                        ST_Multi(ST_GeomFromText(?, 4326))::geometry(MultiPolygon, 4326),
                        'DRAFT',
                        ?
                    )
                    RETURNING
                        id,
                        parcel_id,
                        ST_AsText(geometry) AS geometry_wkt,
                        geometry_status,
                        calculated_area_square_meters,
                        source,
                        effective_from,
                        effective_to,
                        approved_by,
                        approved_at,
                        created_at
                    """,
                    mapper(),
                    UUID.randomUUID(),
                    parcelId,
                    request.geometryWkt(),
                    request.source());
        } catch (DataAccessException exception) {
            throw new InvalidParcelGeometryException(parcelId);
        }
        auditService.record(
                "parcel.geometry-draft-created",
                "parcel",
                parcelId.toString(),
                AuditClassification.STAFF_OPERATIONAL);
        return response;
    }

    @Transactional(readOnly = true)
    List<ParcelGeometryVersionResponse> listGeometryVersions(UUID parcelId) {
        requireParcel(parcelId);
        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    parcel_id,
                    ST_AsText(geometry) AS geometry_wkt,
                    geometry_status,
                    calculated_area_square_meters,
                    source,
                    effective_from,
                    effective_to,
                    approved_by,
                    approved_at,
                    created_at
                FROM parcels.parcel_geometry_versions
                WHERE parcel_id = ?
                ORDER BY created_at DESC
                """,
                mapper(),
                parcelId);
    }

    @Transactional
    ParcelGeometryApprovalResponse requestApproval(UUID parcelId, UUID geometryVersionId, AuthenticatedActor actor) {
        requireParcel(parcelId);
        ParcelGeometryVersionResponse geometry = requireGeometryVersion(geometryVersionId);
        requireGeometryBelongsToParcel(parcelId, geometry);
        requireReviewableGeometry(geometry);
        UUID taskId = workflowTasks.openParcelGeometryApprovalTask(geometryVersionId, actor);
        auditService.record(
                "parcel.geometry-approval-requested",
                "parcel",
                parcelId.toString(),
                AuditClassification.STAFF_OPERATIONAL);
        return new ParcelGeometryApprovalResponse(geometry, REQUESTED_ACTION, "OPEN", taskId);
    }

    @Transactional
    ParcelGeometryApprovalResponse decideApprovalTask(
            UUID taskId,
            DecideWorkflowTaskRequest request,
            AuthenticatedActor actor) {
        requireSupportedTask(workflowTasks.getTask(taskId));
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        ParcelGeometryVersionResponse geometry = requireGeometryVersion(task.targetId());

        if (request.decision() == WorkflowDecision.REJECT) {
            markRejected(geometry.id());
            ParcelGeometryVersionResponse rejected = requireGeometryVersion(geometry.id());
            auditService.record(
                    "parcel.geometry-approval-rejected",
                    "parcel",
                    rejected.parcelId().toString(),
                    AuditClassification.STAFF_OPERATIONAL);
            return new ParcelGeometryApprovalResponse(rejected, task.requestedAction(), task.status().name(), task.id());
        }

        requireReviewableGeometry(geometry);
        publishApprovedGeometry(geometry, actor);
        ParcelGeometryVersionResponse approved = requireGeometryVersion(geometry.id());
        auditService.record(
                "parcel.geometry-approved-current",
                "parcel",
                approved.parcelId().toString(),
                AuditClassification.STAFF_OPERATIONAL);
        return new ParcelGeometryApprovalResponse(approved, task.requestedAction(), task.status().name(), task.id());
    }

    private void requireParcel(UUID parcelId) {
        if (!parcels.existsById(parcelId)) {
            throw new ParcelNotFoundException(parcelId);
        }
    }

    private ParcelGeometryVersionResponse requireGeometryVersion(UUID geometryVersionId) {
        return findGeometryVersion(geometryVersionId)
                .orElseThrow(() -> new ParcelGeometryNotFoundException(geometryVersionId));
    }

    private Optional<ParcelGeometryVersionResponse> findGeometryVersion(UUID geometryVersionId) {
        return jdbcTemplate.query(
                """
                SELECT
                    id,
                    parcel_id,
                    ST_AsText(geometry) AS geometry_wkt,
                    geometry_status,
                    calculated_area_square_meters,
                    source,
                    effective_from,
                    effective_to,
                    approved_by,
                    approved_at,
                    created_at
                FROM parcels.parcel_geometry_versions
                WHERE id = ?
                """,
                mapper(),
                geometryVersionId).stream().findFirst();
    }

    private void requireGeometryBelongsToParcel(UUID parcelId, ParcelGeometryVersionResponse geometry) {
        if (!geometry.parcelId().equals(parcelId)) {
            throw new ParcelGeometryNotFoundException(geometry.id());
        }
    }

    private void requireReviewableGeometry(ParcelGeometryVersionResponse geometry) {
        if (!"DRAFT".equals(geometry.geometryStatus()) && !"VALIDATED".equals(geometry.geometryStatus())) {
            throw new InvalidParcelGeometryStateException(geometry.id(), geometry.geometryStatus());
        }
    }

    private void requireSupportedTask(WorkflowTask task) {
        if (!WORKFLOW_TYPE.equals(task.workflowType())
                || !TARGET_TYPE.equals(task.targetType())
                || !REQUESTED_ACTION.equals(task.requestedAction())) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
    }

    private void markRejected(UUID geometryVersionId) {
        int updated = jdbcTemplate.update(
                """
                UPDATE parcels.parcel_geometry_versions
                SET geometry_status = 'REJECTED'
                WHERE id = ?
                  AND geometry_status IN ('DRAFT', 'VALIDATED')
                """,
                geometryVersionId);
        if (updated != 1) {
            ParcelGeometryVersionResponse geometry = requireGeometryVersion(geometryVersionId);
            throw new InvalidParcelGeometryStateException(geometryVersionId, geometry.geometryStatus());
        }
    }

    private void publishApprovedGeometry(ParcelGeometryVersionResponse geometry, AuthenticatedActor actor) {
        jdbcTemplate.update(
                """
                UPDATE parcels.parcel_geometry_versions
                SET geometry_status = 'SUPERSEDED',
                    effective_to = now()
                WHERE parcel_id = ?
                  AND geometry_status = 'APPROVED'
                  AND effective_to IS NULL
                """,
                geometry.parcelId());
        int updated = jdbcTemplate.update(
                """
                UPDATE parcels.parcel_geometry_versions
                SET geometry_status = 'APPROVED',
                    approved_by = ?,
                    approved_at = now(),
                    effective_from = now(),
                    effective_to = NULL
                WHERE id = ?
                  AND parcel_id = ?
                  AND geometry_status IN ('DRAFT', 'VALIDATED')
                """,
                actor.userId(),
                geometry.id(),
                geometry.parcelId());
        if (updated != 1) {
            ParcelGeometryVersionResponse refreshed = requireGeometryVersion(geometry.id());
            throw new InvalidParcelGeometryStateException(geometry.id(), refreshed.geometryStatus());
        }
        jdbcTemplate.update(
                "UPDATE surveys.surveys SET status = 'APPROVED', updated_at = now(), version = version + 1 WHERE status = 'SUBMITTED' AND id = (SELECT regexp_replace(source, '^FIELD_SURVEY:', '')::uuid FROM parcels.parcel_geometry_versions WHERE id = ? AND source LIKE 'FIELD_SURVEY:%')",
                geometry.id());
    }

    private RowMapper<ParcelGeometryVersionResponse> mapper() {
        return ParcelGeometryService::mapGeometryVersion;
    }

    private static ParcelGeometryVersionResponse mapGeometryVersion(ResultSet resultSet, int rowNum) throws SQLException {
        return new ParcelGeometryVersionResponse(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("parcel_id", UUID.class),
                resultSet.getString("geometry_wkt"),
                resultSet.getString("geometry_status"),
                resultSet.getBigDecimal("calculated_area_square_meters"),
                resultSet.getString("source"),
                resultSet.getObject("effective_from", java.time.OffsetDateTime.class),
                resultSet.getObject("effective_to", java.time.OffsetDateTime.class),
                resultSet.getObject("approved_by", UUID.class),
                resultSet.getObject("approved_at", java.time.OffsetDateTime.class),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class));
    }
}
