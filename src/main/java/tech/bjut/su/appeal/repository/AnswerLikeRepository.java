package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.bjut.su.appeal.entity.Answer;
import tech.bjut.su.appeal.entity.AnswerLike;
import tech.bjut.su.appeal.entity.User;

import java.util.List;
import java.util.Optional;

public interface AnswerLikeRepository extends JpaRepository<AnswerLike, Long> {

    @Query("SELECT DISTINCT a.answer FROM AnswerLike a WHERE a.user = ?1")
    List<Answer> findDistinctAnswerByUser(User user);

    Optional<AnswerLike> findByUserAndAnswer(User user, Answer answer);
}
