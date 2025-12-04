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
import org.springframework.security.access.prepost.PreAuthorize;
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

    private User getAuthenticatedUser(UserDetails principal) {
        return userService.findUserByUsername(principal.getUsername());
    }


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


    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_OWNER')")
    @ResponseStatus(HttpStatus.CREATED)
    public Appointment createAppointment(
        @Valid @RequestBody Appointment appointmentRequest,
        @AuthenticationPrincipal UserDetails principal
    ) {
        Long petId = appointmentRequest.getPet().getId();
        Long vetId = appointmentRequest.getVeterinarian().getId();

        User owner = getAuthenticatedUser(principal);
        User veterinarian = userService.findUserById(vetId);
        Pet pet = petService.findPetById(petId);

        if (!pet.getOwner().equals(owner)) {
            throw new IllegalArgumentException("Δεν επιτρέπεται η κράτηση για κατοικίδιο που δεν ανήκει στον χρήστη.");
        }

        appointmentRequest.setPet(pet);
        appointmentRequest.setVeterinarian(veterinarian);

        return appointmentService.createAppointment(appointmentRequest);
    }
}
