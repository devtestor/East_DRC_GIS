package cd.edrc.landgis.workflow;

import cd.edrc.landgis.common.AuthenticatedActor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowRoleScopeAuthorizer {
    private final JdbcTemplate jdbcTemplate;

    WorkflowRoleScopeAuthorizer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public void requireClaimScope(AuthenticatedActor actor, WorkflowTask task) {
        String requiredRole = task.assignedToRole();
        if (requiredRole == null || requiredRole.isBlank()) {
            throw new WorkflowRoleScopeViolationException(actor.userId(), "UNSPECIFIED");
        }

        Integer count = switch (task.targetType()) {
            case "parcel" -> countMatchingParcelScope(actor, task.targetId(), requiredRole);
            case "ownership-interest" -> countMatchingOwnershipInterestScope(actor, task.targetId(), requiredRole);
            case "parcel-restriction" -> countMatchingParcelRestrictionScope(actor, task.targetId(), requiredRole);
            case "dispute-case" -> countMatchingDisputeCaseScope(actor, task.targetId(), requiredRole);
            case "parcel-information-application" -> countMatchingApplicationScope(actor, task.targetId(), requiredRole);
            default -> countMatchingRoleOnlyScope(actor, requiredRole);
        };
        if (count == null || count == 0) {
            throw new WorkflowRoleScopeViolationException(actor.userId(), requiredRole);
        }
    }

    private Integer countMatchingRoleOnlyScope(AuthenticatedActor actor, String requiredRole) {
        return jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM identity.organization_memberships membership
                JOIN identity.roles role ON role.id = membership.role_id
                WHERE membership.user_id = ?
                  AND membership.status = 'ACTIVE'
                  AND role.code = ?
                  AND (membership.starts_at IS NULL OR membership.starts_at <= now())
                  AND (membership.ends_at IS NULL OR membership.ends_at > now())
                """,
                Integer.class,
                actor.userId(),
                requiredRole);
    }

    private Integer countMatchingParcelScope(AuthenticatedActor actor, java.util.UUID parcelId, String requiredRole) {
        Integer count = jdbcTemplate.queryForObject(
                """
                WITH RECURSIVE target_units AS (
                    SELECT unit.id, unit.parent_id, unit.code, unit.unit_type
                    FROM parcels.parcels parcel
                    JOIN administration.administrative_units unit ON unit.id = parcel.administrative_unit_id
                    WHERE parcel.id = ?
                    UNION ALL
                    SELECT parent.id, parent.parent_id, parent.code, parent.unit_type
                    FROM administration.administrative_units parent
                    JOIN target_units child ON child.parent_id = parent.id
                ),
                target_province AS (
                    SELECT code FROM target_units WHERE unit_type = 'PROVINCE' LIMIT 1
                )
                SELECT count(*)
                FROM identity.organization_memberships membership
                JOIN identity.roles role ON role.id = membership.role_id
                WHERE membership.user_id = ?
                  AND membership.status = 'ACTIVE'
                  AND role.code = ?
                  AND (membership.starts_at IS NULL OR membership.starts_at <= now())
                  AND (membership.ends_at IS NULL OR membership.ends_at > now())
                  AND (
                      membership.province_code IS NULL
                      OR membership.province_code = ''
                      OR membership.province_code = (SELECT code FROM target_province)
                  )
                  AND (
                      membership.jurisdiction_path IS NULL
                      OR membership.jurisdiction_path = ''
                      OR membership.jurisdiction_path = '*'
                      OR EXISTS (
                          SELECT 1
                          FROM target_units
                          WHERE target_units.code = membership.jurisdiction_path
                      )
                  )
                """,
                Integer.class,
                parcelId,
                actor.userId(),
                requiredRole);
        return count;
    }

    private Integer countMatchingOwnershipInterestScope(
            AuthenticatedActor actor,
            java.util.UUID ownershipInterestId,
            String requiredRole) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM rights.ownership_interests interest
                WHERE interest.id = ?
                  AND EXISTS (
                      WITH RECURSIVE target_units AS (
                          SELECT unit.id, unit.parent_id, unit.code, unit.unit_type
                          FROM parcels.parcels parcel
                          JOIN administration.administrative_units unit ON unit.id = parcel.administrative_unit_id
                          WHERE parcel.id = interest.parcel_id
                          UNION ALL
                          SELECT parent.id, parent.parent_id, parent.code, parent.unit_type
                          FROM administration.administrative_units parent
                          JOIN target_units child ON child.parent_id = parent.id
                      ),
                      target_province AS (
                          SELECT code FROM target_units WHERE unit_type = 'PROVINCE' LIMIT 1
                      )
                      SELECT 1
                      FROM identity.organization_memberships membership
                      JOIN identity.roles role ON role.id = membership.role_id
                      WHERE membership.user_id = ?
                        AND membership.status = 'ACTIVE'
                        AND role.code = ?
                        AND (membership.starts_at IS NULL OR membership.starts_at <= now())
                        AND (membership.ends_at IS NULL OR membership.ends_at > now())
                        AND (
                            membership.province_code IS NULL
                            OR membership.province_code = ''
                            OR membership.province_code = (SELECT code FROM target_province)
                        )
                        AND (
                            membership.jurisdiction_path IS NULL
                            OR membership.jurisdiction_path = ''
                            OR membership.jurisdiction_path = '*'
                            OR EXISTS (
                                SELECT 1
                                FROM target_units
                                WHERE target_units.code = membership.jurisdiction_path
                            )
                        )
                  )
                """,
                Integer.class,
                ownershipInterestId,
                actor.userId(),
                requiredRole);
        return count;
    }

    private Integer countMatchingParcelRestrictionScope(
            AuthenticatedActor actor,
            java.util.UUID restrictionId,
            String requiredRole) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM disputes.parcel_restrictions restriction
                WHERE restriction.id = ?
                  AND EXISTS (
                      WITH RECURSIVE target_units AS (
                          SELECT unit.id, unit.parent_id, unit.code, unit.unit_type
                          FROM parcels.parcels parcel
                          JOIN administration.administrative_units unit ON unit.id = parcel.administrative_unit_id
                          WHERE parcel.id = restriction.parcel_id
                          UNION ALL
                          SELECT parent.id, parent.parent_id, parent.code, parent.unit_type
                          FROM administration.administrative_units parent
                          JOIN target_units child ON child.parent_id = parent.id
                      ),
                      target_province AS (
                          SELECT code FROM target_units WHERE unit_type = 'PROVINCE' LIMIT 1
                      )
                      SELECT 1
                      FROM identity.organization_memberships membership
                      JOIN identity.roles role ON role.id = membership.role_id
                      WHERE membership.user_id = ?
                        AND membership.status = 'ACTIVE'
                        AND role.code = ?
                        AND (membership.starts_at IS NULL OR membership.starts_at <= now())
                        AND (membership.ends_at IS NULL OR membership.ends_at > now())
                        AND (
                            membership.province_code IS NULL
                            OR membership.province_code = ''
                            OR membership.province_code = (SELECT code FROM target_province)
                        )
                        AND (
                            membership.jurisdiction_path IS NULL
                            OR membership.jurisdiction_path = ''
                            OR membership.jurisdiction_path = '*'
                            OR EXISTS (
                                SELECT 1 FROM target_units
                                WHERE target_units.code = membership.jurisdiction_path
                            )
                        )
                  )
                """,
                Integer.class,
                restrictionId,
                actor.userId(),
                requiredRole);
        return count;
    }

    private Integer countMatchingDisputeCaseScope(
            AuthenticatedActor actor,
            java.util.UUID caseId,
            String requiredRole) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM disputes.cases dispute_case
                WHERE dispute_case.id = ?
                  AND EXISTS (
                      SELECT 1
                      FROM identity.organization_memberships membership
                      JOIN identity.roles role ON role.id = membership.role_id
                      WHERE membership.user_id = ?
                        AND membership.status = 'ACTIVE'
                        AND role.code = ?
                        AND (membership.starts_at IS NULL OR membership.starts_at <= now())
                        AND (membership.ends_at IS NULL OR membership.ends_at > now())
                        AND (
                            membership.province_code IS NULL OR membership.province_code = '' OR membership.province_code = (
                                WITH RECURSIVE target_units AS (
                                    SELECT unit.id, unit.parent_id, unit.code, unit.unit_type
                                    FROM parcels.parcels parcel
                                    JOIN administration.administrative_units unit ON unit.id = parcel.administrative_unit_id
                                    WHERE parcel.id = dispute_case.parcel_id
                                    UNION ALL
                                    SELECT parent.id, parent.parent_id, parent.code, parent.unit_type
                                    FROM administration.administrative_units parent
                                    JOIN target_units child ON child.parent_id = parent.id
                                )
                                SELECT code FROM target_units WHERE unit_type = 'PROVINCE' LIMIT 1
                            )
                        )
                  )
                """,
                Integer.class,
                caseId,
                actor.userId(),
                requiredRole);
        return count;
    }

    private Integer countMatchingApplicationScope(
            AuthenticatedActor actor,
            java.util.UUID applicationId,
            String requiredRole) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM transactions.applications application
                WHERE application.id = ?
                  AND EXISTS (
                      WITH RECURSIVE target_units AS (
                          SELECT unit.id, unit.parent_id, unit.code, unit.unit_type
                          FROM parcels.parcels parcel
                          JOIN administration.administrative_units unit ON unit.id = parcel.administrative_unit_id
                          WHERE parcel.id = application.parcel_id
                          UNION ALL
                          SELECT parent.id, parent.parent_id, parent.code, parent.unit_type
                          FROM administration.administrative_units parent
                          JOIN target_units child ON child.parent_id = parent.id
                      ),
                      target_province AS (
                          SELECT code FROM target_units WHERE unit_type = 'PROVINCE' LIMIT 1
                      )
                      SELECT 1
                      FROM identity.organization_memberships membership
                      JOIN identity.roles role ON role.id = membership.role_id
                      WHERE membership.user_id = ?
                        AND membership.status = 'ACTIVE'
                        AND role.code = ?
                        AND (membership.starts_at IS NULL OR membership.starts_at <= now())
                        AND (membership.ends_at IS NULL OR membership.ends_at > now())
                        AND (membership.province_code IS NULL OR membership.province_code = '' OR membership.province_code = (SELECT code FROM target_province))
                  )
                """,
                Integer.class,
                applicationId,
                actor.userId(),
                requiredRole);
        return count;
    }
}
