package tech.bjut.su.appeal.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtEncoder;

public class SinglePageAuthenticationConfigurer extends AbstractHttpConfigurer<SinglePageAuthenticationConfigurer, HttpSecurity> {

    private final JwtEncoder jwtEncoder;

    public SinglePageAuthenticationConfigurer(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Override
    public void configure(HttpSecurity http) {
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        http.addFilter(new SinglePageAuthenticationFilter(jwtEncoder, authenticationManager));
    }
}
