package tech.bjut.su.appeal.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.entity.*;
import tech.bjut.su.appeal.enums.UserRoleEnum;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class CustomQueryTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private AnnouncementCategoryRepository announcementCategoryRepository;

    @Autowired
    private AnnouncementCarouselRepository announcementCarouselRepository;

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
        attachment4 = attachmentRepository.save(attachment4);
    }

    @Test
    public void testAnnouncementRepository_updateAllCategoryToNullByCategory() {
        // Setup
        AnnouncementCategory category1 = new AnnouncementCategory();
        category1.setName("Sample Category");
        category1 = announcementCategoryRepository.save(category1);

        AnnouncementCategory category2 = new AnnouncementCategory();
        category2.setName("Sample Category");
        category2 = announcementCategoryRepository.save(category2);

        Announcement announcement1 = new Announcement();
        announcement1.setUser(user);
        announcement1.setTitle("Sample Announcement");
        announcement1.setContent("Sample Announcement");
        announcement1.setPinned(false);
        announcement1.setCategory(category1);
        announcementRepository.save(announcement1);

        Announcement announcement2 = new Announcement();
        announcement2.setUser(user);
        announcement2.setTitle("Sample Announcement");
        announcement2.setContent("Sample Announcement");
        announcement2.setPinned(false);
        announcement2.setCategory(category2);
        announcementRepository.save(announcement2);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Execute the query
        announcementRepository.updateAllCategoryToNullByCategory(category1);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        Announcement fetchedAnnouncement1 = announcementRepository.findById(announcement1.getId()).orElse(null);
        Announcement fetchedAnnouncement2 = announcementRepository.findById(announcement2.getId()).orElse(null);
        assertThat(fetchedAnnouncement1).isNotNull();
        assertThat(fetchedAnnouncement1.getCategory()).isNull();
        assertThat(fetchedAnnouncement2).isNotNull();
        assertThat(fetchedAnnouncement2.getCategory()).isEqualTo(category2);
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
    public void testAnnouncementCarouselRepository_findAllAttachmentIdsUsed() {
        // Setup
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle("Sample Announcement");
        announcement.setContent("Sample Announcement");
        announcement.setPinned(false);
        announcement = announcementRepository.save(announcement);

        AnnouncementCarousel carousel1 = new AnnouncementCarousel();
        carousel1.setAnnouncement(announcement);
        carousel1.setCover(attachment1);
        announcementCarouselRepository.save(carousel1);

        AnnouncementCarousel carousel2 = new AnnouncementCarousel();
        carousel2.setAnnouncement(announcement);
        carousel2.setCover(attachment1);
        announcementCarouselRepository.save(carousel2);

        AnnouncementCarousel carousel3 = new AnnouncementCarousel();
        carousel3.setAnnouncement(announcement);
        carousel3.setCover(attachment2);
        announcementCarouselRepository.save(carousel3);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        List<String> attachmentIds = announcementCarouselRepository.findAllAttachmentIdsUsed();
        assertThat(attachmentIds).containsExactlyInAnyOrder(
            attachment1.getId().toString(),
            attachment2.getId().toString()
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

    @Test
    public void testAnswerLikeRepository_findDistinctAnswerIdsByUser() {
        // Setup
        Answer answer1 = new Answer();
        answer1.setUser(user);
        answer1.setContent("Sample Answer");
        answer1 = answerRepository.save(answer1);

        Answer answer2 = new Answer();
        answer2.setUser(user);
        answer2.setContent("Sample Answer");
        answer2 = answerRepository.save(answer2);

        AnswerLike like1 = new AnswerLike();
        like1.setUser(user);
        like1.setAnswer(answer1);
        answerLikeRepository.save(like1);

        AnswerLike like2 = new AnswerLike();
        like2.setUser(user);
        like2.setAnswer(answer1);
        answerLikeRepository.save(like2);

        AnswerLike like3 = new AnswerLike();
        like3.setUser(user);
        like3.setAnswer(answer2);
        answerLikeRepository.save(like3);

        // Clear transaction
        entityManager.flush();
        entityManager.clear();

        // Fetch and verify
        List<Answer> answersFetched = answerLikeRepository.findDistinctAnswerByUser(user);
        assertThat(answersFetched).containsExactlyInAnyOrder(answer1, answer2);
    }
}
