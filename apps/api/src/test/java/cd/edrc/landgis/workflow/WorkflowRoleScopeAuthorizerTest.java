package cd.edrc.landgis.workflow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

class WorkflowRoleScopeAuthorizerTest {
    @Test
    void allowsActorWithActiveRequiredRole() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "checker@example.test");
        WorkflowTask task = task(UUID.randomUUID());
        when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.eq(Integer.class),
                Mockito.eq(task.targetId()),
                Mockito.eq(actor.userId()),
                Mockito.eq("CADASTRAL_OFFICER"))).thenReturn(1);
        WorkflowRoleScopeAuthorizer authorizer = new WorkflowRoleScopeAuthorizer(jdbcTemplate);

        authorizer.requireClaimScope(actor, task);
    }

    @Test
    void rejectsActorWithoutActiveRequiredRole() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "checker@example.test");
        WorkflowTask task = task(UUID.randomUUID());
        when(jdbcTemplate.queryForObject(
                Mockito.anyString(),
                Mockito.eq(Integer.class),
                Mockito.eq(task.targetId()),
                Mockito.eq(actor.userId()),
                Mockito.eq("CADASTRAL_OFFICER"))).thenReturn(0);
        WorkflowRoleScopeAuthorizer authorizer = new WorkflowRoleScopeAuthorizer(jdbcTemplate);

        assertThatThrownBy(() -> authorizer.requireClaimScope(actor, task))
                .isInstanceOf(WorkflowRoleScopeViolationException.class);
    }

    private WorkflowTask task(UUID parcelId) {
        return new WorkflowTask(
                UUID.randomUUID(),
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                parcelId,
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                UUID.randomUUID(),
                "maker@example.test",
                "CADASTRAL_OFFICER");
    }
}
