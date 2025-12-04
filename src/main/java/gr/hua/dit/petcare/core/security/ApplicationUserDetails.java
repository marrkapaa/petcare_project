package gr.hua.dit.petcare.core.security;

import gr.hua.dit.petcare.core.model.Role;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public final class ApplicationUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String passwordHash;
    private final Role role;

    public ApplicationUserDetails(final Long userId,
                                  final String username,
                                  final String passwordHash,
                                  final Role role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long userId() {
        return this.userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String authorityName = "ROLE_" + this.role.name();
        return Collections.singletonList(new SimpleGrantedAuthority(authorityName));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
