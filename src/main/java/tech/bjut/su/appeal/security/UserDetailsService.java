package tech.bjut.su.appeal.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.service.UserService;

@Component
@Primary
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserService userService;

    public UserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.find(username);

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return new UserPrincipal(user);
    }
}
