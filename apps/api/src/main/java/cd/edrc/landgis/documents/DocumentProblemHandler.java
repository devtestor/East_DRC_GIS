package cd.edrc.landgis.documents;

import java.net.URI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DocumentProblemHandler {
    @ExceptionHandler(DocumentNotFoundException.class)
    ProblemDetail handleNotFound(DocumentNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/document-not-found"));
        problem.setTitle("Document not found");
        return problem;
    }

    @ExceptionHandler(DocumentAccessDeniedException.class)
    ProblemDetail handleAccessDenied(DocumentAccessDeniedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/document-access-denied"));
        problem.setTitle("Document access denied");
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleConflict(DataIntegrityViolationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Document metadata violates a database uniqueness or integrity constraint");
        problem.setType(URI.create("https://edrc-land-gis.local/problems/document-metadata-conflict"));
        problem.setTitle("Document metadata conflict");
        return problem;
    }
}
