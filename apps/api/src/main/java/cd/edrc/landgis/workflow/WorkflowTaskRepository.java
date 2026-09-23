package cd.edrc.landgis.workflow;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, UUID> {
    List<WorkflowTask> findByStatusOrderByCreatedAtAsc(WorkflowTaskStatus status);
}
