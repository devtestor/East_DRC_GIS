package cd.edrc.landgis.pilot;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PilotReadinessRecordRepository extends JpaRepository<PilotReadinessRecord, UUID> {
    List<PilotReadinessRecord> findAllByOrderByCreatedAtDesc();
}
