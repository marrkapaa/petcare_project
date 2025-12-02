package gr.hua.dit.petcare.core.service;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.Role;
import gr.hua.dit.petcare.core.repositories.UserRepository;
import gr.hua.dit.petcare.core.service.model.UserRegistrationRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(UserRegistrationRequest request) {
        // Business Rule: Έλεγχος αν υπάρχει ήδη ο χρήστης
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        // Δημιουργία User Entity
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setRole(request.getRole());

        // Κρυπτογράφηση κωδικού (Ασφάλεια)
        newUser.setPassword(passwordEncoder.encode(request.getRawPassword()));

        return userRepository.save(newUser);
    }
    public User findUserByUsername(String username) {
        // Διατηρούμε το UsernameNotFoundException του Spring Security (Security standard)
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // 1. ΝΕΑ ΜΕΘΟΔΟΣ: Εύρεση User με ID (για AppointmentController lookup)
    public User findUserById(Long id) {
        return userRepository.findById(id)
            // Αλλαγή: Χρησιμοποιούμε custom exception
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    // 2. ΝΕΑ ΜΕΘΟΔΟΣ: Εύρεση όλων των Vets (για το Appointment Form)
    public List<User> findAllUsersByRole(Role role) {
        // Χρησιμοποιεί την custom query findByRole(Role role) στο UserRepository
        return userRepository.findByRole(role);
    }
}
