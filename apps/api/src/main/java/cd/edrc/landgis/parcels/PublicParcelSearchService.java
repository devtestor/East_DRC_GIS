package cd.edrc.landgis.parcels;

import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PublicParcelSearchService {
    private final PublicParcelSummaryRepository publicSummaries;

    PublicParcelSearchService(PublicParcelSummaryRepository publicSummaries) {
        this.publicSummaries = publicSummaries;
    }

    @Transactional(readOnly = true)
    Optional<PublicParcelSummary> findByProposedUpi(String proposedUpi) {
        if (proposedUpi == null || proposedUpi.isBlank()) {
            return Optional.empty();
        }

        String normalizedUpi = proposedUpi.trim().toUpperCase(Locale.ROOT);
        return publicSummaries.findByProposedUpi(normalizedUpi).map(PublicParcelSummaryEntity::toSummary);
    }
}
