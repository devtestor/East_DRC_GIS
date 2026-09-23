package cd.edrc.landgis.parcels;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class ParcelProblemHandler {
    @ExceptionHandler(DuplicateUpiException.class)
    ProblemDetail duplicateUpi(DuplicateUpiException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/duplicate-upi"));
        problem.setTitle("Duplicate proposed UPI");
        return problem;
    }

    @ExceptionHandler({UnknownAdministrativeUnitException.class, MissingUpiSchemeException.class})
    ProblemDetail invalidParcelReference(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/parcel-reference"));
        problem.setTitle("Parcel reference is not valid");
        return problem;
    }

    @ExceptionHandler(ParcelNotFoundException.class)
    ProblemDetail parcelNotFound(ParcelNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/parcel-not-found"));
        problem.setTitle("Parcel not found");
        return problem;
    }

    @ExceptionHandler(InvalidParcelStateTransitionException.class)
    ProblemDetail invalidTransition(InvalidParcelStateTransitionException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/invalid-parcel-state-transition"));
        problem.setTitle("Invalid parcel state transition");
        return problem;
    }

    @ExceptionHandler(InvalidParcelGeometryException.class)
    ProblemDetail invalidGeometry(InvalidParcelGeometryException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/invalid-parcel-geometry"));
        problem.setTitle("Invalid parcel geometry");
        return problem;
    }

    @ExceptionHandler(ParcelGeometryNotFoundException.class)
    ProblemDetail geometryNotFound(ParcelGeometryNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/parcel-geometry-not-found"));
        problem.setTitle("Parcel geometry not found");
        return problem;
    }

    @ExceptionHandler(InvalidParcelGeometryStateException.class)
    ProblemDetail invalidGeometryState(InvalidParcelGeometryStateException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/invalid-parcel-geometry-state"));
        problem.setTitle("Invalid parcel geometry state");
        return problem;
    }
}
