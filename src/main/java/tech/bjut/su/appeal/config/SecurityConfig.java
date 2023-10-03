package tech.bjut.su.appeal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import tech.bjut.su.appeal.security.CasRestAuthenticationProvider;
import tech.bjut.su.appeal.security.SinglePageAuthenticationConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CasRestAuthenticationProvider casRestAuthenticationProvider;

    private final JwtEncoder jwtEncoder;

    public SecurityConfig(
        CasRestAuthenticationProvider casRestAuthenticationProvider,
        JwtEncoder jwtEncoder
    ) {
        this.casRestAuthenticationProvider = casRestAuthenticationProvider;
        this.jwtEncoder = jwtEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable)
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(casRestAuthenticationProvider)
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(new AntPathRequestMatcher("/actuator/health")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            );

        http.apply(new SinglePageAuthenticationConfigurer(jwtEncoder));

        return http.build();
    }
}
