package tech.bjut.su.appeal.security;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.stream.Stream;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    @Nullable
    private final Jwt jwt;

    @Nullable
    private final UserPrincipal principal;

    public JwtAuthenticationToken(
        @Nullable Jwt jwt,
        @Nullable UserPrincipal principal
    ) {
        this(jwt, principal, null);
    }

    public JwtAuthenticationToken(
        @Nullable Jwt jwt,
        @Nullable Collection<ResourceAuthority> authorities
    ) {
        this(jwt, null, authorities);
    }

    public JwtAuthenticationToken(
        @Nullable Jwt jwt,
        @Nullable UserPrincipal principal,
        @Nullable Collection<ResourceAuthority> authorities
    ) {
        super(Stream.concat(
            principal != null ? principal.getAuthorities().stream() : Stream.empty(),
            authorities != null ? authorities.stream() : Stream.empty()
        ).toList());
        super.setAuthenticated(jwt != null && principal != null);
        this.jwt = jwt;
        this.principal = principal;
    }

    @Override
    public Object getCredentials() {
        return jwt;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new IllegalArgumentException("Cannot change authentication status");
    }
}
