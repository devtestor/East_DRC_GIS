package cd.edrc.landgis.health;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PlatformInfoControllerTest {
    @Test
    void exposesLegalBoundaryDisclaimer() {
        PlatformInfoController controller = new PlatformInfoController("proposed system only");

        Map<String, String> response = controller.disclaimer();

        assertThat(response.get("disclaimer")).contains("proposed");
    }
}
