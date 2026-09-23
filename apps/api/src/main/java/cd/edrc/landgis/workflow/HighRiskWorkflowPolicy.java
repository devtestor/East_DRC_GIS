package cd.edrc.landgis.workflow;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HighRiskWorkflowPolicy {
    private static final String ANY = "*";

    private final List<WorkflowPolicyRule> rules = List.of(
            new WorkflowPolicyRule("PARCEL_STATUS_TRANSITION", "parcel", ANY, "CADASTRAL_OFFICER", true),
            new WorkflowPolicyRule("REGISTERED_DEVICE_LIFECYCLE", "registered-device", ANY, "SECURITY_OFFICER", true),
            new WorkflowPolicyRule("PARCEL_GEOMETRY_APPROVAL", "parcel-geometry-version", "APPROVE_CURRENT_GEOMETRY", "CADASTRAL_OFFICER", true),
            new WorkflowPolicyRule("OWNERSHIP_INTEREST_REVIEW", "ownership-interest", "VERIFY_OWNERSHIP_INTEREST", "LAND_TITLE_OFFICER", true),
            new WorkflowPolicyRule("PARCEL_RESTRICTION_RELEASE", "parcel-restriction", "RELEASE_RESTRICTION", "LAND_TITLE_OFFICER", true),
            new WorkflowPolicyRule("DISPUTE_CASE_REVIEW", "dispute-case", "START_DISPUTE_REVIEW", "LAND_TITLE_OFFICER", true),
            new WorkflowPolicyRule("DISPUTE_CASE_DECISION", "dispute-case", "RECORD_DISPUTE_DECISION", "LAND_TITLE_OFFICER", true),
            new WorkflowPolicyRule("DISPUTE_CASE_REOPEN", "dispute-case", "REOPEN_DISPUTE_CASE", "LAND_TITLE_OFFICER", true),
            new WorkflowPolicyRule("PARCEL_INFORMATION_REQUEST_REVIEW", "parcel-information-application", "APPROVE_PARCEL_INFORMATION_REQUEST", "LAND_TITLE_OFFICER", false),
            new WorkflowPolicyRule("PILOT_SIGNOFF_REVIEW", "pilot-signoff", "APPROVE_*", ANY, true),
            new WorkflowPolicyRule("PILOT_GO_NO_GO", "pilot-readiness-record", "APPROVE_PILOT_GO", "PROVINCIAL_LAND_ADMINISTRATOR", true));

    public void requireAllowedOpening(String workflowType, String targetType, String requestedAction, String assignedToRole) {
        WorkflowPolicyRule rule = findRule(workflowType, targetType, requestedAction);
        if (rule == null) {
            throw new PrivilegedWorkflowPolicyViolationException(
                    "Workflow action is not registered in the privileged workflow policy: " + workflowType + "/" + requestedAction);
        }
        if (!rule.roleMatches(assignedToRole)) {
            throw new PrivilegedWorkflowPolicyViolationException(
                    "Workflow action requires role " + rule.requiredRole() + " but was assigned to " + assignedToRole);
        }
    }

    public void requireApprovalAllowed(WorkflowTask task) {
        WorkflowPolicyRule rule = findRule(task.workflowType(), task.targetType(), task.requestedAction());
        if (rule == null) {
            throw new PrivilegedWorkflowPolicyViolationException(
                    "Workflow task cannot be approved because its action is not registered in the privileged workflow policy: "
                            + task.workflowType() + "/" + task.requestedAction());
        }
        if (!rule.roleMatches(task.assignedToRole())) {
            throw new PrivilegedWorkflowPolicyViolationException(
                    "Workflow task cannot be approved because it is assigned to "
                            + task.assignedToRole() + " but policy requires " + rule.requiredRole());
        }
    }

    public List<WorkflowPolicyRule> rules() {
        return rules;
    }

    private WorkflowPolicyRule findRule(String workflowType, String targetType, String requestedAction) {
        return rules.stream()
                .filter(rule -> rule.matches(workflowType, targetType, requestedAction))
                .findFirst()
                .orElse(null);
    }

    public record WorkflowPolicyRule(
            String workflowType,
            String targetType,
            String requestedActionPattern,
            String requiredRole,
            boolean privileged) {
        boolean matches(String workflowType, String targetType, String requestedAction) {
            return this.workflowType.equals(workflowType)
                    && this.targetType.equals(targetType)
                    && actionMatches(requestedAction);
        }

        boolean roleMatches(String assignedToRole) {
            return ANY.equals(requiredRole) || requiredRole.equals(assignedToRole);
        }

        private boolean actionMatches(String requestedAction) {
            if (ANY.equals(requestedActionPattern)) {
                return true;
            }
            if (requestedActionPattern.endsWith("*")) {
                return requestedAction != null
                        && requestedAction.startsWith(requestedActionPattern.substring(0, requestedActionPattern.length() - 1));
            }
            return requestedActionPattern.equals(requestedAction);
        }
    }
}

