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

    private User getAuthenticatedUser(UserDetails principal) {
        return userService.findUserByUsername(principal.getUsername());
    }

    @GetMapping
    public ResponseEntity<List<Pet>> getAllPetsForOwner(@AuthenticationPrincipal UserDetails principal) {
        User owner = getAuthenticatedUser(principal);
        List<Pet> pets = petService.findPetsByOwner(owner);
        return ResponseEntity.ok(pets);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pet createPet(
        @AuthenticationPrincipal UserDetails principal,
        @Valid @RequestBody Pet pet
    ) {
        User owner = getAuthenticatedUser(principal);
        return petService.registerNewPet(pet, owner);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pet> getPetById(@PathVariable Long id, @AuthenticationPrincipal UserDetails principal) {
        User owner = getAuthenticatedUser(principal);
        Pet pet = petService.findPetById(id);

        if (!pet.getOwner().equals(owner)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(pet);
    }
}
