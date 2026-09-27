package cd.edrc.landgis.documents;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRecordRepository extends JpaRepository<DocumentVersionRecord, UUID> {
    List<DocumentVersionRecord> findByDocumentIdOrderByVersionNumberAsc(UUID documentId);

    Optional<DocumentVersionRecord> findByIdAndDocumentId(UUID id, UUID documentId);

    int countByDocumentId(UUID documentId);
}
