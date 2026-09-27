package cd.edrc.landgis.governance;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DocumentExportRequestController.class)
class GovernanceProblemHandler {
    @ExceptionHandler(DocumentExportRequestNotFoundException.class)
    ProblemDetail notFound(DocumentExportRequestNotFoundException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        detail.setType(URI.create("https://api.edrc-land-gis.test/problems/governance/export-request-not-found"));
        detail.setTitle("Document export request not found");
        return detail;
    }

    @ExceptionHandler(DocumentExportReviewException.class)
    ProblemDetail reviewConflict(DocumentExportReviewException exception) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        detail.setType(URI.create("https://api.edrc-land-gis.test/problems/governance/export-review-conflict"));
        detail.setTitle("Document export review conflict");
        return detail;
    }
}
