package tech.bjut.su.appeal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.bjut.su.appeal.security.UserPrincipal;

@RestController
public class UserController {

    @GetMapping("/user")
    public UserPrincipal getCurrentUser(Authentication auth) {
        return (UserPrincipal) auth.getPrincipal();
    }
}
