package cd.edrc.landgis.pilot;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PilotEvidenceRepository extends JpaRepository<PilotEvidence, UUID> {
    List<PilotEvidence> findByPilotIdOrderByAddedAtDesc(UUID pilotId);
}
