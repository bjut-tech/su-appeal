package tech.bjut.su.appeal.service;

import org.springframework.stereotype.Service;
import tech.bjut.su.appeal.config.AppProperties;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.UserRepository;

import java.util.Map;
import java.util.Set;

@Service
public class UserService {

    private final Set<String> adminList;

    private final UserRepository repository;

    public UserService(
        AppProperties properties,
        UserRepository repository
    ) {
        this.adminList = properties.getAuth().getAdmin();
        this.repository = repository;
    }

    public User find(String uid) {
        User user = repository.findByUid(uid);

        if (user != null) {
            user.setAdmin(adminList.contains(user.getUid()));
        }

        return user;
    }

    public User findOrCreate(String uid, String name) {
        return findOrCreate(uid, Map.of("name", name), false);
    }

    public User findOrCreate(String uid, Map<String, String> attributes, boolean trusted) {
        User user = repository.findByUid(uid);
        boolean isNew = false;

        if (user == null) {
            user = new User();
            user.setUid(uid);
            isNew = true;
        }
        user.setRole(UserRoleEnum.STUDENT); // TODO: Actual role
        user.setAdmin(adminList.contains(user.getUid()));

        if (trusted || isNew) {
            user.setName(attributes.get("name"));
        }

        user = repository.save(user);
        return user;
    }
}
