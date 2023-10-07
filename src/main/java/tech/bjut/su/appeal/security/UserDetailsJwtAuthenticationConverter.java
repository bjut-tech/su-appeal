package tech.bjut.su.appeal.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final MyUserDetailsService userDetailsService;

    public UserDetailsJwtAuthenticationConverter(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        UserPrincipal principal = userDetailsService.loadUserByUsername(source.getSubject());

        return new UserDetailsJwtAuthenticationToken(source, principal);
    }
}
