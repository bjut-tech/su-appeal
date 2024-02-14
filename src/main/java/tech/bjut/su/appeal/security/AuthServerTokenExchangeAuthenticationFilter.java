package tech.bjut.su.appeal.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Objects;

public class AuthServerTokenExchangeAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final AntPathRequestMatcher requestMatcher = new AntPathRequestMatcher("/token/exchange");

    public AuthServerTokenExchangeAuthenticationFilter(
        JwtEncoder jwtEncoder,
        AuthenticationManager authenticationManager
    ) {
        super(requestMatcher, authenticationManager);
        this.setAuthenticationSuccessHandler(new JwtAuthenticationSuccessHandler(jwtEncoder));
        this.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String token = "";

        final Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (Objects.equals(cookie.getName(), "_token")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        BearerTokenAuthenticationToken authRequest = new BearerTokenAuthenticationToken(token);
        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
