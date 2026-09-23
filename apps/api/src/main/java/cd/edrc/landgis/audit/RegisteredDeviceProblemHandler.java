package cd.edrc.landgis.audit;

import java.net.URI;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class RegisteredDeviceProblemHandler {
    @ExceptionHandler(RegisteredDeviceNotFoundException.class)
    ProblemDetail handleNotFound(RegisteredDeviceNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/registered-device-not-found"));
        problem.setTitle("Registered device not found");
        return problem;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleConflict(DataIntegrityViolationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Registered device violates a database uniqueness or integrity constraint");
        problem.setType(URI.create("https://edrc-land-gis.local/problems/registered-device-conflict"));
        problem.setTitle("Registered device conflict");
        return problem;
    }
}
