package cd.edrc.landgis.health;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
class PlatformInfoController {
    private final String disclaimer;

    PlatformInfoController(@Value("${edrc.public-mode-disclaimer}") String disclaimer) {
        this.disclaimer = disclaimer;
    }

    @GetMapping("/disclaimer")
    Map<String, String> disclaimer() {
        return Map.of("disclaimer", disclaimer);
    }
}
