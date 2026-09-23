package cd.edrc.landgis.governance;

import java.util.List;

public record PrivacyExportDecision(
        boolean allowed,
        boolean redactionRequired,
        boolean approvalRequired,
        List<String> blockers) {
}
