package tech.bjut.su.appeal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tech.bjut.su.appeal.dto.TokenResponseDto;

import java.io.IOException;
import java.time.Instant;

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
            .issuer("https://appeal.bjut.tech/")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiry))
            .subject(((UserPrincipal) authentication.getPrincipal()).getUsername())
            .build();

        Jwt jwt = this.encoder.encode(JwtEncoderParameters.from(header, claims));

        TokenResponseDto jsonResponse = new TokenResponseDto();
        jsonResponse.setAccessToken(jwt.getTokenValue());
        jsonResponse.setExpiresIn(expiry);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(), jsonResponse);
    }
}
