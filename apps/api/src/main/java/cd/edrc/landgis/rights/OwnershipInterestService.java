package cd.edrc.landgis.rights;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.disputes.ParcelRestrictionService;
import cd.edrc.landgis.parcels.ParcelNotFoundException;
import cd.edrc.landgis.parcels.ParcelRepository;
import cd.edrc.landgis.parties.PartyNotFoundException;
import cd.edrc.landgis.parties.PartyService;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class OwnershipInterestService {
    private final ParcelRepository parcels;
    private final PartyService parties;
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;
    private final WorkflowTaskService workflowTasks;
    private final ParcelRestrictionService restrictions;

    OwnershipInterestService(
            ParcelRepository parcels,
            PartyService parties,
            JdbcTemplate jdbcTemplate,
            AuditService auditService,
            WorkflowTaskService workflowTasks,
            ParcelRestrictionService restrictions) {
        this.parcels = parcels;
        this.parties = parties;
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
        this.workflowTasks = workflowTasks;
        this.restrictions = restrictions;
    }

    @Transactional
    OwnershipInterestResponse create(UUID parcelId, CreateOwnershipInterestRequest request, AuthenticatedActor actor) {
        requireParcel(parcelId);
        restrictions.requireNoOwnershipChangeBlock(parcelId);
        parties.find(request.partyId()).orElseThrow(() -> new PartyNotFoundException(request.partyId()));
        OwnershipInterestResponse response = jdbcTemplate.queryForObject(
                """
                INSERT INTO rights.ownership_interests (
                    id,
                    parcel_id,
                    party_id,
                    interest_type,
                    interest_status,
                    share_percent,
                    data_confidence,
                    source,
                    created_by_user_id,
                    created_by
                )
                VALUES (?, ?, ?, ?, 'CLAIMED', ?, ?, ?, ?, ?)
                RETURNING
                    id,
                    parcel_id,
                    party_id,
                    (SELECT display_name FROM parties.parties p WHERE p.id = party_id) AS party_display_name,
                    interest_type,
                    interest_status,
                    share_percent,
                    data_confidence,
                    source,
                    effective_from,
                    effective_to,
                    created_by_user_id,
                    created_by,
                    created_at
                """,
                mapper(),
                UUID.randomUUID(),
                parcelId,
                request.partyId(),
                request.interestType().name(),
                request.sharePercent(),
                request.dataConfidence().name(),
                request.source().trim(),
                actor.userId(),
                actor.username());
        auditService.record(
                "ownership-interest.claim-created",
                "parcel",
                parcelId.toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of(
                        "interestId", response.id().toString(),
                        "partyId", request.partyId().toString(),
                        "interestType", response.interestType(),
                        "interestStatus", response.interestStatus(),
                        "dataConfidence", response.dataConfidence()));
        return response;
    }

    @Transactional(readOnly = true)
    List<OwnershipInterestResponse> listByParcel(UUID parcelId) {
        requireParcel(parcelId);
        return jdbcTemplate.query(
                """
                SELECT
                    interest.id,
                    interest.parcel_id,
                    interest.party_id,
                    party.display_name AS party_display_name,
                    interest.interest_type,
                    interest.interest_status,
                    interest.share_percent,
                    interest.data_confidence,
                    interest.source,
                    interest.effective_from,
                    interest.effective_to,
                    interest.created_by_user_id,
                    interest.created_by,
                    interest.created_at
                FROM rights.ownership_interests interest
                JOIN parties.parties party ON party.id = interest.party_id
                WHERE interest.parcel_id = ?
                ORDER BY interest.created_at DESC
                """,
                mapper(),
                parcelId);
    }

    @Transactional(readOnly = true)
    List<OwnershipConflictResponse> listConflicts(UUID parcelId) {
        requireParcel(parcelId);
        return jdbcTemplate.query(
                """
                SELECT 'COMPETING_OWNERSHIP_CLAIMS' AS conflict_type,
                       'HIGH' AS severity,
                       count(*)::int AS affected_interest_count,
                       'Multiple active ownership claims require human review; this is not a legal ownership determination.' AS summary
                FROM rights.ownership_interests
                WHERE parcel_id = ?
                  AND interest_type = 'OWNERSHIP_CLAIM'
                  AND interest_status IN ('CLAIMED', 'UNDER_REVIEW', 'VERIFIED')
                HAVING count(*) > 1
                UNION ALL
                SELECT 'OWNERSHIP_SHARE_OVERALLOCATION' AS conflict_type,
                       'HIGH' AS severity,
                       count(*)::int AS affected_interest_count,
                       'Active ownership-claim shares exceed 100 percent and require reconciliation.' AS summary
                FROM rights.ownership_interests
                WHERE parcel_id = ?
                  AND interest_type = 'OWNERSHIP_CLAIM'
                  AND interest_status IN ('CLAIMED', 'UNDER_REVIEW', 'VERIFIED')
                HAVING COALESCE(sum(share_percent), 0) > 100
                """,
                (resultSet, rowNum) -> new OwnershipConflictResponse(
                        parcelId,
                        resultSet.getString("conflict_type"),
                        resultSet.getString("severity"),
                        resultSet.getInt("affected_interest_count"),
                        resultSet.getString("summary")),
                parcelId,
                parcelId);
    }

    @Transactional
    OwnershipInterestReviewResponse requestReview(UUID parcelId, UUID interestId, AuthenticatedActor actor) {
        requireParcel(parcelId);
        restrictions.requireNoOwnershipChangeBlock(parcelId);
        OwnershipInterestResponse interest = getInterestForParcel(parcelId, interestId);
        UUID taskId = workflowTasks.openOwnershipInterestReviewTask(interestId, actor);
        OwnershipInterestResponse underReview = updateInterestStatus(
                interest.id(),
                InterestStatus.UNDER_REVIEW,
                "ownership-interest.review-requested",
                actor,
                taskId);
        return new OwnershipInterestReviewResponse(
                underReview,
                "VERIFY_OWNERSHIP_INTEREST",
                "OPEN",
                taskId);
    }

    @Transactional
    OwnershipInterestReviewResponse decideReviewTask(
            UUID taskId,
            DecideWorkflowTaskRequest request,
            AuthenticatedActor actor) {
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        requireSupportedTask(task);
        UUID parcelId = parcelIdForInterest(task.targetId());
        restrictions.requireNoOwnershipChangeBlock(parcelId);
        InterestStatus targetStatus = request.decision() == WorkflowDecision.APPROVE
                ? InterestStatus.VERIFIED
                : InterestStatus.REJECTED;
        OwnershipInterestResponse interest = updateInterestStatus(
                task.targetId(),
                targetStatus,
                request.decision() == WorkflowDecision.APPROVE
                        ? "ownership-interest.review-approved"
                        : "ownership-interest.review-rejected",
                actor,
                taskId);
        return new OwnershipInterestReviewResponse(
                interest,
                task.requestedAction(),
                task.status().name(),
                task.id());
    }

    private OwnershipInterestResponse getInterestForParcel(UUID parcelId, UUID interestId) {
        List<OwnershipInterestResponse> matches = jdbcTemplate.query(
                """
                SELECT
                    interest.id,
                    interest.parcel_id,
                    interest.party_id,
                    party.display_name AS party_display_name,
                    interest.interest_type,
                    interest.interest_status,
                    interest.share_percent,
                    interest.data_confidence,
                    interest.source,
                    interest.effective_from,
                    interest.effective_to,
                    interest.created_by_user_id,
                    interest.created_by,
                    interest.created_at
                FROM rights.ownership_interests interest
                JOIN parties.parties party ON party.id = interest.party_id
                WHERE interest.parcel_id = ?
                  AND interest.id = ?
                """,
                mapper(),
                parcelId,
                interestId);
        if (matches.isEmpty()) {
            throw new OwnershipInterestNotFoundException(interestId);
        }
        return matches.getFirst();
    }

    private OwnershipInterestResponse updateInterestStatus(
            UUID interestId,
            InterestStatus status,
            String auditAction,
            AuthenticatedActor actor,
            UUID taskId) {
        OwnershipInterestResponse response = jdbcTemplate.queryForObject(
                """
                UPDATE rights.ownership_interests
                SET interest_status = ?,
                    updated_at = now(),
                    version = version + 1
                WHERE id = ?
                RETURNING
                    id,
                    parcel_id,
                    party_id,
                    (SELECT display_name FROM parties.parties p WHERE p.id = party_id) AS party_display_name,
                    interest_type,
                    interest_status,
                    share_percent,
                    data_confidence,
                    source,
                    effective_from,
                    effective_to,
                    created_by_user_id,
                    created_by,
                    created_at
                """,
                mapper(),
                status.name(),
                interestId);
        auditService.record(
                auditAction,
                "ownership-interest",
                interestId.toString(),
                AuditClassification.LEGAL_EVIDENCE,
                actor.userId(),
                null,
                java.util.Map.of(
                        "parcelId", response.parcelId().toString(),
                        "partyId", response.partyId().toString(),
                        "interestStatus", response.interestStatus(),
                        "taskId", taskId.toString(),
                        "legalBoundary", "Operational review only; not an official land-title certificate"));
        return response;
    }

    private UUID parcelIdForInterest(UUID interestId) {
        List<UUID> matches = jdbcTemplate.query(
                """
                SELECT parcel_id
                FROM rights.ownership_interests
                WHERE id = ?
                """,
                (resultSet, rowNum) -> resultSet.getObject("parcel_id", UUID.class),
                interestId);
        if (matches.isEmpty()) {
            throw new OwnershipInterestNotFoundException(interestId);
        }
        return matches.getFirst();
    }

    private void requireSupportedTask(WorkflowTask task) {
        if (!"OWNERSHIP_INTEREST_REVIEW".equals(task.workflowType())
                || !"ownership-interest".equals(task.targetType())
                || !"VERIFY_OWNERSHIP_INTEREST".equals(task.requestedAction())) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
    }

    private void requireParcel(UUID parcelId) {
        if (!parcels.existsById(parcelId)) {
            throw new ParcelNotFoundException(parcelId);
        }
    }

    private RowMapper<OwnershipInterestResponse> mapper() {
        return OwnershipInterestService::map;
    }

    private static OwnershipInterestResponse map(ResultSet resultSet, int rowNum) throws SQLException {
        return new OwnershipInterestResponse(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("parcel_id", UUID.class),
                resultSet.getObject("party_id", UUID.class),
                resultSet.getString("party_display_name"),
                resultSet.getString("interest_type"),
                resultSet.getString("interest_status"),
                resultSet.getBigDecimal("share_percent"),
                resultSet.getString("data_confidence"),
                resultSet.getString("source"),
                resultSet.getObject("effective_from", java.time.OffsetDateTime.class),
                resultSet.getObject("effective_to", java.time.OffsetDateTime.class),
                resultSet.getObject("created_by_user_id", UUID.class),
                resultSet.getString("created_by"),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class));
    }
}
