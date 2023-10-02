package tech.bjut.su.appeal.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsJwtAuthenticationConverter extends JwtAuthenticationConverter {

    public UserDetailsJwtAuthenticationConverter(MyUserDetailsService userDetailsService) {
        super();
        this.setJwtGrantedAuthoritiesConverter(new UserDetailsJwtGrantedAuthoritiesConverter(userDetailsService));
    }
}
