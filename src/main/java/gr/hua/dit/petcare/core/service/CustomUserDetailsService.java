package gr.hua.dit.petcare.core.service;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repositories.UserRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Βρίσκουμε τον χρήστη από το Repository
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // 1. Δημιουργούμε το GrantedAuthority από το όνομα του ρόλου (π.χ. "OWNER")
        Collection<? extends GrantedAuthority> authorities =
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name()));

        // 2. Επιστρέφουμε το αντικείμενο UserDetails με το σωστό τύπο Authorities
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(), // Ο κωδικός που είναι ήδη κρυπτογραφημένος
                authorities // Τώρα στέλνουμε Collection<GrantedAuthority>
        );
    }
}
