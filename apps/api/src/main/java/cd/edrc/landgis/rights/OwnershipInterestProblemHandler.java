package cd.edrc.landgis.rights;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = OwnershipInterestController.class)
class OwnershipInterestProblemHandler {
    @ExceptionHandler(OwnershipInterestNotFoundException.class)
    ProblemDetail handleNotFound(OwnershipInterestNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Ownership interest not found");
        return problem;
    }
}
