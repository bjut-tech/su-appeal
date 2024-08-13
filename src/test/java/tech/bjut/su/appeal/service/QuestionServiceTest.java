package tech.bjut.su.appeal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.bjut.su.appeal.dto.QuestionAnswerDto;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.entity.Answer;
import tech.bjut.su.appeal.entity.Attachment;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.repository.AnswerRepository;
import tech.bjut.su.appeal.repository.AttachmentRepository;
import tech.bjut.su.appeal.repository.QuestionRepository;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private QuestionService questionService;

    // The test data
    private User user;

    private static final long QUESTION_ID = 1;
    private static final long ANSWER_ID = 1;
    private static final String USER_UID = "user";
    private static final String QUESTION_CONTENT = "title";
    private static final String QUESTION_CONTACT = "contact";
    private static final CampusEnum QUESTION_CAMPUS = CampusEnum.MAIN;
    private static final String ANSWER_CONTENT = "content";
    private static final UUID ATTACHMENT_ID = UUID.randomUUID();

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUid(USER_UID);
    }

    @Test
    public void testCreate_noAttachment_noContact() {
        // set up mocking
        setUpMocking_questionRepository_save();

        // execute
        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setCampus(QUESTION_CAMPUS);
        dto.setContent(QUESTION_CONTENT);

        Question res = questionService.create(user, dto);

        // verify
        assertThat(res).isNotNull();
        assertThat(res.getId()).isGreaterThan(0);
        assertThat(res.getUser()).isEqualTo(user);
        assertThat(res.getContent()).isEqualTo(QUESTION_CONTENT);
        assertThat(res.getCampus()).isEqualTo(QUESTION_CAMPUS);
        assertThat(res.getContact()).isNullOrEmpty();
        assertThat(res.getAttachments()).isNullOrEmpty();
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    public void testCreate_oneAttachment() {
        // set up mocking
        setUpMocking_questionRepository_save();
        setUpMocking_attachmentRepository_findAllById();

        // execute
        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setCampus(QUESTION_CAMPUS);
        dto.setContent(QUESTION_CONTENT);
        dto.setAttachmentIds(List.of(ATTACHMENT_ID));

        Question res = questionService.create(user, dto);

        // verify
        assertThat(res).isNotNull();
        assertThat(res.getId()).isGreaterThan(0);
        assertThat(res.getUser()).isEqualTo(user);
        assertThat(res.getContent()).isEqualTo(QUESTION_CONTENT);
        assertThat(res.getCampus()).isEqualTo(QUESTION_CAMPUS);
        assertThat(res.getContact()).isNullOrEmpty();
        assertThat(res.getAttachments()).hasSize(1);
        assertThat(res.getAttachments().get(0).getId()).isEqualTo(ATTACHMENT_ID);
        verify(questionRepository, times(1)).save(any(Question.class));
        verify(attachmentRepository, times(1)).findAllById(anyList());
    }

    @Test
    public void testCreate_hasContact() {
        // set up mocking
        setUpMocking_questionRepository_save();

        // execute
        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setCampus(QUESTION_CAMPUS);
        dto.setContent(QUESTION_CONTENT);
        dto.setContact(QUESTION_CONTACT);

        Question res = questionService.create(user, dto);

        // verify
        assertThat(res).isNotNull();
        assertThat(res.getId()).isGreaterThan(0);
        assertThat(res.getUser()).isEqualTo(user);
        assertThat(res.getContent()).isEqualTo(QUESTION_CONTENT);
        assertThat(res.getCampus()).isEqualTo(QUESTION_CAMPUS);
        assertThat(res.getContact()).isEqualTo(QUESTION_CONTACT);
        assertThat(res.getAttachments()).isNullOrEmpty();
        verify(questionRepository, times(1)).save(any(Question.class));
    }

    @Test
    public void testSetPublished_setToTrue() {
        // setup mocking
        setUpMocking_questionRepository_save();

        // execute
        Question question = new Question();
        question.setPublished(false);

        questionService.setPublished(question, true);

        // verify
        assertThat(question.isPublished()).isTrue();
        verify(questionRepository, times(1)).save(question);
    }

    @Test
    public void testSetPublished_setToFalse() {
        // setup mocking
        setUpMocking_questionRepository_save();

        // execute
        Question question = new Question();
        question.setPublished(true);

        questionService.setPublished(question, false);

        // verify
        assertThat(question.isPublished()).isFalse();
        verify(questionRepository, times(1)).save(question);
    }

    @Test
    public void testAnswer_noAttachment() {
        // setup mocking
        setUpMocking_questionRepository_save();
        setUpMocking_answerRepository_save();

        // execute
        Question question = new Question();
        question.setId(QUESTION_ID);

        QuestionAnswerDto dto = new QuestionAnswerDto();
        dto.setContent(ANSWER_CONTENT);

        Question res = questionService.answer(question, user, dto);

        // verify
        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(QUESTION_ID);
        assertThat(res.getAnswer()).isNotNull();
        assertThat(res.getAnswer().getId()).isEqualTo(QUESTION_ID);
        assertThat(res.getAnswer().getContent()).isEqualTo(ANSWER_CONTENT);
        assertThat(res.getAnswer().getUser()).isEqualTo(user);
        assertThat(res.getAnswer().getAttachments()).isNullOrEmpty();
        verify(questionRepository, times(1)).save(question);
        verify(answerRepository, times(1)).save(any(Answer.class));
    }

    @Test
    public void testAnswer_oneAttachment() {
        // setup mocking
        setUpMocking_questionRepository_save();
        setUpMocking_answerRepository_save();
        setUpMocking_attachmentRepository_findAllById();

        // execute
        Question question = new Question();
        question.setId(QUESTION_ID);

        QuestionAnswerDto dto = new QuestionAnswerDto();
        dto.setContent(ANSWER_CONTENT);
        dto.setAttachmentIds(List.of(ATTACHMENT_ID));

        Question res = questionService.answer(question, user, dto);

        // verify
        assertThat(res).isNotNull();
        assertThat(res.getId()).isEqualTo(QUESTION_ID);
        assertThat(res.getAnswer()).isNotNull();
        assertThat(res.getAnswer().getId()).isEqualTo(QUESTION_ID);
        assertThat(res.getAnswer().getContent()).isEqualTo(ANSWER_CONTENT);
        assertThat(res.getAnswer().getUser()).isEqualTo(user);
        assertThat(res.getAnswer().getAttachments()).hasSize(1);
        assertThat(res.getAnswer().getAttachments().get(0).getId()).isEqualTo(ATTACHMENT_ID);
        verify(questionRepository, times(1)).save(question);
        verify(answerRepository, times(1)).save(any(Answer.class));
        verify(attachmentRepository, times(1)).findAllById(anyList());
    }

    private void setUpMocking_questionRepository_save() {
        when(questionRepository.save(any(Question.class)))
            .thenAnswer(invocation -> {
                Question question = invocation.getArgument(0);
                question.setId(QUESTION_ID);
                return question;
            });
    }

    private void setUpMocking_answerRepository_save() {
        when(answerRepository.save(any(Answer.class)))
            .thenAnswer(invocation -> {
                Answer answer = invocation.getArgument(0);
                answer.setId(QUESTION_ID);
                return answer;
            });
    }

    private void setUpMocking_attachmentRepository_findAllById() {
        when(attachmentRepository.findAllById(anyList()))
            .thenAnswer(invocation -> {
                List<UUID> ids = invocation.getArgument(0);
                return ids.stream().map(id -> {
                    Attachment attachment = new Attachment();
                    attachment.setId(id);
                    return attachment;
                }).toList();
            });
    }
}
