package tech.bjut.su.appeal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtEncoder encoder;

    public JwtAuthenticationSuccessHandler(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        Instant now = Instant.now();
        long expiry = 86400;
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiry))
            .subject(((UserPrincipal) authentication.getPrincipal()).getUsername())
            .build();

        Jwt jwt = this.encoder.encode(JwtEncoderParameters.from(header, claims));

        Map<String, String> jsonResponse = Map.of(
            "access_token", jwt.getTokenValue(),
            "expires_in", String.valueOf(expiry),
            "token_type", "Bearer"
        );

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(), jsonResponse);
    }
}
