package gr.hua.dit.petcare.core.repositories;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);
}
