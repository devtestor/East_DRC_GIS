package cd.edrc.landgis.rights;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.config.SecurityConfig;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import cd.edrc.landgis.parties.DataConfidence;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import java.math.BigDecimal;
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

@WebMvcTest(OwnershipInterestController.class)
@Import(SecurityConfig.class)
class OwnershipInterestControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private OwnershipInterestService interestService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedInterestCreation() throws Exception {
        mvc.perform(post("/api/v1/parcels/{parcelId}/ownership-interests", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffInterestCreation() throws Exception {
        UUID parcelId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(interestService.create(eq(parcelId), any(CreateOwnershipInterestRequest.class), eq(actor)))
                .thenReturn(new OwnershipInterestResponse(
                        UUID.randomUUID(),
                        parcelId,
                        partyId,
                        "Fictional Claimant",
                        InterestType.OWNERSHIP_CLAIM.name(),
                        InterestStatus.CLAIMED.name(),
                        new BigDecimal("100.00"),
                        DataConfidence.SELF_DECLARED.name(),
                        "interview-note",
                        null,
                        null,
                        actor.userId(),
                        actor.username(),
                        OffsetDateTime.now()));

        mvc.perform(post("/api/v1/parcels/{parcelId}/ownership-interests", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "partyId": "%s",
                                  "interestType": "OWNERSHIP_CLAIM",
                                  "sharePercent": 100.00,
                                  "dataConfidence": "SELF_DECLARED",
                                  "source": "interview-note"
                                }
                                """.formatted(partyId)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffInterestListing() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(interestService.listByParcel(parcelId)).thenReturn(List.of());

        mvc.perform(get("/api/v1/parcels/{parcelId}/ownership-interests", parcelId))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsUnauthenticatedConflictListing() throws Exception {
        mvc.perform(get("/api/v1/parcels/{parcelId}/ownership-interests/conflicts", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffConflictListing() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(interestService.listConflicts(parcelId)).thenReturn(List.of());

        mvc.perform(get("/api/v1/parcels/{parcelId}/ownership-interests/conflicts", parcelId))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsUnauthenticatedReviewRequest() throws Exception {
        mvc.perform(post(
                        "/api/v1/parcels/{parcelId}/ownership-interests/{interestId}/review-requests",
                        UUID.randomUUID(),
                        UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffReviewRequest() throws Exception {
        UUID parcelId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        OwnershipInterestResponse interest = interest(parcelId, interestId, actor);
        when(interestService.requestReview(parcelId, interestId, actor))
                .thenReturn(new OwnershipInterestReviewResponse(
                        interest,
                        "VERIFY_OWNERSHIP_INTEREST",
                        "OPEN",
                        taskId));

        mvc.perform(post(
                        "/api/v1/parcels/{parcelId}/ownership-interests/{interestId}/review-requests",
                        parcelId,
                        interestId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "checker@example.test")
    void allowsStaffReviewDecision() throws Exception {
        UUID parcelId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "checker@example.test");
        when(actorResolver.requireActor("checker@example.test")).thenReturn(actor);
        when(interestService.decideReviewTask(eq(taskId), any(DecideWorkflowTaskRequest.class), eq(actor)))
                .thenReturn(new OwnershipInterestReviewResponse(
                        interest(parcelId, interestId, actor),
                        "VERIFY_OWNERSHIP_INTEREST",
                        "APPROVED",
                        taskId));

        mvc.perform(post(
                        "/api/v1/parcels/{parcelId}/ownership-interests/tasks/{taskId}/decisions",
                        parcelId,
                        taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Evidence reviewed"
                                }
                                """))
                .andExpect(status().isOk());
    }

    private static OwnershipInterestResponse interest(UUID parcelId, UUID interestId, AuthenticatedActor actor) {
        return new OwnershipInterestResponse(
                interestId,
                parcelId,
                UUID.randomUUID(),
                "Fictional Claimant",
                InterestType.OWNERSHIP_CLAIM.name(),
                InterestStatus.UNDER_REVIEW.name(),
                new BigDecimal("100.00"),
                DataConfidence.DOCUMENT_SUPPORTED.name(),
                "review-note",
                null,
                null,
                actor.userId(),
                actor.username(),
                OffsetDateTime.now());
    }
}
