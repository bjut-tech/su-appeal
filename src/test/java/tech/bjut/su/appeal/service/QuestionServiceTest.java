package tech.bjut.su.appeal.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import tech.bjut.su.appeal.dto.QuestionAnswerDto;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.entity.*;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QuestionService.class)
@Transactional
public class QuestionServiceTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private AnswerLikeRepository answerLikeRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionService questionService;

    // The test data
    private User user1, user2;

    private static final String USER_UID_1 = "user1";
    private static final String USER_UID_2 = "user2";
    private static final String QUESTION_CONTACT = "contact";
    private static final String QUESTION_CONTENT = "content";
    private static final CampusEnum QUESTION_CAMPUS = CampusEnum.MAIN;
    private static final String ANSWER_CONTENT = "content";

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setUid(USER_UID_1);
        user1.setRole(UserRoleEnum.STUDENT);
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUid(USER_UID_2);
        user2.setRole(UserRoleEnum.STUDENT);
        user2 = userRepository.save(user2);
    }

    @Test
    public void testCreate_noAttachment_noContact() {
        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setCampus(QUESTION_CAMPUS);
        dto.setContent(QUESTION_CONTENT);

        Question question = questionService.create(user1, dto);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getUser()).isEqualTo(user1);
        assertThat(fetchedQuestion.getContent()).isEqualTo(QUESTION_CONTENT);
        assertThat(fetchedQuestion.getCampus()).isEqualTo(QUESTION_CAMPUS);
        assertThat(fetchedQuestion.getContact()).isNullOrEmpty();
        assertThat(fetchedQuestion.isPublished()).isFalse(); // should be not published by default
    }

    @Test
    public void testCreate_canSaveContact() {
        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setCampus(QUESTION_CAMPUS);
        dto.setContent(QUESTION_CONTENT);
        dto.setContact(QUESTION_CONTACT);

        Question question = questionService.create(user1, dto);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getUser()).isEqualTo(user1);
        assertThat(fetchedQuestion.getContent()).isEqualTo(QUESTION_CONTENT);
        assertThat(fetchedQuestion.getCampus()).isEqualTo(QUESTION_CAMPUS);
        assertThat(fetchedQuestion.getContact()).isEqualTo(QUESTION_CONTACT);
        assertThat(fetchedQuestion.isPublished()).isFalse(); // should be not published by default
    }

    @Test
    public void testCreate_canSaveAttachments() {
        // setup
        Attachment attachment = new Attachment();
        attachment.setSize(0);
        attachment = attachmentRepository.save(attachment);

        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setCampus(QUESTION_CAMPUS);
        dto.setContent(QUESTION_CONTENT);
        dto.setAttachmentIds(List.of(attachment.getId()));

        Question question = questionService.create(user1, dto);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAttachments()).containsExactly(attachment);
    }

    @Test
    public void testSetPublished_setToTrue() {
        // setup
        Question question = new Question();
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(false);
        questionRepository.save(question);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        questionService.setPublished(question, true);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.isPublished()).isTrue();
    }

    @Test
    public void testSetPublished_setToFalse() {
        // setup
        Question question = new Question();
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(true);
        questionRepository.save(question);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        questionService.setPublished(question, false);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.isPublished()).isFalse();
    }

    @Test
    public void testAnswer_noAttachment() {
        // setup
        Question question = new Question();
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question = questionRepository.save(question);

        QuestionAnswerDto dto = new QuestionAnswerDto();
        dto.setContent(ANSWER_CONTENT);

        question = questionService.answer(question, user1, dto);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAnswer()).isNotNull();
        assertThat(fetchedQuestion.getAnswer().getContent()).isEqualTo(ANSWER_CONTENT);
        assertThat(fetchedQuestion.getAnswer().getLikesCount()).isZero();
    }

    @Test
    public void testAnswer_canSaveAttachments() {
        // setup
        Question question = new Question();
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question = questionRepository.save(question);

        Attachment attachment = new Attachment();
        attachment.setSize(0);
        attachment = attachmentRepository.save(attachment);

        QuestionAnswerDto dto = new QuestionAnswerDto();
        dto.setContent(ANSWER_CONTENT);
        dto.setAttachmentIds(List.of(attachment.getId()));

        question = questionService.answer(question, user1, dto);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAnswer()).isNotNull();
        assertThat(fetchedQuestion.getAnswer().getAttachments()).containsExactly(attachment);
    }

    @Test
    public void testLikeAnswer() {
        // setup
        Answer answer = new Answer();
        answer.setUser(user1);
        answer.setContent(ANSWER_CONTENT);
        answer = answerRepository.save(answer);

        Question question = new Question();
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setAnswer(answer);
        question = questionRepository.save(question);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNotNull();
        questionService.likeAnswer(user1, fetchedAnswer);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAnswer()).isNotNull();
        assertThat(fetchedQuestion.getAnswer().getLikesCount()).isEqualTo(1);

        List<AnswerLike> fetchedLikes = answerLikeRepository.findByUser(user1);
        assertThat(fetchedLikes)
            .filteredOn(like -> like.getAnswer().equals(fetchedQuestion.getAnswer()))
            .hasSize(1);
    }

    @Test
    public void testLikeAnswer_anonymous() {
        // setup
        Answer answer = new Answer();
        answer.setUser(user1);
        answer.setContent(ANSWER_CONTENT);
        answer = answerRepository.save(answer);

        Question question = new Question();
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setAnswer(answer);
        question = questionRepository.save(question);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNotNull();
        questionService.likeAnswer(null, fetchedAnswer);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAnswer()).isNotNull();
        assertThat(fetchedQuestion.getAnswer().getLikesCount()).isEqualTo(1);
    }

    @Test
    public void testUnlikeAnswer() {
        // setup
        Answer answer = new Answer();
        answer.setUser(user1);
        answer.setContent(ANSWER_CONTENT);
        answer.setLikesCount(1);
        answer = answerRepository.save(answer);

        AnswerLike like = new AnswerLike();
        like.setUser(user1);
        like.setAnswer(answer);
        answerLikeRepository.save(like);

        Question question = new Question();
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setAnswer(answer);
        question = questionRepository.save(question);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNotNull();
        questionService.unlikeAnswer(user1, fetchedAnswer);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAnswer()).isNotNull();
        assertThat(fetchedQuestion.getAnswer().getLikesCount()).isZero();

        List<AnswerLike> fetchedLikes = answerLikeRepository.findByUser(user1);
        assertThat(fetchedLikes)
            .filteredOn(fetchedLike -> fetchedLike.getAnswer().equals(fetchedQuestion.getAnswer()))
            .isEmpty();
    }

    @Test
    public void testDeleteQuestion_byEntity() {
        // the byEntity delete is only called by admins, so any question can be deleted

        // setup
        Answer answer = new Answer();
        answer.setUser(user1);
        answer.setContent(ANSWER_CONTENT);
        answer = answerRepository.save(answer);

        AnswerLike like = new AnswerLike();
        like.setUser(user1);
        like.setAnswer(answer);
        like = answerLikeRepository.save(like);

        Question question = new Question();
        question.setAnswer(answer);
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(true);
        question = questionRepository.save(question);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        questionService.delete(question);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        // all the stuff above should be gone when the question is deleted
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNull();

        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNull();

        AnswerLike fetchedLike = answerLikeRepository.findById(like.getId()).orElse(null);
        assertThat(fetchedLike).isNull();
    }

    @Test
    public void testDeleteQuestion_withUser() {
        // this is the delete method called by users,
        // they can only delete questions owned by them and only when the question is not published

        // setup
        Answer answer1 = new Answer();
        answer1.setUser(user1);
        answer1.setContent(ANSWER_CONTENT);
        answer1 = answerRepository.save(answer1);

        AnswerLike like1 = new AnswerLike();
        like1.setUser(user1);
        like1.setAnswer(answer1);
        like1 = answerLikeRepository.save(like1);

        // q1: current user, not published -> deletable
        Question question1 = new Question();
        question1.setAnswer(answer1);
        question1.setUser(user1);
        question1.setCampus(QUESTION_CAMPUS);
        question1.setContent(QUESTION_CONTENT);
        question1 = questionRepository.save(question1);

        // q2: other user -> not deletable
        Question question2 = new Question();
        question2.setUser(user2);
        question2.setCampus(QUESTION_CAMPUS);
        question2.setContent(QUESTION_CONTENT);
        question2 = questionRepository.save(question2);

        // q3: published -> not deletable
        Question question3 = new Question();
        question3.setUser(user1);
        question3.setCampus(QUESTION_CAMPUS);
        question3.setContent(QUESTION_CONTENT);
        question3.setPublished(true);
        question3 = questionRepository.save(question3);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        questionService.delete(user1, question1);
        questionService.delete(user1, question2);
        questionService.delete(user1, question3);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        Answer fetchedAnswer1 = answerRepository.findById(answer1.getId()).orElse(null);
        assertThat(fetchedAnswer1).isNull();

        AnswerLike fetchedLike1 = answerLikeRepository.findById(like1.getId()).orElse(null);
        assertThat(fetchedLike1).isNull();

        Question fetchedQuestion1 = questionRepository.findById(question1.getId()).orElse(null);
        assertThat(fetchedQuestion1).isNull();

        Question fetchedQuestion2 = questionRepository.findById(question2.getId()).orElse(null);
        assertThat(fetchedQuestion2).isNotNull();

        Question fetchedQuestion3 = questionRepository.findById(question3.getId()).orElse(null);
        assertThat(fetchedQuestion3).isNotNull();
    }

    @Test
    public void testDeleteAnswer() {
        // setup
        Answer answer = new Answer();
        answer.setUser(user1);
        answer.setContent(ANSWER_CONTENT);
        answer = answerRepository.save(answer);

        AnswerLike like = new AnswerLike();
        like.setUser(user1);
        like.setAnswer(answer);
        like = answerLikeRepository.save(like);

        Question question = new Question();
        question.setAnswer(answer);
        question.setUser(user1);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(true);
        question = questionRepository.save(question);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        Question fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        questionService.deleteAnswer(fetchedQuestion);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // fetch and verify
        fetchedQuestion = questionRepository.findById(question.getId()).orElse(null);
        assertThat(fetchedQuestion).isNotNull();
        assertThat(fetchedQuestion.getAnswer()).isNull();

        Answer fetchedAnswer = answerRepository.findById(answer.getId()).orElse(null);
        assertThat(fetchedAnswer).isNull();

        AnswerLike fetchedLike = answerLikeRepository.findById(like.getId()).orElse(null);
        assertThat(fetchedLike).isNull();
    }
}
