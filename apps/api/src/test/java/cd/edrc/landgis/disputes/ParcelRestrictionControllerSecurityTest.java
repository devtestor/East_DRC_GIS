package cd.edrc.landgis.disputes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(ParcelRestrictionController.class)
@Import(SecurityConfig.class)
class ParcelRestrictionControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ParcelRestrictionService restrictionService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedRestrictionCreation() throws Exception {
        mvc.perform(post("/api/v1/parcels/{parcelId}/restrictions", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffRestrictionCreation() throws Exception {
        UUID parcelId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(restrictionService.create(eq(parcelId), any(CreateParcelRestrictionRequest.class), eq(actor)))
                .thenReturn(restriction(parcelId, actor));

        mvc.perform(post("/api/v1/parcels/{parcelId}/restrictions", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "restrictionType": "COURT_CAUTION",
                                  "source": "court-order",
                                  "authorityReference": "COURT-2026-001",
                                  "summary": "Court caution pending hearing",
                                  "blocksOwnershipChanges": true,
                                  "effectiveTo": null
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffRestrictionListing() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(restrictionService.listByParcel(parcelId)).thenReturn(List.of());

        mvc.perform(get("/api/v1/parcels/{parcelId}/restrictions", parcelId))
                .andExpect(status().isOk());
    }

    private static ParcelRestrictionResponse restriction(UUID parcelId, AuthenticatedActor actor) {
        return new ParcelRestrictionResponse(
                UUID.randomUUID(),
                parcelId,
                RestrictionType.COURT_CAUTION.name(),
                RestrictionStatus.ACTIVE.name(),
                "court-order",
                "COURT-2026-001",
                "Court caution pending hearing",
                true,
                OffsetDateTime.now(),
                null,
                actor.userId(),
                actor.username(),
                OffsetDateTime.now());
    }
}
