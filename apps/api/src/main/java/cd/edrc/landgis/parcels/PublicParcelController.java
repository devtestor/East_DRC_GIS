package cd.edrc.landgis.parcels;

import java.util.Optional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/parcels")
class PublicParcelController {
    private final PublicParcelSearchService publicParcelSearch;

    PublicParcelController(PublicParcelSearchService publicParcelSearch) {
        this.publicParcelSearch = publicParcelSearch;
    }

    @GetMapping("/search")
    Optional<PublicParcelSummary> searchByUpi(@RequestParam String upi) {
        return publicParcelSearch.findByProposedUpi(upi);
    }
}
