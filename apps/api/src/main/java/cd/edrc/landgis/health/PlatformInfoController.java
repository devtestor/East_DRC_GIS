package cd.edrc.landgis.health;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
class PlatformInfoController {
    private final String disclaimer;
    private final boolean devSeedEnabled;

    PlatformInfoController(
            @Value("${edrc.public-mode-disclaimer}") String disclaimer,
            @Value("${edrc.dev-seed.enabled:false}") boolean devSeedEnabled) {
        this.disclaimer = disclaimer;
        this.devSeedEnabled = devSeedEnabled;
    }

    @GetMapping("/disclaimer")
    Map<String, String> disclaimer() {
        return Map.of("disclaimer", disclaimer);
    }

    @GetMapping("/pilot-readiness")
    PilotReadinessResponse pilotReadiness() {
        List<PilotReadinessCheck> checks = List.of(
                new PilotReadinessCheck(
                        "legal-boundary",
                        "READY",
                        "Public and staff surfaces identify the platform as a proposed workflow system, not an official registry."),
                new PilotReadinessCheck(
                        "human-approval",
                        "READY",
                        "Legal and cadastral changes require workflow tasks and maker-checker review before publication."),
                new PilotReadinessCheck(
                        "sandbox-integrations",
                        "READY",
                        "Identity and payment adapters are sandbox implementations until approved production credentials exist."),
                new PilotReadinessCheck(
                        "development-seed",
                        devSeedEnabled ? "ATTENTION" : "READY",
                        devSeedEnabled
                                ? "Development seed accounts are enabled and must be disabled before any controlled pilot environment."
                                : "Development seed accounts are disabled by default."),
                new PilotReadinessCheck(
                        "pilot-scope",
                        "ATTENTION",
                        "Pilot geography, approving institutions, support roster, RTO/RPO options and data-sharing authority require owner approval."));
        return new PilotReadinessResponse(
                "PILOT_PREPARATION",
                "Not approved for official registry operation",
                OffsetDateTime.now(ZoneOffset.UTC),
                disclaimer,
                checks);
    }

    record PilotReadinessResponse(
            String phase,
            String legalStatus,
            OffsetDateTime generatedAt,
            String disclaimer,
            List<PilotReadinessCheck> checks) {
    }

    record PilotReadinessCheck(String key, String status, String summary) {
    }
}
