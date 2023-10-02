package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.bjut.su.appeal.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {

}
