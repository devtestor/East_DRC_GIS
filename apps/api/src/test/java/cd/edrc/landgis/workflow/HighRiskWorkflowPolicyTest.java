package cd.edrc.landgis.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class HighRiskWorkflowPolicyTest {
    private static final AuthenticatedActor MAKER = new AuthenticatedActor(
            UUID.fromString("10000000-0000-0000-0000-000000000201"),
            "maker@example.test");

    private final HighRiskWorkflowPolicy policy = new HighRiskWorkflowPolicy();

    @Test
    void catalogContainsKnownPrivilegedRegistryOperations() {
        assertThat(policy.rules())
                .extracting(HighRiskWorkflowPolicy.WorkflowPolicyRule::workflowType)
                .contains(
                        "PARCEL_GEOMETRY_APPROVAL",
                        "OWNERSHIP_INTEREST_REVIEW",
                        "PARCEL_RESTRICTION_RELEASE",
                        "DISPUTE_CASE_DECISION",
                        "REGISTERED_DEVICE_LIFECYCLE",
                        "PILOT_GO_NO_GO");
    }

    @Test
    void rejectsOpeningWhenRequiredRoleDoesNotMatchPolicy() {
        assertThatThrownBy(() -> policy.requireAllowedOpening(
                "PARCEL_GEOMETRY_APPROVAL",
                "parcel-geometry-version",
                "APPROVE_CURRENT_GEOMETRY",
                "PLATFORM_ADMIN"))
                .isInstanceOf(PrivilegedWorkflowPolicyViolationException.class)
                .hasMessageContaining("CADASTRAL_OFFICER");
    }

    @Test
    void rejectsApprovalForUnregisteredWorkflowTask() {
        WorkflowTask task = new WorkflowTask(
                UUID.randomUUID(),
                "UNREGISTERED_LEGAL_CHANGE",
                "parcel",
                UUID.randomUUID(),
                "MUTATE_WITHOUT_APPROVAL_POLICY",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");

        assertThatThrownBy(() -> policy.requireApprovalAllowed(task))
                .isInstanceOf(PrivilegedWorkflowPolicyViolationException.class)
                .hasMessageContaining("not registered");
    }

    @Test
    void allowsPilotSignoffActionsWithExplicitApprovePrefixAndRoleScope() {
        policy.requireAllowedOpening(
                "PILOT_SIGNOFF_REVIEW",
                "pilot-signoff",
                "APPROVE_SECURITY",
                "SECURITY_OFFICER");
    }
}

