package cd.edrc.landgis.parcels;

import cd.edrc.landgis.administration.AdministrativeUnit;
import org.springframework.stereotype.Service;

@Service
class PublicParcelProjectionService {
    private final PublicParcelSummaryRepository publicSummaries;

    PublicParcelProjectionService(PublicParcelSummaryRepository publicSummaries) {
        this.publicSummaries = publicSummaries;
    }

    void publishDraftSummary(Parcel parcel, ParcelIdentifier identifier, AdministrativeUnit unit) {
        publicSummaries.save(new PublicParcelSummaryEntity(
                parcel.id(),
                parcel.status().name(),
                identifier.identifierValue(),
                parcel.administrativeUnitId(),
                unit.name(),
                unit.unitType().name(),
                parcel.landUse(),
                parcel.tenureClassification()));
    }
}
