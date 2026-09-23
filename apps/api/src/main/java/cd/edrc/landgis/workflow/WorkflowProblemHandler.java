package cd.edrc.landgis.workflow;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WorkflowProblemHandler {
    @ExceptionHandler(WorkflowTaskNotFoundException.class)
    ProblemDetail handleNotFound(WorkflowTaskNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/workflow-task-not-found"));
        problem.setTitle("Workflow task not found");
        return problem;
    }

    @ExceptionHandler(WorkflowTaskNotOpenException.class)
    ProblemDetail handleNotOpen(WorkflowTaskNotOpenException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/workflow-task-not-open"));
        problem.setTitle("Workflow task is not open");
        return problem;
    }

    @ExceptionHandler(WorkflowTaskAssignmentRequiredException.class)
    ProblemDetail handleAssignmentRequired(WorkflowTaskAssignmentRequiredException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/workflow-task-assignment-required"));
        problem.setTitle("Workflow task assignment required");
        return problem;
    }

    @ExceptionHandler(WorkflowTaskAssignedToAnotherActorException.class)
    ProblemDetail handleAssignedToAnotherActor(WorkflowTaskAssignedToAnotherActorException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/workflow-task-assigned-to-another-actor"));
        problem.setTitle("Workflow task assigned to another actor");
        return problem;
    }

    @ExceptionHandler(MakerCheckerViolationException.class)
    ProblemDetail handleMakerCheckerViolation(MakerCheckerViolationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/maker-checker-violation"));
        problem.setTitle("Maker-checker separation required");
        return problem;
    }

    @ExceptionHandler(WorkflowRoleScopeViolationException.class)
    ProblemDetail handleRoleScopeViolation(WorkflowRoleScopeViolationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/workflow-role-scope-required"));
        problem.setTitle("Workflow role scope required");
        return problem;
    }

    @ExceptionHandler(WorkflowEvidenceRequiredException.class)
    ProblemDetail handleEvidenceRequired(WorkflowEvidenceRequiredException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/workflow-evidence-required"));
        problem.setTitle("Workflow evidence required");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/invalid-workflow-request"));
        problem.setTitle("Invalid workflow request");
        return problem;
    }

    @ExceptionHandler(InvalidWorkflowEvidenceException.class)
    ProblemDetail handleInvalidEvidence(InvalidWorkflowEvidenceException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/invalid-workflow-evidence"));
        problem.setTitle("Invalid workflow evidence");
        return problem;
    }

    @ExceptionHandler(UnsupportedWorkflowTaskException.class)
    ProblemDetail handleUnsupported(UnsupportedWorkflowTaskException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/unsupported-workflow-task"));
        problem.setTitle("Unsupported workflow task");
        return problem;
    }

    @ExceptionHandler(PrivilegedWorkflowPolicyViolationException.class)
    ProblemDetail handlePrivilegedWorkflowPolicyViolation(PrivilegedWorkflowPolicyViolationException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/privileged-workflow-policy-violation"));
        problem.setTitle("Privileged workflow policy violation");
        return problem;
    }
}
