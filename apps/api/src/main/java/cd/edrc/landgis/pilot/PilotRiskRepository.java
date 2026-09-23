package cd.edrc.landgis.pilot;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PilotRiskRepository extends JpaRepository<PilotRisk, UUID> {
    List<PilotRisk> findByPilotIdOrderByCreatedAtAsc(UUID pilotId);

    long countByPilotIdAndBlockingGoLiveTrueAndStatus(UUID pilotId, PilotRiskStatus status);
}
