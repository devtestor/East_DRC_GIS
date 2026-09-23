package cd.edrc.landgis.common;

import java.util.UUID;

public record AuthenticatedActor(UUID userId, String username) {
}
