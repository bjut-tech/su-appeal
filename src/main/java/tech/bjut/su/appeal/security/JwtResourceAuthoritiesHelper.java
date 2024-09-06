package tech.bjut.su.appeal.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JwtResourceAuthoritiesHelper {
    public static final String CLAIM_RESOURCES = "resources";

    public static Collection<ResourceAuthority> extractAuthorities(Jwt jwt) {
        List<ResourceAuthority> authorities = new ArrayList<>();
        try {
            for (String claimed : jwt.getClaimAsStringList("resources")) {
                String[] parts = StringUtils.split(claimed, ',');
                if (parts == null || parts.length != 2) {
                    continue;
                }
                authorities.add(new ResourceAuthority(parts[0], parts[1]));
            }
        } catch (RuntimeException ignored) {}

        return authorities;
    }

    public static List<String> extractClaim(Collection<GrantedAuthority> authorities) {
        List<String> result = new ArrayList<>();
        for (GrantedAuthority authority : authorities) {
            if (authority instanceof ResourceAuthority resourceAuthority) {
                result.add(resourceAuthority.getEntityName() + "," + resourceAuthority.getEntityId());
            }
        }
        return result;
    }

    public static List<String> extractClaim(@Nullable Authentication authentication, GrantedAuthority additionalAuthority) {
        List<GrantedAuthority> authorities = authentication == null
            ? new ArrayList<>()
            : new ArrayList<>(authentication.getAuthorities());
        authorities.add(additionalAuthority);
        return extractClaim(authorities);
    }
}
