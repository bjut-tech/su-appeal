package tech.bjut.su.appeal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.dto.RedirectResponseDto;

import java.net.URI;

@RestController
public class LoginController {

    private final AppProperties properties;

    public LoginController(AppProperties properties) {
        this.properties = properties;
    }

    @GetMapping("/token/redirect")
    public RedirectResponseDto redirect() {
        final String callbackUrl = UriComponentsBuilder
            .fromHttpUrl(properties.getFrontend())
            .path("/login/callback")
            .toUriString();

        final String url = UriComponentsBuilder
            .fromHttpUrl(properties.getAuthServer())
            .path("/login.html")
            .queryParam("redirect", callbackUrl)
            .encode()
            .toUriString();

        return new RedirectResponseDto(url);
    }
}
