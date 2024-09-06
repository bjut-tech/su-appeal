package tech.bjut.su.appeal.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tech.bjut.su.appeal.security.CasRestAuthenticationProvider;
import tech.bjut.su.appeal.security.JwtAuthenticationConverter;
import tech.bjut.su.appeal.security.SinglePageAuthenticationConfigurer;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String frontendUrl;

    private final SecretKey jwtSecret;

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(AppProperties properties) {
        this.frontendUrl = properties.getFrontend();
        this.jwtSecret = new SecretKeySpec(
            properties.getAuth().getJwtSecret()
                .getBytes(StandardCharsets.UTF_8),
            "NONE"
        );
    }

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        CasRestAuthenticationProvider casRestAuthenticationProvider,
        JwtAuthenticationConverter jwtAuthenticationConverter,
        SinglePageAuthenticationConfigurer singlePageAuthenticationConfigurer
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())
            .headers(headers -> headers
                .httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable)
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            )
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

        http.apply(singlePageAuthenticationConfigurer);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
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

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(jwtSecret).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWKSource<SecurityContext> source = new ImmutableSecret<>(jwtSecret);
        return new NimbusJwtEncoder(source);
    }
}
