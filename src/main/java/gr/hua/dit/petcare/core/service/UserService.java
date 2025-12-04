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
        // br έλεγχος αν υπάρχει ήδη ο χρήστης
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists.");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setRole(request.getRole());

        newUser.setPassword(passwordEncoder.encode(request.getRawPassword()));

        return userRepository.save(newUser);
    }
    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    public List<User> findAllUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }
}
