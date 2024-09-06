package tech.bjut.su.appeal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.bjut.su.appeal.security.UserPrincipal;
import tech.bjut.su.appeal.service.SecurityService;

@RestController
public class UserController {

    private final SecurityService securityService;

    public UserController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping("/user")
    public UserPrincipal getCurrentUser() {
        return securityService.principal();
    }
}
