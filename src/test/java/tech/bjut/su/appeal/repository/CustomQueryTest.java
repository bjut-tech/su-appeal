package tech.bjut.su.appeal.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.UserRoleEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class CustomQueryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    // The test data
    private User user;
    private Attachment attachment1, attachment2, attachment3, attachment4;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUid("user1");
        user.setRole(UserRoleEnum.STUDENT);
        user = userRepository.save(user);

        attachment1 = new Attachment();
        attachment1.setSize(0);
        attachment1 = attachmentRepository.save(attachment1);
        attachment2 = new Attachment();
        attachment2.setSize(0);
        attachment2 = attachmentRepository.save(attachment2);
        attachment3 = new Attachment();
        attachment3.setSize(0);
        attachment3 = attachmentRepository.save(attachment3);
        attachment4 = new Attachment();
        attachment4.setSize(0);
        attachmentRepository.save(attachment4);
    }

    @Test
    public void testAnnouncementRepository_findAllAttachmentIdsUsed() {
        // Setup
        Announcement announcement1 = new Announcement();
        announcement1.setUser(user);
        announcement1.setTitle("Sample Announcement");
        announcement1.setContent("Sample Announcement");
        announcement1.setPinned(false);
        announcement1.setAttachments(List.of(attachment1, attachment2));
        announcementRepository.save(announcement1);
        Announcement announcement2 = new Announcement();
        announcement2.setUser(user);
        announcement2.setTitle("Sample Announcement");
        announcement2.setContent("Sample Announcement");
        announcement2.setPinned(false);
        announcement2.setAttachments(List.of(attachment2, attachment3));
        announcementRepository.save(announcement2);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        List<String> attachmentIds = announcementRepository.findAllAttachmentIdsUsed();
        assertThat(attachmentIds).containsExactlyInAnyOrder(
            attachment1.getId().toString(),
            attachment2.getId().toString(),
            attachment3.getId().toString()
        );
    }

    @Test
    public void testQuestionRepository_findAllAttachmentIdsUsed() {
        // Setup
        Question question1 = new Question();
        question1.setUser(user);
        question1.setContent("Sample Question");
        question1.setPublished(false);
        question1.setAttachments(List.of(attachment1, attachment2));
        questionRepository.save(question1);
        Question question2 = new Question();
        question2.setUser(user);
        question2.setContent("Sample Question");
        question2.setPublished(false);
        question2.setAttachments(List.of(attachment2, attachment3));
        questionRepository.save(question2);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        List<String> attachmentIds = questionRepository.findAllAttachmentIdsUsed();
        assertThat(attachmentIds).containsExactlyInAnyOrder(
            attachment1.getId().toString(),
            attachment2.getId().toString(),
            attachment3.getId().toString()
        );
    }

    @Test
    public void testAnswerRepository_findAllAttachmentIdsUsed() {
        // Setup
        Question question = new Question();
        question.setUser(user);
        question.setContent("Sample Question");
        question.setPublished(false);
        question = questionRepository.save(question);

        tech.bjut.su.appeal.entity.Answer answer1 = new tech.bjut.su.appeal.entity.Answer();
        answer1.setUser(user);
        answer1.setContent("Sample Answer");
        answer1.setQuestion(question);
        answer1.setAttachments(List.of(attachment1, attachment2));
        answerRepository.save(answer1);
        tech.bjut.su.appeal.entity.Answer answer2 = new tech.bjut.su.appeal.entity.Answer();
        answer2.setUser(user);
        answer2.setContent("Sample Answer");
        answer2.setQuestion(question);
        answer2.setAttachments(List.of(attachment2, attachment3));
        answerRepository.save(answer2);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        List<String> attachmentIds = answerRepository.findAllAttachmentIdsUsed();
        assertThat(attachmentIds).containsExactlyInAnyOrder(
            attachment1.getId().toString(),
            attachment2.getId().toString(),
            attachment3.getId().toString()
        );
    }
}
