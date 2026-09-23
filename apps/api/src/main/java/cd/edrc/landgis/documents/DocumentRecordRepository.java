package cd.edrc.landgis.documents;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRecordRepository extends JpaRepository<DocumentRecord, UUID> {
    List<DocumentRecord> findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(String ownerType, UUID ownerId);
}
