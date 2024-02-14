package tech.bjut.su.appeal.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AuthServerTokenExchangeAuthenticationProvider implements AuthenticationProvider {

    private final String authServer;

    private final RestTemplate restTemplate;

    private final UserService userService;

    public AuthServerTokenExchangeAuthenticationProvider(
        AppProperties appProperties,
        UserService userService
    ) {
        this.authServer = appProperties.getAuthServer();
        this.restTemplate = new RestTemplate();
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        final String token = ((BearerTokenAuthenticationToken) authentication).getToken();

        final String url = UriComponentsBuilder
            .fromHttpUrl(authServer)
            .path("/userinfo")
            .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(token);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                ObjectNode obj = (ObjectNode) objectMapper.readTree(response.getBody());

                String uid = obj.get("sub").asText();
                Map<String, String> attributes = new HashMap<>();
                obj.fieldNames().forEachRemaining(key -> {
                    if (key.equals("sub")) {
                        return;
                    }
                    JsonNode node = obj.get(key);
                    if (node != null && node.isTextual()) {
                        attributes.put(key, node.asText());
                    }
                });

                User user = userService.findOrCreate(uid, attributes);
                UserPrincipal principal = new UserPrincipal(user);
                return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            } catch (JsonProcessingException e) {
                throw new AuthenticationServiceException("Failed to parse userinfo response: " + e.getMessage());
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
                throw new BadCredentialsException("Invalid token");
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
