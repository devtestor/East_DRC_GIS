package cd.edrc.landgis.documents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

class DocumentAccessAuthorizerTest {
    @Test
    void creatorCanReadWithoutWorkflowLookup() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "creator@example.test");
        DocumentRecord document = documentCreatedBy(actor.userId(), "restricted-staff");

        assertThat(authorizer.canRead(document, actor)).isTrue();
        verify(jdbcTemplate, never()).queryForObject(any(String.class), eq(Integer.class), any());
    }

    @Test
    void workflowParticipantCanReadWorkflowScopedDocument() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DocumentRecord document = documentCreatedBy(UUID.randomUUID(), "workflow-task-and-authorized-staff");
        when(jdbcTemplate.queryForObject(
                any(String.class),
                eq(Integer.class),
                eq(document.id()),
                eq(actor.userId()),
                eq(actor.userId()),
                eq(actor.userId())))
                .thenReturn(1);

        assertThat(authorizer.canRead(document, actor)).isTrue();
    }

    @Test
    void custodianOrganizationMemberCanReadDocument() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "custodian@example.test");
        UUID organizationId = UUID.randomUUID();
        DocumentRecord document = documentCreatedBy(
                UUID.randomUUID(),
                "restricted-staff",
                organizationId,
                "CADASTRAL_OFFICER");
        when(jdbcTemplate.queryForObject(
                any(String.class),
                eq(Integer.class),
                eq(actor.userId()),
                eq(organizationId),
                eq("CADASTRAL_OFFICER")))
                .thenReturn(1);

        assertThat(authorizer.canRead(document, actor)).isTrue();
    }

    @Test
    void nonParticipantCannotReadWorkflowScopedDocument() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DocumentRecord document = documentCreatedBy(UUID.randomUUID(), "workflow-task-and-authorized-staff");
        when(jdbcTemplate.queryForObject(
                any(String.class),
                eq(Integer.class),
                eq(document.id()),
                eq(actor.userId()),
                eq(actor.userId()),
                eq(actor.userId())))
                .thenReturn(0);

        assertThat(authorizer.canRead(document, actor)).isFalse();
    }

    @Test
    void approvedApplicationApplicantCanReadGeneratedReport() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "applicant@example.test");
        DocumentRecord document = documentCreatedBy(
                UUID.randomUUID(), "application-applicant-and-authorized-staff", null, null, "application");
        when(jdbcTemplate.queryForObject(
                any(String.class),
                eq(Integer.class),
                eq(document.ownerId()),
                eq(document.id()),
                eq(actor.userId())))
                .thenReturn(1);

        assertThat(authorizer.canRead(document, actor)).isTrue();
    }

    @Test
    void unrelatedUserCannotReadGeneratedReport() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "other@example.test");
        DocumentRecord document = documentCreatedBy(
                UUID.randomUUID(), "application-applicant-and-authorized-staff", null, null, "application");
        when(jdbcTemplate.queryForObject(
                any(String.class),
                eq(Integer.class),
                eq(document.ownerId()),
                eq(document.id()),
                eq(actor.userId())))
                .thenReturn(0);

        assertThat(authorizer.canRead(document, actor)).isFalse();
    }

    @Test
    void nonCreatorCannotReadRestrictedStaffDocumentWithoutWorkflowPolicy() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DocumentRecord document = documentCreatedBy(UUID.randomUUID(), "restricted-staff");

        assertThat(authorizer.canRead(document, actor)).isFalse();
        verify(jdbcTemplate, never()).queryForObject(any(String.class), eq(Integer.class), any());
    }

    @Test
    void onlyCreatorCanAppendVersion() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor creator = new AuthenticatedActor(UUID.randomUUID(), "creator@example.test");
        AuthenticatedActor other = new AuthenticatedActor(UUID.randomUUID(), "other@example.test");
        DocumentRecord document = documentCreatedBy(creator.userId(), "workflow-task-and-authorized-staff");

        assertThat(authorizer.canAppendVersion(document, creator)).isTrue();
        assertThat(authorizer.canAppendVersion(document, other)).isFalse();
    }

    @Test
    void custodianOrganizationMemberCanAppendVersion() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        DocumentAccessAuthorizer authorizer = new DocumentAccessAuthorizer(jdbcTemplate);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "custodian@example.test");
        UUID organizationId = UUID.randomUUID();
        DocumentRecord document = documentCreatedBy(
                UUID.randomUUID(),
                "restricted-staff",
                organizationId,
                "CADASTRAL_OFFICER");
        when(jdbcTemplate.queryForObject(
                any(String.class),
                eq(Integer.class),
                eq(actor.userId()),
                eq(organizationId),
                eq("CADASTRAL_OFFICER")))
                .thenReturn(1);

        assertThat(authorizer.canAppendVersion(document, actor)).isTrue();
    }

    private DocumentRecord documentCreatedBy(UUID createdByUserId, String accessPolicy) {
        return documentCreatedBy(createdByUserId, accessPolicy, null, null);
    }

    private DocumentRecord documentCreatedBy(
            UUID createdByUserId,
            String accessPolicy,
            UUID custodianOrganizationId,
            String custodianRoleCode) {
        return documentCreatedBy(
                createdByUserId, accessPolicy, custodianOrganizationId, custodianRoleCode, "parcel");
    }

    private DocumentRecord documentCreatedBy(
            UUID createdByUserId,
            String accessPolicy,
            UUID custodianOrganizationId,
            String custodianRoleCode,
            String ownerType) {
        return new DocumentRecord(
                UUID.randomUUID(),
                "SURVEY_PLAN",
                ownerType,
                UUID.randomUUID(),
                "Fictional survey plan",
                DocumentClassification.LEGAL_EVIDENCE,
                "LEGAL_RECORD",
                accessPolicy,
                custodianOrganizationId,
                custodianRoleCode,
                false,
                createdByUserId,
                "creator@example.test",
                null);
    }
}
