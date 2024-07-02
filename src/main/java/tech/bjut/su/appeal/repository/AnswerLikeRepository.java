package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.bjut.su.appeal.entity.Answer;
import tech.bjut.su.appeal.entity.AnswerLike;
import tech.bjut.su.appeal.entity.User;

import java.util.List;
import java.util.Optional;

public interface AnswerLikeRepository extends JpaRepository<AnswerLike, Long> {

    List<AnswerLike> findByUser(User user);

    Optional<AnswerLike> findByUserAndAnswer(User user, Answer answer);

    void deleteAllByAnswer(Answer answer);
}
