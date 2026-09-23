package cd.edrc.landgis.parcels;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface UpiSchemeVersionRepository extends JpaRepository<UpiSchemeVersion, UUID> {
    Optional<UpiSchemeVersion> findFirstBySchemeCodeAndStatusOrderByEffectiveFromDesc(String schemeCode, String status);
}
