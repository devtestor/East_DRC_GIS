package cd.edrc.landgis.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTaskEvidenceLinkRepository extends JpaRepository<WorkflowTaskEvidenceLink, UUID> {
    long countByTaskId(UUID taskId);

    List<WorkflowTaskEvidenceLink> findByTaskIdOrderByAddedAtAsc(UUID taskId);
}
