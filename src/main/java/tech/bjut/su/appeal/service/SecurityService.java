package tech.bjut.su.appeal.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.security.ResourceAuthority;
import tech.bjut.su.appeal.security.UserPrincipal;

@Service
public class SecurityService {

    public Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public UserPrincipal principal() {
        Authentication authentication = authentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }

        return (UserPrincipal) authentication.getPrincipal();
    }

    public User user() {
        UserPrincipal principal = principal();
        if (principal == null) {
            return null;
        }

        return principal.getUser();
    }

    public String username() {
        UserPrincipal principal = principal();
        if (principal == null) {
            return null;
        }

        return principal.getUsername();
    }

    public boolean hasAuthority(String authority) {
        Authentication authentication = authentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream().anyMatch(grantedAuthority ->
            authority.equals(grantedAuthority.getAuthority())
        );
    }

    public boolean hasAuthority(ResourceAuthority authority) {
        Authentication authentication = authentication();
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream().anyMatch(grantedAuthority ->
            grantedAuthority instanceof ResourceAuthority
                && authority.equals(grantedAuthority)
        );
    }
}
