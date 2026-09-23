package cd.edrc.landgis.administration;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnit, UUID> {
    Optional<AdministrativeUnit> findByUnitTypeAndCode(AdministrativeUnitType unitType, String code);

    List<AdministrativeUnit> findByStatusOrderByNameAsc(AdministrativeUnitStatus status);
}
