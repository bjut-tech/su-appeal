package tech.bjut.su.appeal.repository;

import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.support.ExtendedSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface QuestionRepository extends JpaRepository<Question, Long>, ExtendedSpecificationExecutor<Question> {

    long countByUser(User user);

    long countByAnswerNull();

    Window<Question> findFirst10ByOrderByIdDesc(KeysetScrollPosition position);

    Window<Question> findFirst10ByPublishedTrueOrderByIdDesc(KeysetScrollPosition position);

    Window<Question> findFirst10ByUserOrderByIdDesc(User user, KeysetScrollPosition position);

    Optional<Question> findByIdAndUser(Question question, User user);

    @Query(value = "SELECT DISTINCT a.id FROM Question p JOIN p.attachments a")
    List<String> findAllAttachmentIdsUsed();
}
