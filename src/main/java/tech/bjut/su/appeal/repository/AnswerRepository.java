package tech.bjut.su.appeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tech.bjut.su.appeal.entity.Answer;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    Optional<Answer> findByQuestionId(Long id);

    @Query(value = "SELECT DISTINCT a.id FROM Answer p JOIN p.attachments a")
    List<String> findAllAttachmentIdsUsed();
}
