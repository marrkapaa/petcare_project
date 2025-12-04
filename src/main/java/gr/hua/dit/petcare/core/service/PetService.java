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

    // νέο Pet
    public Pet registerNewPet(Pet pet, User owner) {
        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    // εύρεση Pet με βάση τον Owner
    public List<Pet> findPetsByOwner(User owner) {
        return petRepository.findByOwner(owner);
    }

    // εύρεση Pet με ID (για AppointmentController lookup)
    public Pet findPetById(Long id) {
        return petRepository.findById(id)
            .orElseThrow(() -> new PetNotFoundException("Pet not found with ID: " + id));
    }
}
