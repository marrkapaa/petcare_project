package gr.hua.dit.petcare.core.service;

import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repositories.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    // 1. Καταχώριση νέου Pet
    public Pet registerNewPet(Pet pet, User owner) {
        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    // 2. Εύρεση Pet με βάση τον Owner (Authorization Rule)
    public List<Pet> findPetsByOwner(User owner) {
        return petRepository.findByOwner(owner);
    }

    // 3. Εύρεση Pet με ID (για AppointmentController lookup)
    public Pet findPetById(Long id) {
        return petRepository.findById(id)
            // Αλλαγή: Χρησιμοποιούμε custom exception
            .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + id));
    }
}
