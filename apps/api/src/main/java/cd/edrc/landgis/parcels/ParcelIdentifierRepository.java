package cd.edrc.landgis.parcels;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ParcelIdentifierRepository extends JpaRepository<ParcelIdentifier, UUID> {
    Optional<ParcelIdentifier> findByIdentifierTypeAndIdentifierValueAndStatus(
            IdentifierType identifierType,
            String identifierValue,
            IdentifierStatus status);

    Optional<ParcelIdentifier> findByParcelIdAndIdentifierTypeAndStatus(
            UUID parcelId,
            IdentifierType identifierType,
            IdentifierStatus status);
}
