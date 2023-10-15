package tech.bjut.su.appeal.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.security.UserPrincipal;

import java.util.Objects;

@Service
public class SecurityService {

    public Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public User user() {
        Authentication authentication = authentication();

        if (authentication == null) {
            return null;
        }

        return ((UserPrincipal) authentication.getPrincipal()).getUser();
    }

    public String username() {
        Authentication authentication = authentication();

        if (authentication == null) {
            return null;
        }

        return authentication.getName();
    }

    public boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream().anyMatch(
            grantedAuthority -> Objects.equals(grantedAuthority.getAuthority(), authority)
        );
    }
}
