package cd.edrc.landgis.parcels;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.config.SecurityConfig;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.WorkflowTaskResponse;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ParcelWorkflowTaskController.class)
@Import(SecurityConfig.class)
class ParcelWorkflowTaskControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private WorkflowTaskService workflowTaskService;

    @MockBean
    private ParcelRegistryService parcelRegistryService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedWorkflowTaskList() throws Exception {
        mvc.perform(get("/api/v1/workflow/tasks"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffWorkflowTaskList() throws Exception {
        when(workflowTaskService.listOpenTasks()).thenReturn(List.of());

        mvc.perform(get("/api/v1/workflow/tasks"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsUnauthenticatedWorkflowTaskClaim() throws Exception {
        mvc.perform(post("/api/v1/workflow/tasks/{taskId}/claim", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "checker@example.test")
    void allowsStaffWorkflowTaskClaim() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID parcelId = UUID.randomUUID();
        UUID makerId = UUID.randomUUID();
        UUID checkerId = UUID.randomUUID();
        AuthenticatedActor checker = new AuthenticatedActor(checkerId, "checker@example.test");
        when(actorResolver.requireActor("checker@example.test")).thenReturn(checker);
        when(workflowTaskService.claimTask(any(UUID.class), any(AuthenticatedActor.class)))
                .thenReturn(new WorkflowTaskResponse(
                        taskId,
                        "PARCEL_STATUS_TRANSITION",
                        "parcel",
                        parcelId,
                        "UNDER_SURVEY_TO_UNDER_REVIEW",
                        makerId,
                        "maker@example.test",
                        "CLAIMED",
                        "NORMAL",
                        OffsetDateTime.now(),
                        "checker@example.test",
                        checkerId,
                        "CADASTRAL_OFFICER",
                        OffsetDateTime.now(),
                        null,
                        null,
                        null,
                        null));

        mvc.perform(post("/api/v1/workflow/tasks/{taskId}/claim", taskId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "checker@example.test")
    void allowsStaffWorkflowTaskDecision() throws Exception {
        UUID taskId = UUID.randomUUID();
        UUID parcelId = UUID.randomUUID();
        AuthenticatedActor checker = new AuthenticatedActor(UUID.randomUUID(), "checker@example.test");
        when(actorResolver.requireActor("checker@example.test")).thenReturn(checker);
        when(parcelRegistryService.decideStatusTransitionTask(
                any(UUID.class),
                any(DecideWorkflowTaskRequest.class),
                any(AuthenticatedActor.class)))
                .thenReturn(new ParcelTransitionResponse(
                        parcelId,
                        "UNDER_REVIEW",
                        "UNDER_REVIEW",
                        "APPROVED_AND_APPLIED",
                        taskId));

        mvc.perform(post("/api/v1/workflow/tasks/{taskId}/decisions", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Reviewed cadastral evidence"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
