package tech.bjut.su.appeal.config;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.boot.test.context.TestConfiguration;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.UserRepository;

import java.util.List;

@TestConfiguration
public class TestSecurityConfig {

    private final UserRepository userRepository;

    public TestSecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    @Transactional
    public void populateUsers() {
        User user = new User();
        user.setUid(USER_UID);
        user.setRole(USER_ROLE);
        user.setName(USER_NAME);

        User admin = new User();
        admin.setUid(ADMIN_UID);
        admin.setRole(ADMIN_ROLE);
        admin.setName(ADMIN_NAME);

        userRepository.saveAllAndFlush(List.of(user, admin));
    }

    // The test data
    public static final String USER_UID = "user";
    public static final UserRoleEnum USER_ROLE = UserRoleEnum.STUDENT;
    public static final String USER_NAME = "User";
    public static final String ADMIN_UID = "admin";
    public static final UserRoleEnum ADMIN_ROLE = UserRoleEnum.STUDENT;
    public static final String ADMIN_NAME = "Admin";
}
