package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.bjut.su.appeal.entity.QuestionCategory;

public interface QuestionCategoryRepository extends JpaRepository<QuestionCategory, Long> {
}
