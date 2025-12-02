package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.Appointment;
import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.service.AppointmentService;
import gr.hua.dit.petcare.core.service.PetService;
import gr.hua.dit.petcare.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Για πιο λεπτομερή έλεγχο ρόλων
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentRestController {

    private final AppointmentService appointmentService;
    private final UserService userService;
    private final PetService petService;

    public AppointmentRestController(AppointmentService appointmentService, UserService userService, PetService petService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.petService = petService;
    }

    // Βοηθητική μέθοδος για ανάκτηση του πλήρους User Entity
    private User getAuthenticatedUser(UserDetails principal) {
        return userService.findUserByUsername(principal.getUsername());
    }

    /**
     * GET /api/appointments : Επιστρέφει τα ραντεβού ανάλογα με τον ρόλο.
     * Owner: τα δικά του ραντεβού. Vet: το δικό του πρόγραμμα.
     */
    @GetMapping
    public ResponseEntity<List<Appointment>> getAppointments(@AuthenticationPrincipal UserDetails principal) {
        User user = getAuthenticatedUser(principal);

        if (user.getRole().name().equals("OWNER")) {
            return ResponseEntity.ok(appointmentService.findAppointmentsByOwner(user));
        } else if (user.getRole().name().equals("VETERINARIAN")) {
            return ResponseEntity.ok(appointmentService.findAppointmentsByVeterinarian(user));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * POST /api/appointments : Κλείσιμο νέου ραντεβού (Μόνο για OWNER).
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_OWNER')") // Ελέγχουμε ρητά το ρόλο
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment createAppointment(
        @Valid @RequestBody Appointment appointmentRequest,
        @AuthenticationPrincipal UserDetails principal
    ) {
        // Η φόρμα/client πρέπει να στείλει τα IDs του Pet και του Vet μέσα στο appointmentRequest
        Long petId = appointmentRequest.getPet().getId();
        Long vetId = appointmentRequest.getVeterinarian().getId();

        // 1. Βρίσκουμε τα πλήρη Entities για την Business Logic (Authorization & Rules)
        User owner = getAuthenticatedUser(principal);
        User veterinarian = userService.findUserById(vetId);
        Pet pet = petService.findPetById(petId);

        // Authorization check: Ο OWNER μπορεί να κλείσει ραντεβού μόνο για δικό του Pet
        if (!pet.getOwner().equals(owner)) {
            throw new IllegalArgumentException("Δεν επιτρέπεται η κράτηση για κατοικίδιο που δεν ανήκει στον χρήστη.");
        }

        // 2. Θέτουμε τα πλήρη Entities (Pet, Vet)
        appointmentRequest.setPet(pet);
        appointmentRequest.setVeterinarian(veterinarian);

        // 3. Καλούμε το Service (Εφαρμόζεται ο κανόνας επικαλύψεων & καλείται η External Service)
        return appointmentService.createAppointment(appointmentRequest);
    }
}
