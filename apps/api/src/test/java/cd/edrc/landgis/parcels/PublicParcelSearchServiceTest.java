package cd.edrc.landgis.parcels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PublicParcelSearchServiceTest {
    @Test
    void returnsDataMinimizedParcelSummaryByUpi() {
        PublicParcelSummaryRepository publicSummaries = Mockito.mock(PublicParcelSummaryRepository.class);
        UUID parcelId = UUID.randomUUID();
        UUID adminUnitId = UUID.randomUUID();
        PublicParcelSummaryEntity summary = new PublicParcelSummaryEntity(
                parcelId,
                "DRAFT",
                "NK-DEM-000003",
                adminUnitId,
                "Commune fictive publique",
                "COMMUNE",
                "Residential",
                "Customary claim - unverified");

        when(publicSummaries.findByProposedUpi("NK-DEM-000003")).thenReturn(Optional.of(summary));

        PublicParcelSearchService service = new PublicParcelSearchService(publicSummaries);

        Optional<PublicParcelSummary> result = service.findByProposedUpi(" nk-dem-000003 ");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().proposedUpi()).isEqualTo("NK-DEM-000003");
        assertThat(result.orElseThrow().administrativeUnitName()).isEqualTo("Commune fictive publique");
    }
}
