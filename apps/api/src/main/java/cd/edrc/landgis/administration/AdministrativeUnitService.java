package cd.edrc.landgis.administration;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AdministrativeUnitService {
    private final AdministrativeUnitRepository administrativeUnits;
    private final AuditService auditService;

    AdministrativeUnitService(AdministrativeUnitRepository administrativeUnits, AuditService auditService) {
        this.administrativeUnits = administrativeUnits;
        this.auditService = auditService;
    }

    @Transactional
    AdministrativeUnitResponse create(CreateAdministrativeUnitRequest request) {
        if (request.parentId() != null && !administrativeUnits.existsById(request.parentId())) {
            throw new UnknownParentAdministrativeUnitException(request.parentId());
        }

        String normalizedCode = request.code().trim().toUpperCase(Locale.ROOT);
        administrativeUnits.findByUnitTypeAndCode(request.unitType(), normalizedCode).ifPresent(existing -> {
            throw new DuplicateAdministrativeUnitException(request.unitType(), normalizedCode);
        });

        AdministrativeUnit unit = administrativeUnits.save(new AdministrativeUnit(
                UUID.randomUUID(),
                request.parentId(),
                request.unitType(),
                normalizedCode,
                request.name().trim(),
                request.validFrom()));
        auditService.record("administration.unit-created", "administrative-unit", unit.id().toString(), AuditClassification.STAFF_OPERATIONAL);
        return new AdministrativeUnitResponse(unit.id(), unit.code(), unit.name());
    }

    @Transactional(readOnly = true)
    List<AdministrativeUnitResponse> findActive() {
        return administrativeUnits.findByStatusOrderByNameAsc(AdministrativeUnitStatus.ACTIVE).stream()
                .map(unit -> new AdministrativeUnitResponse(unit.id(), unit.code(), unit.name()))
                .toList();
    }
}
