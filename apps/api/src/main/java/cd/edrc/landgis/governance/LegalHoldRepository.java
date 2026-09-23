package cd.edrc.landgis.governance;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalHoldRepository extends JpaRepository<LegalHold, UUID> {
    boolean existsByTargetTypeAndTargetIdAndStatus(String targetType, UUID targetId, LegalHoldStatus status);

    Optional<LegalHold> findByTargetTypeAndTargetIdAndStatus(String targetType, UUID targetId, LegalHoldStatus status);
}
