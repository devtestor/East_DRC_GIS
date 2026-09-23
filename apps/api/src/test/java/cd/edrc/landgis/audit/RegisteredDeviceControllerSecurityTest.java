package cd.edrc.landgis.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.config.SecurityConfig;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RegisteredDeviceController.class)
@Import(SecurityConfig.class)
class RegisteredDeviceControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private RegisteredDeviceService deviceService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedDeviceEnrollment() throws Exception {
        mvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffDeviceEnrollment() throws Exception {
        UUID actorId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(actorId, "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(deviceService.enroll(any(CreateRegisteredDeviceRequest.class), eq(actor)))
                .thenReturn(new RegisteredDeviceResponse(
                        UUID.randomUUID(),
                        "FIELD-TABLET-010",
                        actorId,
                        null,
                        RegisteredDeviceType.FIELD_MOBILE.name(),
                        RegisteredDeviceStatus.ACTIVE.name(),
                        OffsetDateTime.now(),
                        null,
                        null,
                        OffsetDateTime.now(),
                        OffsetDateTime.now()));

        mvc.perform(post("/api/v1/devices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "deviceId": "FIELD-TABLET-010",
                                  "assignedUserId": "%s",
                                  "deviceType": "FIELD_MOBILE"
                                }
                                """.formatted(actorId)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffDeviceRevocationRequest() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(actorId, "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(deviceService.revoke("FIELD-TABLET-010", actor))
                .thenReturn(new RegisteredDeviceLifecycleResponse(
                        new RegisteredDeviceResponse(
                                UUID.randomUUID(),
                                "FIELD-TABLET-010",
                                actorId,
                                null,
                                RegisteredDeviceType.FIELD_MOBILE.name(),
                                RegisteredDeviceStatus.ACTIVE.name(),
                                OffsetDateTime.now(),
                                null,
                                null,
                                OffsetDateTime.now(),
                                OffsetDateTime.now()),
                        "REVOKE",
                        "OPEN",
                        taskId));

        mvc.perform(post("/api/v1/devices/{deviceId}/revoke", "FIELD-TABLET-010"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffDeviceLifecycleDecision() throws Exception {
        UUID actorId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(actorId, "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(deviceService.decideLifecycleTask(eq(taskId), any(), eq(actor)))
                .thenReturn(new RegisteredDeviceLifecycleResponse(
                        new RegisteredDeviceResponse(
                                UUID.randomUUID(),
                                "FIELD-TABLET-010",
                                actorId,
                                null,
                                RegisteredDeviceType.FIELD_MOBILE.name(),
                                RegisteredDeviceStatus.REVOKED.name(),
                                OffsetDateTime.now(),
                                null,
                                OffsetDateTime.now(),
                                OffsetDateTime.now(),
                                OffsetDateTime.now()),
                        "REVOKE",
                        "APPROVED",
                        taskId));

        mvc.perform(post("/api/v1/devices/tasks/{taskId}/decisions", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "decision": "APPROVE",
                                  "reason": "Reviewed security evidence"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
