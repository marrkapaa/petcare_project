package gr.hua.dit.petcare.core.service;

import gr.hua.dit.petcare.core.model.Role;
import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long saveUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Το Username υπάρχει ήδη.");
        }


        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == null) {
            user.setRole(Role.OWNER); 
        }

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Ο χρήστης δεν βρέθηκε: " + username));
    }
    
    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ο χρήστης δεν βρέθηκε με ID: " + id));
    }
}
