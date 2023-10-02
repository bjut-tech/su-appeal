package tech.bjut.su.appeal.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;

public class UserDetailsJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final MyUserDetailsService userDetailsService;

    public UserDetailsJwtGrantedAuthoritiesConverter(MyUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        UserPrincipal principal = userDetailsService.loadUserByUsername(source.getSubject());
        return new ArrayList<>(principal.getAuthorities());
    }
}
