package cd.edrc.landgis.health;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PlatformInfoControllerTest {
    @Test
    void exposesLegalBoundaryDisclaimer() {
        PlatformInfoController controller = new PlatformInfoController("proposed system only", false);

        Map<String, String> response = controller.disclaimer();

        assertThat(response.get("disclaimer")).contains("proposed");
    }

    @Test
    void exposesPilotReadinessWithoutOfficialRegistryClaims() {
        PlatformInfoController controller = new PlatformInfoController("proposed system only", false);

        PlatformInfoController.PilotReadinessResponse response = controller.pilotReadiness();

        assertThat(response.phase()).isEqualTo("PILOT_PREPARATION");
        assertThat(response.legalStatus()).contains("Not approved");
        assertThat(response.checks()).extracting("key")
                .contains("legal-boundary", "human-approval", "sandbox-integrations", "pilot-scope");
    }
}
