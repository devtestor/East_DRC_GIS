package cd.edrc.landgis.parcels;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParcelRepository extends JpaRepository<Parcel, UUID> {
    List<Parcel> findByAdministrativeUnitId(UUID administrativeUnitId);
}
