package tech.bjut.su.appeal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.stereotype.Component;

@Component
public class SinglePageAuthenticationConfigurer extends AbstractHttpConfigurer<SinglePageAuthenticationConfigurer, HttpSecurity> {

    private final ObjectMapper objectMapper;

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    public SinglePageAuthenticationConfigurer(
        ObjectMapper objectMapper,
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder
    ) {
        this.objectMapper = objectMapper;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void configure(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilter(new SinglePageAuthenticationFilter(objectMapper, jwtEncoder, jwtDecoder, authenticationManager));
    }
}
