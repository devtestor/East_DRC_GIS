package cd.edrc.landgis.audit;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RegisteredDeviceRepository extends JpaRepository<RegisteredDevice, UUID> {
    Optional<RegisteredDevice> findByDeviceId(String deviceId);

    List<RegisteredDevice> findAllByOrderByCreatedAtDesc();
}
