package cd.edrc.landgis.documents;

import cd.edrc.landgis.common.AuthenticatedActor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentAccessAuthorizer {
    private static final String WORKFLOW_ACCESS_POLICY = "workflow-task-and-authorized-staff";
    private static final String APPLICATION_APPLICANT_ACCESS_POLICY = "application-applicant-and-authorized-staff";

    private final JdbcTemplate jdbcTemplate;

    DocumentAccessAuthorizer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public boolean canRead(DocumentRecord document, AuthenticatedActor actor) {
        if (document.createdByUserId().equals(actor.userId())) {
            return true;
        }
        if (hasCustodianRole(document, actor)) {
            return true;
        }
        if (APPLICATION_APPLICANT_ACCESS_POLICY.equals(document.accessPolicy())
                && isApprovedApplicationApplicant(document, actor)) {
            return true;
        }
        if (!WORKFLOW_ACCESS_POLICY.equals(document.accessPolicy())) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM workflow.task_evidence_links evidence
                JOIN workflow.tasks task ON task.id = evidence.task_id
                WHERE evidence.evidence_type = 'DOCUMENT'
                  AND lower(evidence.reference_type) = 'document'
                  AND evidence.reference_id = ?
                  AND (
                      task.requested_by_user_id = ?
                      OR task.assigned_to_user_id = ?
                      OR task.decided_by_user_id = ?
                  )
                """,
                Integer.class,
                document.id(),
                actor.userId(),
                actor.userId(),
                actor.userId());
        return count != null && count > 0;
    }

    private boolean isApprovedApplicationApplicant(DocumentRecord document, AuthenticatedActor actor) {
        if (!"application".equalsIgnoreCase(document.ownerType())) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM transactions.applications application
                WHERE application.id = ?
                  AND application.generated_document_id = ?
                  AND application.applicant_user_id = ?
                  AND application.status = 'APPROVED'
                """,
                Integer.class,
                document.ownerId(),
                document.id(),
                actor.userId());
        return count != null && count > 0;
    }

    public boolean canAppendVersion(DocumentRecord document, AuthenticatedActor actor) {
        return document.createdByUserId().equals(actor.userId()) || hasCustodianRole(document, actor);
    }

    private boolean hasCustodianRole(DocumentRecord document, AuthenticatedActor actor) {
        if (document.custodianOrganizationId() == null || document.custodianRoleCode() == null
                || document.custodianRoleCode().isBlank()) {
            return false;
        }
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM identity.organization_memberships membership
                JOIN identity.roles role ON role.id = membership.role_id
                WHERE membership.user_id = ?
                  AND membership.organization_id = ?
                  AND membership.status = 'ACTIVE'
                  AND role.code = ?
                  AND (membership.starts_at IS NULL OR membership.starts_at <= now())
                  AND (membership.ends_at IS NULL OR membership.ends_at > now())
                """,
                Integer.class,
                actor.userId(),
                document.custodianOrganizationId(),
                document.custodianRoleCode());
        return count != null && count > 0;
    }
}
