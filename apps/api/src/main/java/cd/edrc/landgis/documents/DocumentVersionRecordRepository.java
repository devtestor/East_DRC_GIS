package cd.edrc.landgis.documents;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRecordRepository extends JpaRepository<DocumentVersionRecord, UUID> {
    List<DocumentVersionRecord> findByDocumentIdOrderByVersionNumberAsc(UUID documentId);

    int countByDocumentId(UUID documentId);
}
