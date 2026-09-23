package cd.edrc.landgis.pilot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PilotSignoffRepository extends JpaRepository<PilotSignoff, UUID> {
    List<PilotSignoff> findByPilotIdOrderByCreatedAtAsc(UUID pilotId);

    long countByPilotIdAndStatus(UUID pilotId, PilotSignoffStatus status);

    Optional<PilotSignoff> findByWorkflowTaskId(UUID workflowTaskId);
}
