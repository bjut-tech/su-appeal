package tech.bjut.su.appeal.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class UserDetailsJwtAuthenticationToken extends JwtAuthenticationToken {

    private final UserPrincipal principal;

    public UserDetailsJwtAuthenticationToken(Jwt jwt, UserPrincipal principal) {
        super(jwt, principal.getAuthorities(), principal.getUser().getUid());
        this.principal = principal;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
