package cd.edrc.landgis.disputes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.config.SecurityConfig;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
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

@WebMvcTest(DisputeCaseController.class)
@Import(SecurityConfig.class)
class DisputeCaseControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private DisputeCaseService disputeService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedDisputeCreation() throws Exception {
        mvc.perform(post("/api/v1/parcels/{parcelId}/disputes", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffDisputeListing() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(disputeService.listByParcel(parcelId)).thenReturn(List.of());

        mvc.perform(get("/api/v1/parcels/{parcelId}/disputes", parcelId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffDisputeCreation() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(actorResolver.requireActor("officer@example.test"))
                .thenReturn(new cd.edrc.landgis.common.AuthenticatedActor(UUID.randomUUID(), "officer@example.test"));
        when(disputeService.create(eq(parcelId), any(CreateDisputeCaseRequest.class), any()))
                .thenReturn(new DisputeCaseResponse(
                        UUID.randomUUID(), parcelId, DisputeCaseType.OWNERSHIP_DISPUTE.name(),
                        DisputeCaseStatus.OPEN.name(), "Summary", "complaint", null,
                        UUID.randomUUID(), "officer@example.test", java.time.OffsetDateTime.now(), java.time.OffsetDateTime.now()));

        mvc.perform(post("/api/v1/parcels/{parcelId}/disputes", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"caseType":"OWNERSHIP_DISPUTE","summary":"Summary","source":"complaint"}
                                """))
                .andExpect(status().isCreated());
    }
}
