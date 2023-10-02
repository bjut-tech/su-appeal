package tech.bjut.su.appeal.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JWTConfig {

    private final SecretKey jwtSecret;

    public JWTConfig(AppProperties properties) {
        this.jwtSecret = new SecretKeySpec(
            properties.getAuth().getJwtSecret()
                .getBytes(StandardCharsets.UTF_8),
            "NONE"
        );
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(jwtSecret).build();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        // OctetSequenceKey key = new OctetSequenceKey.Builder(jwtSecret)
        //     .algorithm(JWSAlgorithm.HS256)
        //     .build();
        // JWKSource<SecurityContext> source = new ImmutableJWKSet<>(new JWKSet(key));

        JWKSource<SecurityContext> source = new ImmutableSecret<>(jwtSecret);
        return new NimbusJwtEncoder(source);
    }
}
