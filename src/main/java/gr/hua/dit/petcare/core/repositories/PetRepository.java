package gr.hua.dit.petcare.core.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import gr.hua.dit.petcare.core.model.Pet;
import gr.hua.dit.petcare.core.model.User;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByOwner(User owner);
}
