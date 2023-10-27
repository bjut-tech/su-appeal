package tech.bjut.su.appeal.repository;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    long countByUser(User user);

    long countByAnswerNull();

    Window<Question> findFirst10ByOrderByIdDesc(KeysetScrollPosition position);

    Window<Question> findFirst10ByPublishedTrueOrderByIdDesc(KeysetScrollPosition position);

    Window<Question> findFirst10ByUserOrderByIdDesc(User user, KeysetScrollPosition position);

    @Override
    @EntityGraph(attributePaths = { "user", "attachments", "answer" })
    Optional<Question> findById(@NonNull Long id);

    @EntityGraph(attributePaths = { "user", "attachments", "answer" })
    Optional<Question> findByIdAndUser(Long id, User user);

    void deleteByPublishedFalseAndIdAndUser(Long id, User user);

    @Query(value = "SELECT DISTINCT a.id FROM Question p JOIN p.attachments a")
    List<String> findAllAttachmentIdsUsed();
}
