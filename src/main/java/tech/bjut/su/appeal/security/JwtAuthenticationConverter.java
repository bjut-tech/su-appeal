package tech.bjut.su.appeal.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserDetailsService userDetailsService;

    public JwtAuthenticationConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        UserPrincipal principal = null;
        if (source.getSubject() != null) {
            principal = (UserPrincipal) userDetailsService.loadUserByUsername(source.getSubject());
        }

        return new JwtAuthenticationToken(source, principal, JwtResourceAuthoritiesHelper.extractAuthorities(source));
    }
}
