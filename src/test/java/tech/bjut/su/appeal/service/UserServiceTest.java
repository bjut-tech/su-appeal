package tech.bjut.su.appeal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.UserRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DataJpaTest
@ActiveProfiles("test")
@EnableConfigurationProperties(AppProperties.class)
@TestPropertySource(properties = {"app.auth.admin=user2"})
@Import(UserService.class)
@Transactional
public class UserServiceTest {

    @SpyBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    // The test data
    private static final String USER_UID = "user1";
    private static final String USER_UID_ADMIN = "user2";
    private static final String USER_UID_NON_EXIST = "nonExist";
    private static final String USER_NAME = "User";
    private static final String USER_NAME_UPDATED = "User Updated";

    private static final String ATTRIBUTES_KEY_NAME = "name";

    @BeforeEach
    public void setUp() {
        User user = new User();
        user.setUid(USER_UID);
        user.setName(USER_NAME);
        user.setRole(UserRoleEnum.STUDENT);

        User admin = new User();
        admin.setUid(USER_UID_ADMIN);
        admin.setName(USER_NAME);
        admin.setRole(UserRoleEnum.STUDENT);

        userRepository.saveAll(List.of(user, admin));
    }

    @Test
    public void testFind_existNonAdmin() {
        // execute
        User fetchedUser = userService.find(USER_UID);

        // verify
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getUid()).isEqualTo(USER_UID);
        assertThat(fetchedUser.getName()).isEqualTo(USER_NAME);
        assertThat(fetchedUser.isAdmin()).isFalse();
    }

    @Test
    public void testFind_existAdmin() {
        // execute
        User fetchedUser = userService.find(USER_UID_ADMIN);

        // verify
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getUid()).isEqualTo(USER_UID_ADMIN);
        assertThat(fetchedUser.getName()).isEqualTo(USER_NAME);
        assertThat(fetchedUser.isAdmin()).isTrue();
    }

    @Test
    public void testFind_notExist() {
        // execute
        User user = userService.find(USER_UID_NON_EXIST);

        // verify
        assertThat(user).isNull();
        verify(userRepository, times(1)).findByUid(USER_UID_NON_EXIST);
    }

    @Test
    public void testFindOrCreate_existNonAdmin() {
        // execute
        User user = userService.findOrCreate(USER_UID, Map.of(ATTRIBUTES_KEY_NAME, USER_NAME_UPDATED));

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getUid()).isEqualTo(USER_UID);
        assertThat(user.getName()).isEqualTo(USER_NAME_UPDATED);
        assertThat(user.isAdmin()).isFalse();
        verify(userRepository, times(1)).findByUid(USER_UID);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testFindOrCreate_existAdmin() {
        // execute
        User user = userService.findOrCreate(USER_UID_ADMIN, Map.of(ATTRIBUTES_KEY_NAME, USER_NAME_UPDATED));

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getUid()).isEqualTo(USER_UID_ADMIN);
        assertThat(user.getName()).isEqualTo(USER_NAME_UPDATED);
        assertThat(user.isAdmin()).isTrue();
        verify(userRepository, times(1)).findByUid(USER_UID_ADMIN);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testFindOrCreate_nonExist() {
        // execute
        User user = userService.findOrCreate(USER_UID_NON_EXIST, Map.of(ATTRIBUTES_KEY_NAME, USER_NAME));

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getUid()).isEqualTo(USER_UID_NON_EXIST);
        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.isAdmin()).isFalse();
        verify(userRepository, times(1)).findByUid(USER_UID_NON_EXIST);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testFindOrCreate_nameDirectly() {
        // execute
        User user = userService.findOrCreate(USER_UID, USER_NAME_UPDATED);

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getUid()).isEqualTo(USER_UID);
        assertThat(user.getName()).isEqualTo(USER_NAME_UPDATED);
        assertThat(user.isAdmin()).isFalse();
        verify(userRepository, times(1)).findByUid(USER_UID);
        verify(userRepository, times(1)).save(user);
    }
}
