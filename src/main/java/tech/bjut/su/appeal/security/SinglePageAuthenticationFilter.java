package tech.bjut.su.appeal.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class SinglePageAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public SinglePageAuthenticationFilter(
        JwtEncoder jwtEncoder,
        AuthenticationManager authenticationManager
    ) {
        super(authenticationManager);
        this.setFilterProcessesUrl("/token");
        this.setPostOnly(true);
        this.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler(jwtEncoder));
        this.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
    }
}
