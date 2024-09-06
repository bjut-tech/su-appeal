package tech.bjut.su.appeal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class SinglePageAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public SinglePageAuthenticationFilter(
        ObjectMapper objectMapper,
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder,
        AuthenticationManager authenticationManager
    ) {
        super(authenticationManager);
        this.setFilterProcessesUrl("/token");
        this.setPostOnly(true);
        this.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler(objectMapper, jwtEncoder, jwtDecoder));
        this.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
    }
}
