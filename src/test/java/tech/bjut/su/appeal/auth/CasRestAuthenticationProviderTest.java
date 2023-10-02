package tech.bjut.su.appeal.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import tech.bjut.su.appeal.security.CasRestAuthenticationProvider;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CasRestAuthenticationProviderTest {

    @Autowired
    private CasRestAuthenticationProvider casRestAuthenticationProvider;

    @Test
    public void testSupports() {
        assertTrue(casRestAuthenticationProvider.supports(UsernamePasswordAuthenticationToken.class));
        assertFalse(casRestAuthenticationProvider.supports(JwtAuthenticationToken.class));
        assertFalse(casRestAuthenticationProvider.supports(AnonymousAuthenticationToken.class));
    }

    @Test
    public void testAuthenticateFails() {
        var token = new UsernamePasswordAuthenticationToken("test", "test");
        assertThrows(BadCredentialsException.class, () -> casRestAuthenticationProvider.authenticate(token));
    }
}
