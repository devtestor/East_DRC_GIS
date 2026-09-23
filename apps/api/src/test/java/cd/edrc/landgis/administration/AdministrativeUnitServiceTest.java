package cd.edrc.landgis.administration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditService;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AdministrativeUnitServiceTest {
    @Test
    void rejectsDuplicateTypeAndCode() {
        AdministrativeUnitRepository repository = Mockito.mock(AdministrativeUnitRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(repository.findByUnitTypeAndCode(AdministrativeUnitType.PROVINCE, "NK-FICTIONAL"))
                .thenReturn(Optional.of(Mockito.mock(AdministrativeUnit.class)));

        AdministrativeUnitService service = new AdministrativeUnitService(repository, audit);

        assertThatThrownBy(() -> service.create(new CreateAdministrativeUnitRequest(
                null,
                AdministrativeUnitType.PROVINCE,
                "nk-fictional",
                "Nord-Kivu Fictional",
                LocalDate.of(2026, 1, 1))))
                .isInstanceOf(DuplicateAdministrativeUnitException.class);
    }

    @Test
    void normalizesAdministrativeUnitCode() {
        AdministrativeUnitRepository repository = Mockito.mock(AdministrativeUnitRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        when(repository.findByUnitTypeAndCode(AdministrativeUnitType.PROVINCE, "NK-FICTIONAL")).thenReturn(Optional.empty());
        when(repository.save(any(AdministrativeUnit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdministrativeUnitService service = new AdministrativeUnitService(repository, audit);

        AdministrativeUnitResponse response = service.create(new CreateAdministrativeUnitRequest(
                null,
                AdministrativeUnitType.PROVINCE,
                "nk-fictional",
                "Nord-Kivu Fictional",
                LocalDate.of(2026, 1, 1)));

        assertThat(response.code()).isEqualTo("NK-FICTIONAL");
    }
}
