package cd.edrc.landgis.parties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.config.SecurityConfig;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
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

@WebMvcTest(PartyController.class)
@Import(SecurityConfig.class)
class PartyControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PartyService partyService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedPartyCreation() throws Exception {
        mvc.perform(post("/api/v1/parties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffPartyCreation() throws Exception {
        UUID actorId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(actorId, "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(partyService.create(any(CreatePartyRequest.class), any(AuthenticatedActor.class)))
                .thenReturn(new PartyResponse(
                        UUID.randomUUID(),
                        PartyType.INDIVIDUAL.name(),
                        "Fictional Claimant",
                        DataConfidence.SELF_DECLARED.name(),
                        PartyVerificationStatus.PENDING.name(),
                        actorId,
                        "officer@example.test",
                        OffsetDateTime.now()));

        mvc.perform(post("/api/v1/parties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "partyType": "INDIVIDUAL",
                                  "displayName": "Fictional Claimant",
                                  "dataConfidence": "SELF_DECLARED"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffPartySearch() throws Exception {
        when(partyService.search("claimant")).thenReturn(List.of());

        mvc.perform(get("/api/v1/parties").param("query", "claimant"))
                .andExpect(status().isOk());
    }
}
