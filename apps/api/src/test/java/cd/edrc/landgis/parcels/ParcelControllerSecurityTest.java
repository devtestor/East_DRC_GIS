package cd.edrc.landgis.parcels;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.config.SecurityConfig;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ParcelController.class)
@Import(SecurityConfig.class)
class ParcelControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ParcelRegistryService parcelRegistryService;

    @MockBean
    private ParcelGeometryService parcelGeometryService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedParcelCreation() throws Exception {
        mvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffParcelCreation() throws Exception {
        UUID adminUnitId = UUID.randomUUID();
        when(parcelRegistryService.createDraft(any(CreateParcelRequest.class)))
                .thenReturn(new ParcelResponse(UUID.randomUUID(), "DRAFT", adminUnitId, "NK-DEM-000002"));

        mvc.perform(post("/api/v1/parcels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "administrativeUnitId": "%s",
                                  "proposedUpi": "NK-DEM-000002",
                                  "landUse": "Residential",
                                  "tenureClassification": "Customary claim - unverified"
                                }
                                """.formatted(adminUnitId)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffParcelStatusTransition() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(actorResolver.requireActor("user")).thenReturn(new AuthenticatedActor(UUID.randomUUID(), "user"));
        when(parcelRegistryService.transitionStatus(any(UUID.class), any(TransitionParcelStatusRequest.class), any(AuthenticatedActor.class)))
                .thenReturn(new ParcelTransitionResponse(parcelId, "UNDER_SURVEY", "UNDER_SURVEY", "APPLIED", null));

        mvc.perform(patch("/api/v1/parcels/{parcelId}/status", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "targetStatus": "UNDER_SURVEY"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsUnauthenticatedDraftGeometryCreation() throws Exception {
        mvc.perform(post("/api/v1/parcels/{parcelId}/geometries", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffDraftGeometryCreation() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(parcelGeometryService.createDraftGeometry(any(UUID.class), any(CreateParcelGeometryVersionRequest.class)))
                .thenReturn(new ParcelGeometryVersionResponse(
                        UUID.randomUUID(),
                        parcelId,
                        "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                        "DRAFT",
                        new BigDecimal("123.45"),
                        "survey-plan-demo",
                        null,
                        null,
                        null,
                        null,
                        OffsetDateTime.now()));

        mvc.perform(post("/api/v1/parcels/{parcelId}/geometries", parcelId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "geometryWkt": "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                                  "source": "survey-plan-demo"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffGeometryHistoryRead() throws Exception {
        UUID parcelId = UUID.randomUUID();
        when(parcelGeometryService.listGeometryVersions(parcelId)).thenReturn(List.of());

        mvc.perform(get("/api/v1/parcels/{parcelId}/geometries", parcelId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffGeometryApprovalRequest() throws Exception {
        UUID parcelId = UUID.randomUUID();
        UUID geometryId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(parcelGeometryService.requestApproval(eq(parcelId), eq(geometryId), eq(actor)))
                .thenReturn(new ParcelGeometryApprovalResponse(
                        new ParcelGeometryVersionResponse(
                                geometryId,
                                parcelId,
                                "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                                "DRAFT",
                                new BigDecimal("123.45"),
                                "survey-plan-demo",
                                null,
                                null,
                                null,
                                null,
                                OffsetDateTime.now()),
                        "APPROVE_CURRENT_GEOMETRY",
                        "OPEN",
                        taskId));

        mvc.perform(post("/api/v1/parcels/{parcelId}/geometries/{geometryVersionId}/approval-requests", parcelId, geometryId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffGeometryApprovalDecision() throws Exception {
        UUID parcelId = UUID.randomUUID();
        UUID geometryId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(parcelGeometryService.decideApprovalTask(eq(taskId), any(), eq(actor)))
                .thenReturn(new ParcelGeometryApprovalResponse(
                        new ParcelGeometryVersionResponse(
                                geometryId,
                                parcelId,
                                "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                                "APPROVED",
                                new BigDecimal("123.45"),
                                "survey-plan-demo",
                                OffsetDateTime.now(),
                                null,
                                actor.userId(),
                                OffsetDateTime.now(),
                                OffsetDateTime.now()),
                        "APPROVE_CURRENT_GEOMETRY",
                        "APPROVED",
                        taskId));

        mvc.perform(post("/api/v1/parcels/geometries/tasks/{taskId}/decisions", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Cadastral review complete"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffParcelReadById() throws Exception {
        UUID parcelId = UUID.randomUUID();
        UUID adminUnitId = UUID.randomUUID();
        when(parcelRegistryService.getParcel(parcelId))
                .thenReturn(new ParcelResponse(parcelId, "DRAFT", adminUnitId, "NK-DEM-000002"));

        mvc.perform(get("/api/v1/parcels/{parcelId}", parcelId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void allowsStaffParcelSearchByUpi() throws Exception {
        UUID parcelId = UUID.randomUUID();
        UUID adminUnitId = UUID.randomUUID();
        when(parcelRegistryService.findByProposedUpi(eq("NK-DEM-000002")))
                .thenReturn(java.util.Optional.of(new ParcelResponse(parcelId, "DRAFT", adminUnitId, "NK-DEM-000002")));

        mvc.perform(get("/api/v1/parcels/search").param("upi", "NK-DEM-000002"))
                .andExpect(status().isOk());
    }
}
