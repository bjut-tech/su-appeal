package tech.bjut.su.appeal.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.service.UserService;
import tech.bjut.su.appeal.util.IPv6Generator;
import tech.bjut.su.appeal.util.InsecureRestTemplate;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Lazy
public class CasRestAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate restTemplate;

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(CasRestAuthenticationProvider.class);

    public CasRestAuthenticationProvider(
        InsecureRestTemplate restTemplate,
        UserService userService
    ) {
        this.restTemplate = restTemplate;
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        if (username.isBlank() || password.isBlank()) {
            return null; // unable to authenticate
        }

        final String url = "https://bjutwaf.bjut.tech/v1/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setHost(new InetSocketAddress("cas.bjut.edu.cn", 0));
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("User-Agent", "Mozilla/5.0");
        headers.set("X-Forwarded-For", IPv6Generator.generateInternal());

        logger.debug("CAS auth request headers: " + headers);

        MultiValueMap<String, String> request = new LinkedMultiValueMap<>();
        request.put("username", List.of(username));
        request.put("password", List.of(password));

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(request, headers),
                String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            try {
                ObjectNode obj = (ObjectNode) mapper.readTree(response.getBody())
                    .get("authentication")
                    .get("principal");

                String uid = obj.get("id").asText();

                Map<String, String> attributes = new HashMap<>();
                ObjectNode objAttrs = (ObjectNode) obj.get("attributes");
                objAttrs.fieldNames().forEachRemaining(key -> {
                    JsonNode node = objAttrs.get(key).get(0);
                    if (node != null && node.isTextual()) {
                        attributes.put(key, node.asText());
                    }
                });

                User user = userService.findOrCreate(uid, attributes, true);
                UserPrincipal principal = new UserPrincipal(user);
                return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

            } catch (JsonProcessingException e) {
                throw new AuthenticationServiceException("Failed to parse CAS response: " + e.getMessage());
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
                throw new BadCredentialsException("Invalid credentials");
            } else if (e.getStatusCode().isSameCodeAs(HttpStatus.LOCKED)) {
                throw new LockedException("Account temporarily locked");
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
