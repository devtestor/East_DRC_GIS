package cd.edrc.landgis.pilot;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.config.SecurityConfig;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import java.time.LocalDate;
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

@WebMvcTest(PilotReadinessController.class)
@Import(SecurityConfig.class)
class PilotReadinessControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PilotReadinessService pilotService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedPilotCreation() throws Exception {
        mvc.perform(post("/api/v1/pilots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffPilotCreation() throws Exception {
        UUID actorId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(actorId, "officer@example.test");
        UUID pilotId = UUID.randomUUID();
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(pilotService.create(any(CreatePilotReadinessRequest.class), eq(actor)))
                .thenReturn(new PilotReadinessResponse(
                        pilotId,
                        "Pilot",
                        "Fictional geography",
                        "Owner",
                        LocalDate.of(2026, 10, 1),
                        null,
                        PilotStatus.DRAFT.name(),
                        null,
                        null,
                        actor.username(),
                        null,
                        null,
                        OffsetDateTime.now(),
                        List.of(),
                        List.of(),
                        List.of()));

        mvc.perform(post("/api/v1/pilots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Pilot",
                                  "geographyScope": "Fictional geography",
                                  "pilotOwner": "Owner",
                                  "plannedStartDate": "2026-10-01"
                                }
                                """))
                .andExpect(status().isCreated());
    }
}
