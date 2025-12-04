package gr.hua.dit.petcare.web.rest;

import gr.hua.dit.petcare.core.model.User;
import gr.hua.dit.petcare.core.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DebugController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DebugController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/debug/users")
    public String debugUsers() {
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>Λίστα Χρηστών (Debug)</h1>");
        sb.append("<table border='1'><tr><th>ID</th><th>Username</th><th>Role</th><th>Password Hash</th></tr>");
        
        for (User user : users) {
            sb.append("<tr>");
            sb.append("<td>").append(user.getId()).append("</td>");
            sb.append("<td>").append(user.getUsername()).append("</td>");
            sb.append("<td>").append(user.getRole()).append("</td>");
            sb.append("<td>").append(user.getPassword()).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    @GetMapping("/debug/check")
    public String checkPassword(@RequestParam String username, @RequestParam String rawPassword) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return "User not found";
        
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
        return "Username: " + username + "<br>" +
               "Raw Input: " + rawPassword + "<br>" +
               "DB Hash: " + user.getPassword() + "<br>" +
               "<strong>MATCHES? " + matches + "</strong>";
    }
}
