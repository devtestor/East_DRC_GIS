package cd.edrc.landgis.governance;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentExportRequestRepository extends JpaRepository<DocumentExportRequest, UUID> {
    List<DocumentExportRequest> findByStatusOrderByRequestedAtAsc(ExportRequestStatus status);
}
