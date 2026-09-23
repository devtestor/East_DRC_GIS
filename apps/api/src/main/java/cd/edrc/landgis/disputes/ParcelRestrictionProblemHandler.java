package cd.edrc.landgis.disputes;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ParcelRestrictionProblemHandler {
    @ExceptionHandler(DisputeCaseNotFoundException.class)
    ProblemDetail disputeCaseNotFound(DisputeCaseNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Dispute case not found");
        return problem;
    }

    @ExceptionHandler(ParcelRestrictionNotFoundException.class)
    ProblemDetail notFound(ParcelRestrictionNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Parcel restriction not found");
        return problem;
    }

    @ExceptionHandler(ActiveParcelRestrictionException.class)
    ProblemDetail activeRestriction(ActiveParcelRestrictionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setTitle("Active parcel restriction");
        return problem;
    }
}
