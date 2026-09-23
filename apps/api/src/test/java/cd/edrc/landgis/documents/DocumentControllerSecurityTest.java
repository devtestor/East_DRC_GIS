package cd.edrc.landgis.documents;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.common.AuthenticatedActor;
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

@WebMvcTest(DocumentController.class)
@Import(SecurityConfig.class)
class DocumentControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private AuthenticatedActorResolver actorResolver;

    @Test
    void rejectsUnauthenticatedDocumentCreation() throws Exception {
        mvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffDocumentCreation() throws Exception {
        UUID documentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(documentService.create(any(CreateDocumentRequest.class), any(AuthenticatedActor.class)))
                .thenReturn(new DocumentResponse(
                        documentId,
                        "SURVEY_PLAN",
                        "parcel",
                        ownerId,
                        "Fictional survey plan",
                        "LEGAL_EVIDENCE",
                        "LEGAL_RECORD",
                        "workflow-task-and-authorized-staff",
                        null,
                        null,
                        false,
                        actor.userId(),
                        actor.username(),
                        java.time.OffsetDateTime.now(),
                        null,
                        List.of()));

        mvc.perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "documentType": "SURVEY_PLAN",
                                  "ownerType": "parcel",
                                  "ownerId": "%s",
                                  "title": "Fictional survey plan",
                                  "classification": "LEGAL_EVIDENCE",
                                  "retentionCategory": "LEGAL_RECORD",
                                  "accessPolicy": "workflow-task-and-authorized-staff",
                                  "legalHold": false,
                                  "objectStorageKey": "documents/fictional/survey-plan.pdf",
                                  "originalFilename": "survey-plan.pdf",
                                  "mediaType": "application/pdf",
                                  "sizeBytes": 128,
                                  "checksumSha256": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                                  "malwareScanStatus": "PENDING",
                                  "digitalSignatureStatus": "UNSIGNED"
                                }
                                """.formatted(ownerId)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "STAFF", username = "officer@example.test")
    void allowsStaffDocumentVersionAppend() throws Exception {
        UUID documentId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(actorResolver.requireActor("officer@example.test")).thenReturn(actor);
        when(documentService.addVersion(any(UUID.class), any(AddDocumentVersionRequest.class), any(AuthenticatedActor.class)))
                .thenReturn(new DocumentResponse(
                        documentId,
                        "SURVEY_PLAN",
                        "parcel",
                        ownerId,
                        "Fictional survey plan",
                        "LEGAL_EVIDENCE",
                        "LEGAL_RECORD",
                        "workflow-task-and-authorized-staff",
                        null,
                        null,
                        false,
                        actor.userId(),
                        actor.username(),
                        java.time.OffsetDateTime.now(),
                        null,
                        List.of()));

        mvc.perform(post("/api/v1/documents/{documentId}/versions", documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "objectStorageKey": "documents/fictional/survey-plan-v2.pdf",
                                  "originalFilename": "survey-plan-v2.pdf",
                                  "mediaType": "application/pdf",
                                  "sizeBytes": 192,
                                  "checksumSha256": "cccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccccc",
                                  "malwareScanStatus": "PENDING",
                                  "digitalSignatureStatus": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isOk());
    }
}
