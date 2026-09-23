package cd.edrc.landgis.parcels;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PublicParcelSummaryRepository extends JpaRepository<PublicParcelSummaryEntity, UUID> {
    Optional<PublicParcelSummaryEntity> findByProposedUpi(String proposedUpi);
}
