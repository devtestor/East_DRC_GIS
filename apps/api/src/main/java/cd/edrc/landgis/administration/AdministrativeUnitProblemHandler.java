package cd.edrc.landgis.administration;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class AdministrativeUnitProblemHandler {
    @ExceptionHandler(DuplicateAdministrativeUnitException.class)
    ProblemDetail duplicateAdministrativeUnit(DuplicateAdministrativeUnitException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/duplicate-administrative-unit"));
        problem.setTitle("Duplicate administrative unit");
        return problem;
    }

    @ExceptionHandler(UnknownParentAdministrativeUnitException.class)
    ProblemDetail unknownParent(UnknownParentAdministrativeUnitException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/administrative-unit-parent"));
        problem.setTitle("Administrative unit parent is not valid");
        return problem;
    }
}
