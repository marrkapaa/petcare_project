package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.service.PetService;
import gr.hua.dit.petcare.core.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
public class PetRestController {

    private final PetService petService;
    private final UserService userService;

    public PetRestController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;
    }

    // Βοηθητική μέθοδος για ανάκτηση του πλήρους User Entity από το Principal
    private User getAuthenticatedUser(UserDetails principal) {
        return userService.findUserByUsername(principal.getUsername());
    }

    /**
     * GET /api/pets : Επιστρέφει όλα τα κατοικίδια του συνδεδεμένου ιδιοκτήτη.
     * Απαιτείται ρόλος OWNER.
     */
    @GetMapping
    public ResponseEntity<List<Pet>> getAllPetsForOwner(@AuthenticationPrincipal UserDetails principal) {
        User owner = getAuthenticatedUser(principal);
        List<Pet> pets = petService.findPetsByOwner(owner);
        return ResponseEntity.ok(pets);
    }

    /**
     * POST /api/pets : Καταχωρεί ένα νέο κατοικίδιο.
     * Απαιτείται ρόλος OWNER.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pet createPet(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @RequestBody Pet pet // Χρήση Pet Entity/Model
    ) {
        User owner = getAuthenticatedUser(principal);
        // Εδώ χρησιμοποιούμε το Pet Entity για απλότητα στο REST API (Ε6)
        return petService.registerNewPet(pet, owner);
    }

    /**
     * GET /api/pets/{id} : Επιστρέφει ένα συγκεκριμένο κατοικίδιο (με έλεγχο ιδιοκτησίας).
     * Απαιτείται ρόλος OWNER.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        User owner = getAuthenticatedUser(principal);
        Pet pet = petService.findPetById(id);

        // Authorization check: Ελέγχουμε αν το Pet ανήκει στον συνδεδεμένο Owner
        if (!pet.getOwner().equals(owner)) {
            // Αν δεν ανήκει, επιστρέφουμε 403 Forbidden ή 404 Not Found (για λόγους ασφάλειας)
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(pet);
    }
}
