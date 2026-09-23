package cd.edrc.landgis.workflow;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.documents.DocumentClassification;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WorkflowEvidenceValidatorTest {
    @Test
    void allowsLegalEvidenceDocumentReference() {
        UUID documentId = UUID.randomUUID();
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document(documentId, DocumentClassification.LEGAL_EVIDENCE)));
        WorkflowEvidenceValidator validator = new WorkflowEvidenceValidator(documents);

        validator.validate(new AddWorkflowTaskEvidenceRequest(
                WorkflowEvidenceType.DOCUMENT,
                "document",
                documentId,
                null,
                "Survey plan evidence"));
    }

    @Test
    void rejectsDocumentEvidenceWithoutDocumentId() {
        WorkflowEvidenceValidator validator = new WorkflowEvidenceValidator(Mockito.mock(DocumentRecordRepository.class));

        assertThatThrownBy(() -> validator.validate(new AddWorkflowTaskEvidenceRequest(
                WorkflowEvidenceType.DOCUMENT,
                "document",
                null,
                "external-only",
                "External document evidence")))
                .isInstanceOf(InvalidWorkflowEvidenceException.class);
    }

    @Test
    void rejectsPublicDocumentAsWorkflowEvidence() {
        UUID documentId = UUID.randomUUID();
        DocumentRecordRepository documents = Mockito.mock(DocumentRecordRepository.class);
        when(documents.findById(documentId)).thenReturn(Optional.of(document(documentId, DocumentClassification.PUBLIC)));
        WorkflowEvidenceValidator validator = new WorkflowEvidenceValidator(documents);

        assertThatThrownBy(() -> validator.validate(new AddWorkflowTaskEvidenceRequest(
                WorkflowEvidenceType.DOCUMENT,
                "document",
                documentId,
                null,
                "Public brochure is not legal evidence")))
                .isInstanceOf(InvalidWorkflowEvidenceException.class);
    }

    private DocumentRecord document(UUID id, DocumentClassification classification) {
        return new DocumentRecord(
                id,
                "SURVEY_PLAN",
                "parcel",
                UUID.randomUUID(),
                "Fictional survey plan",
                classification,
                "LEGAL_RECORD",
                "workflow-task-and-authorized-staff",
                null,
                null,
                false,
                UUID.randomUUID(),
                "officer@example.test",
                null);
    }
}
