package cd.edrc.landgis.identity;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class IdentityProblemHandler {
    @ExceptionHandler(DuplicateUserException.class)
    ProblemDetail duplicateUser(DuplicateUserException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/duplicate-user"));
        problem.setTitle("Duplicate user");
        return problem;
    }
}
