package tech.bjut.su.appeal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tech.bjut.su.appeal.security.CasRestAuthenticationProvider;
import tech.bjut.su.appeal.security.SinglePageAuthenticationConfigurer;
import tech.bjut.su.appeal.security.UserDetailsJwtAuthenticationConverter;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CasRestAuthenticationProvider casRestAuthenticationProvider;

    private final UserDetailsJwtAuthenticationConverter jwtAuthenticationConverter;

    private final JwtEncoder jwtEncoder;

    private final String frontendUrl;

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(
        AppProperties properties,
        CasRestAuthenticationProvider casRestAuthenticationProvider,
        UserDetailsJwtAuthenticationConverter jwtAuthenticationConverter,
        JwtEncoder jwtEncoder
    ) {
        this.casRestAuthenticationProvider = casRestAuthenticationProvider;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.jwtEncoder = jwtEncoder;
        this.frontendUrl = properties.getFrontend();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .headers(headers -> headers.httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable)
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(casRestAuthenticationProvider)
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(
                    new AntPathRequestMatcher("/admin/**"),
                    new AntPathRequestMatcher("/actuator/metrics/**")
                ).hasAuthority("ADMIN")
                .requestMatchers(
                    new AntPathRequestMatcher("/user/**")
                ).authenticated()
                .anyRequest().permitAll()
            )
            .exceptionHandling((exceptions) -> exceptions
                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            );

        http.apply(new SinglePageAuthenticationConfigurer(jwtEncoder));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            logger.info("Configured frontend URL: {}", frontendUrl);
            configuration.setAllowedOrigins(List.of(frontendUrl));
        }
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setMaxAge(Duration.ofHours(1));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
