package cd.edrc.landgis.identity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record RegisterUserRequest(
        @Email @NotBlank String email,
        @NotBlank @Size(max = 160) String displayName,
        @NotBlank @Size(min = 12, max = 256) String password) {
}
