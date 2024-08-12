package tech.bjut.su.appeal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.UserRepository;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    // The test data
    private static final long USER_ID = 1;
    private static final String USER_UID = "user";
    private static final String USER_UID_NON_EXIST = "nonExist";
    private static final String USER_UID_ADMIN = "admin";
    private static final String USER_NAME = "User";
    private static final String USER_NAME_UPDATED = "User Updated";
    private static final String ATTRIBUTES_KEY_NAME = "name";

    @BeforeEach
    public void setUp() {
        // create app properties
        AppProperties appProperties = new AppProperties();
        appProperties.getAuth().setAdmin(new HashSet<>(Set.of(USER_UID_ADMIN)));

        // manually created the service
        userService = new UserService(appProperties, userRepository);
    }

    @Test
    public void testFind_existNonAdmin() {
        // setup mocking
        setupMocking_userRepository_findByUid();

        // execute
        User user = userService.find(USER_UID);

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getUid()).isEqualTo(USER_UID);
        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.isAdmin()).isFalse();
        verify(userRepository, times(1)).findByUid(USER_UID);
    }

    @Test
    public void testFind_existAdmin() {
        // setup mocking
        setupMocking_userRepository_findByUid();

        // execute
        User user = userService.find(USER_UID_ADMIN);

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getUid()).isEqualTo(USER_UID_ADMIN);
        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.isAdmin()).isTrue();
        verify(userRepository, times(1)).findByUid(USER_UID_ADMIN);
    }

    @Test
    public void testFind_notExist() {
        // setup mocking
        setupMocking_userRepository_findByUid();

        // execute
        User user = userService.find(USER_UID_NON_EXIST);

        // verify
        assertThat(user).isNull();
        verify(userRepository, times(1)).findByUid(USER_UID_NON_EXIST);
    }

    @Test
    public void testFindOrCreate_existNonAdmin() {
        // setup mocking
        setupMocking_userRepository_findByUid();
        setupMocking_userRepository_save();

        // execute
        User user = userService.findOrCreate(USER_UID, Map.of(ATTRIBUTES_KEY_NAME, USER_NAME_UPDATED));

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getUid()).isEqualTo(USER_UID);
        assertThat(user.getName()).isEqualTo(USER_NAME_UPDATED);
        assertThat(user.isAdmin()).isFalse();
        verify(userRepository, times(1)).findByUid(USER_UID);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testFindOrCreate_existAdmin() {
        // setup mocking
        setupMocking_userRepository_findByUid();
        setupMocking_userRepository_save();

        // execute
        User user = userService.findOrCreate(USER_UID_ADMIN, Map.of(ATTRIBUTES_KEY_NAME, USER_NAME_UPDATED));

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getUid()).isEqualTo(USER_UID_ADMIN);
        assertThat(user.getName()).isEqualTo(USER_NAME_UPDATED);
        assertThat(user.isAdmin()).isTrue();
        verify(userRepository, times(1)).findByUid(USER_UID_ADMIN);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testFindOrCreate_nonExist() {
        // setup mocking
        setupMocking_userRepository_findByUid();
        setupMocking_userRepository_save();

        // execute
        User user = userService.findOrCreate(USER_UID_NON_EXIST, Map.of(ATTRIBUTES_KEY_NAME, USER_NAME));

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getUid()).isEqualTo(USER_UID_NON_EXIST);
        assertThat(user.getName()).isEqualTo(USER_NAME);
        assertThat(user.isAdmin()).isFalse();
        verify(userRepository, times(1)).findByUid(USER_UID_NON_EXIST);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testFindOrCreate_nameDirectly() {
        // setup mocking
        setupMocking_userRepository_findByUid();
        setupMocking_userRepository_save();

        // execute
        User user = userService.findOrCreate(USER_UID, USER_NAME_UPDATED);

        // verify
        assertThat(user).isNotNull();
        assertThat(user.getId()).isGreaterThan(0);
        assertThat(user.getUid()).isEqualTo(USER_UID);
        assertThat(user.getName()).isEqualTo(USER_NAME_UPDATED);
        assertThat(user.isAdmin()).isFalse();
        verify(userRepository, times(1)).findByUid(USER_UID);
        verify(userRepository, times(1)).save(user);
    }

    private void setupMocking_userRepository_findByUid() {
        when(userRepository.findByUid(anyString()))
            .thenAnswer(invocation -> {
                if (USER_UID.equals(invocation.getArgument(0)) || USER_UID_ADMIN.equals(invocation.getArgument(0))) {
                    User user = new User();
                    user.setId(USER_ID);
                    user.setUid(invocation.getArgument(0));
                    user.setName(USER_NAME);
                    return user;
                }
                return null;
            });
    }

    private void setupMocking_userRepository_save() {
        when(userRepository.save(any(User.class)))
            .thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(USER_ID);
                return user;
            });
    }
}
