package gr.hua.dit.petcare.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. Password Encoder - Χρήση BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. ΠΡΩΤΟΣ ΚΑΝΟΝΑΣ (ORDER 1): H2 Console Fix
    // Απαιτείται για να λειτουργήσει το H2 Console όταν είναι ενεργοποιημένη η ασφάλεια
    @Bean
    @Order(1)
    public SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .securityMatcher("/h2-console/**")
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
            .build();
    }

    // 3. ΔΕΥΤΕΡΟΣ ΚΑΝΟΝΑΣ (ORDER 2): UI Security (Owner/Vet Roles)
    @Bean
    @Order(2)
    public SecurityFilterChain uiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Δημόσιες σελίδες: Αρχική, Εγγραφή, Login
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**").permitAll()

                // Owners Access: Μόνο με ρόλο ROLE_OWNER
                .requestMatchers("/owners/**").hasAuthority("ROLE_OWNER")

                // Vets Access: Μόνο με ρόλο ROLE_VETERINARIAN
                .requestMatchers("/vets/**").hasAuthority("ROLE_VETERINARIAN")

                // Οποιοδήποτε άλλο request απαιτεί Authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true) // Μετά το login, πήγαινε στην αρχική
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login") // Μετά το logout, πήγαινε στο login
                .permitAll()
            )
            // Απενεργοποίηση CSRF για το POST /login (standard practice)
            .csrf(csrf -> csrf.ignoringRequestMatchers("/login", "/register"));

        return http.build();
    }

}
