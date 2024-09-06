package tech.bjut.su.appeal.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.bjut.su.appeal.entity.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EqualsAndHashCode(of = "user")
public class UserPrincipal implements UserDetails {

    @Getter
    private final User user;

    private final Collection<SimpleGrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.user = user;

        List<SimpleGrantedAuthority> authoritiesGranted = new ArrayList<>();
        authoritiesGranted.add(new SimpleGrantedAuthority(user.getRole().name()));
        if (this.user.isAdmin()) {
            authoritiesGranted.add(new SimpleGrantedAuthority("ADMIN"));
        }
        authorities = Collections.unmodifiableCollection(authoritiesGranted);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return user.getUid();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
