package cd.edrc.landgis.governance;

import java.util.List;

public record RetentionDispositionDecision(
        boolean eligibleForAutomatedDisposal,
        boolean approvalRequired,
        boolean archivalRequired,
        List<String> blockers) {
}
