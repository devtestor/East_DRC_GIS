package cd.edrc.landgis.governance;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentExportPackageRepository extends JpaRepository<DocumentExportPackage, UUID> {
    boolean existsByExportRequestId(UUID exportRequestId);

    Optional<DocumentExportPackage> findByExportRequestId(UUID exportRequestId);
}
