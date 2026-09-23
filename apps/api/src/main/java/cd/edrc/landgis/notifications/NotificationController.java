package cd.edrc.landgis.notifications;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController {
    private final NotificationService notifications;
    private final AuthenticatedActorResolver actors;

    NotificationController(NotificationService notifications, AuthenticatedActorResolver actors) {
        this.notifications = notifications;
        this.actors = actors;
    }

    @GetMapping
    List<NotificationResponse> list(Principal principal) {
        return notifications.listForActor(actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{notificationId}/read")
    ResponseEntity<Void> markRead(@PathVariable UUID notificationId, Principal principal) {
        notifications.markRead(notificationId, actors.requireActor(principal == null ? null : principal.getName()));
        return ResponseEntity.noContent().build();
    }
}
