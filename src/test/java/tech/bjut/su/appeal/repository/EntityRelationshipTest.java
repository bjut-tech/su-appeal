package tech.bjut.su.appeal.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.entity.*;
import tech.bjut.su.appeal.enums.UserRoleEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback
public class EntityRelationshipTest {

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
    private AnswerLikeRepository answerLikeRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    // The test data
    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUid("user1");
        user.setRole(UserRoleEnum.STUDENT);
        user = userRepository.save(user);
    }

    @Test
    public void testUserAnnouncementRelationship() {
        // Setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle("Sample Announcement");
        announcement.setContent("Sample Announcement");
        announcement.setPinned(false);
        announcement = announcementRepository.save(announcement);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        User fetchedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getAnnouncements()).contains(announcement);

        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.getUser()).isEqualTo(user);
    }

    @Test
    public void testUserQuestionAnswerRelationships() {
        // Setup
        Answer answer = new Answer();
        answer.setUser(user);
        answer.setContent("Sample Answer");
        answer = answerRepository.save(answer);

        Question question = new Question();
        question.setUser(user);
        question.setContent("Sample Question");
        question.setAnswer(answer);
        question.setPublished(false);
        question = questionRepository.save(question);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        User fetchedUser = userRepository.findById(user.getId()).orElse(null);
        assertThat(fetchedUser).isNotNull();
        assertThat(fetchedUser.getQuestions()).contains(question);
        assertThat(fetchedUser.getAnswers()).contains(answer);

        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getUser()).isEqualTo(user);
        assertThat(fetchedQuestion.getAnswer()).isEqualTo(answer);

        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNotNull();
        assertThat(fetchedAnswer.getUser()).isEqualTo(user);
        assertThat(fetchedAnswer.getQuestion()).isEqualTo(question);
    }

    @Test
    public void testAttachmentRelationships() {
        // Setup
        Attachment attachment1 = new Attachment();
        attachment1.setSize(0);
        attachment1 = attachmentRepository.save(attachment1);
        Attachment attachment2 = new Attachment();
        attachment2.setSize(0);
        attachment2 = attachmentRepository.save(attachment2);
        List<Attachment> attachments = List.of(attachment1, attachment2);

        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle("Sample Announcement");
        announcement.setContent("Sample Announcement");
        announcement.setPinned(false);
        announcement.setAttachments(attachments);
        announcementRepository.save(announcement);

        Question question = new Question();
        question.setUser(user);
        question.setContent("Sample Question");
        question.setPublished(false);
        question.setAttachments(attachments);
        questionRepository.save(question);

        Answer answer = new Answer();
        answer.setUser(user);
        answer.setContent("Sample Answer");
        answer.setAttachments(attachments);
        answerRepository.save(answer);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        Announcement fetchedAnnouncement = announcementRepository.findById(announcement.getId()).orElse(null);
        assertThat(fetchedAnnouncement).isNotNull();
        assertThat(fetchedAnnouncement.getAttachments()).containsExactlyInAnyOrder(attachment1, attachment2);

        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAttachments()).containsExactlyInAnyOrder(attachment1, attachment2);

        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNotNull();
        assertThat(fetchedAnswer.getAttachments()).containsExactlyInAnyOrder(attachment1, attachment2);
    }

    @Test
    public void testAnswerLikeRelationship() {
        // Setup
        Answer answer = new Answer();
        answer.setUser(user);
        answer.setContent("Sample Answer");
        answer.setLikesCount(1);
        answer = answerRepository.save(answer);

        AnswerLike answerLike = new AnswerLike();
        answerLike.setUser(user);
        answerLike.setAnswer(answer);
        answerLike = answerLikeRepository.save(answerLike);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNotNull();
        assertThat(fetchedAnswer.getLikes()).contains(answerLike);

        AnswerLike fetchedAnswerLike = answerLikeRepository.findById(answerLike.getId()).orElse(null);
        assertThat(fetchedAnswerLike).isNotNull();
        assertThat(fetchedAnswerLike.getUser()).isEqualTo(user);
        assertThat(fetchedAnswerLike.getAnswer()).isEqualTo(answer);
    }
}
