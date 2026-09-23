package cd.edrc.landgis.parties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePartyRequest(
        @NotNull PartyType partyType,
        @NotBlank @Size(max = 240) String displayName,
        @NotNull DataConfidence dataConfidence) {
}
