package gr.hua.dit.petcare.core.security;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repositories.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public ApplicationUserDetailsService(final UserRepository userRepository) {
        if (userRepository == null) throw new NullPointerException();
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        final User user = this.userRepository
            .findByUsername(username)
            .orElse(null);

        if (user == null) {
            throw new UsernameNotFoundException("User with username " + username + " does not exist");
        }

        return new ApplicationUserDetails(
            user.getId(),
            user.getUsername(),
            user.getPassword(),
            user.getRole()
        );
    }
}
