package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.bjut.su.appeal.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUid(String uid);
}
