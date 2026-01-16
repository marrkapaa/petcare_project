package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.ports.VaccinePort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vaccines")
public class VaccineRestController {

    private final VaccinePort vaccinePort;

    public VaccineRestController(VaccinePort vaccinePort) {
        this.vaccinePort = vaccinePort;
    }

    /**
     * GET /api/vaccines?species=Dog
     * Καλεί την εξωτερική υπηρεσία (μέσω του Adapter) και φέρνει τη λίστα εμβολίων.
     * Προσβάσιμο μόνο από Κτηνιάτρους.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_VETERINARIAN')")
    public ResponseEntity<List<String>> getVaccines(@RequestParam String species) {
        List<String> vaccines = vaccinePort.getAvailableVaccines(species);
        return ResponseEntity.ok(vaccines);
    }
}
