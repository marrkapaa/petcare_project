package gr.hua.dit.petcare.core.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@Entity
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Το όνομα είναι υποχρεωτικό.") // ΠΡΟΣΘΗΚΗ
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Το είδος είναι υποχρεωτικό.") // ΠΡΟΣΘΗΚΗ
    @Column(nullable = false)
    private String species;

    private String breed;

    @Min(value = 0, message = "Η ηλικία δεν μπορεί να είναι αρνητική.") // ΠΡΟΣΘΗΚΗ
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    public Pet() {
    }

    // --- GETTERS & SETTERS ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    // --- EQUALS & HASHCODE (Βασισμένα στο id) ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return id != null && Objects.equals(id, pet.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
