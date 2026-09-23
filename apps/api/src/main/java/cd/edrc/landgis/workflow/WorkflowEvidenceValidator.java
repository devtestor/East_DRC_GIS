package cd.edrc.landgis.workflow;

import cd.edrc.landgis.documents.DocumentClassification;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import java.util.EnumSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkflowEvidenceValidator {
    private static final EnumSet<DocumentClassification> WORKFLOW_DOCUMENT_CLASSIFICATIONS = EnumSet.of(
            DocumentClassification.STAFF_OPERATIONAL,
            DocumentClassification.PROTECTED_PERSONAL,
            DocumentClassification.LEGAL_EVIDENCE,
            DocumentClassification.FINANCIAL);

    private final DocumentRecordRepository documents;

    WorkflowEvidenceValidator(DocumentRecordRepository documents) {
        this.documents = documents;
    }

    @Transactional(readOnly = true)
    public void validate(AddWorkflowTaskEvidenceRequest request) {
        if (request.evidenceType() != WorkflowEvidenceType.DOCUMENT) {
            return;
        }
        if (request.referenceId() == null) {
            throw new InvalidWorkflowEvidenceException("DOCUMENT evidence requires referenceId");
        }
        if (!"document".equalsIgnoreCase(request.referenceType())) {
            throw new InvalidWorkflowEvidenceException("DOCUMENT evidence referenceType must be document");
        }
        DocumentRecord document = documents.findById(request.referenceId())
                .orElseThrow(() -> new InvalidWorkflowEvidenceException("Referenced document does not exist"));
        if (!WORKFLOW_DOCUMENT_CLASSIFICATIONS.contains(document.classification())) {
            throw new InvalidWorkflowEvidenceException(
                    "Document classification is not permitted as workflow evidence: " + document.classification());
        }
    }
}
