package cd.edrc.landgis.pilot;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PilotOperationalGateRepository extends JpaRepository<PilotOperationalGate, UUID> {
    List<PilotOperationalGate> findByPilotIdOrderByCreatedAtAsc(UUID pilotId);
}
