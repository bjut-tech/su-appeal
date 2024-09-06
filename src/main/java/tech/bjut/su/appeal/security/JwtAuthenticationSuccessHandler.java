package tech.bjut.su.appeal.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tech.bjut.su.appeal.dto.TokenResponseDto;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final JwtEncoder jwtEncoder;

    private final JwtDecoder jwtDecoder;

    private static final String MERGE_TOKEN_HEADER = "X-Merge-Token";

    private static final List<String> MERGE_TOKEN_ALLOWED_CLAIMS = List.of(
        JwtResourceAuthoritiesHelper.CLAIM_RESOURCES
    );

    public JwtAuthenticationSuccessHandler(
        ObjectMapper objectMapper,
        JwtEncoder jwtEncoder,
        JwtDecoder jwtDecoder
    ) {
        this.objectMapper = objectMapper;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        Jwt tokenToMerge = tokenToMerge(request);
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        Instant now = Instant.now();
        long expiry = 86400; // one day
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiry))
            .subject(((UserPrincipal) authentication.getPrincipal()).getUsername());
        if (tokenToMerge != null) {
            for (String claim : MERGE_TOKEN_ALLOWED_CLAIMS) {
                if (tokenToMerge.hasClaim(claim)) {
                    builder.claim(claim, tokenToMerge.getClaims().get(claim));
                }
            }
        }
        JwtClaimsSet claims = builder.build();

        Jwt jwt = jwtEncoder.encode(JwtEncoderParameters.from(header, claims));

        TokenResponseDto jsonResponse = new TokenResponseDto();
        jsonResponse.setAccessToken(jwt.getTokenValue());
        jsonResponse.setExpiresIn(expiry);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), jsonResponse);
    }

    @Nullable
    private Jwt tokenToMerge(HttpServletRequest request) {
        String tokenRaw = request.getHeader(MERGE_TOKEN_HEADER);
        try {
            return jwtDecoder.decode(tokenRaw);
        } catch (JwtException e) {
            return null;
        }
    }
}
