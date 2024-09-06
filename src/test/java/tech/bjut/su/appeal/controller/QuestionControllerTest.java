package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tech.bjut.su.appeal.config.TestSecurityConfig;
import tech.bjut.su.appeal.dto.QuestionAnswerDto;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.QuestionRepository;
import tech.bjut.su.appeal.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    // the test data
    private static final String USER_UID = "user1";
    private static final UserRoleEnum USER_ROLE = UserRoleEnum.STUDENT;
    private static final String QUESTION_CONTACT = "contact";
    private static final CampusEnum QUESTION_CAMPUS = CampusEnum.MAIN;
    private static final String QUESTION_CONTENT = "question";
    private static final String ANSWER_CONTENT = "answer";

    @Test
    @WithAnonymousUser
    public void testCount_anonymous() throws Exception {
        mvc.perform(get("/questions/count"))
            .andExpectAll(
                jsonPath("$.history").doesNotExist(),
                jsonPath("$.unreplied").doesNotExist()
            );
    }

    @Test
    @WithUserDetails
    public void testCount_nonAdmin() throws Exception {
        mvc.perform(get("/questions/count"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.history").isNumber(),
                jsonPath("$.unreplied").doesNotExist()
            );
    }

    @Test
    @WithUserDetails(TestSecurityConfig.ADMIN_UID)
    public void testCount_admin() throws Exception {
        mvc.perform(get("/questions/count"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.history").isNumber(),
                jsonPath("$.unreplied").isNumber()
            );
    }

    @Test
    @WithAnonymousUser
    public void testHistory_anonymous() throws Exception {
        // setup
        User user = userRepository.findByUid(TestSecurityConfig.USER_UID);

        Question question = new Question();
        question.setUser(null);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(true);
        questionRepository.save(question);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        mvc.perform(get("/questions/history"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.data").isEmpty()
            );
    }

    @Test
    @WithAnonymousUser
    public void testShow_anonymous() throws Exception {
        // setup
        User user = new User();
        user.setUid(USER_UID);
        user.setRole(USER_ROLE);
        user = userRepository.save(user);

        Question question1 = new Question();
        question1.setUser(user);
        question1.setContact(QUESTION_CONTACT);
        question1.setContent(QUESTION_CONTENT);
        question1.setPublished(true);
        question1 = questionRepository.save(question1);

        Question question2 = new Question();
        question2.setUser(null);
        question2.setContent(QUESTION_CONTENT);
        question2.setPublished(false);
        question2 = questionRepository.save(question2);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        mvc.perform(get("/questions/" + question1.getId()))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(question1.getId()),
                jsonPath("$.user").doesNotExist(),
                jsonPath("$.contact").doesNotExist()
            );

        mvc.perform(get("/questions/" + question2.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails
    public void testShow_nonAdmin() throws Exception {
        // setup
        User user1 = userRepository.findByUid(TestSecurityConfig.USER_UID);

        User user2 = new User();
        user2.setUid(USER_UID);
        user2.setRole(USER_ROLE);
        user2 = userRepository.save(user2);

        Question question1 = new Question();
        question1.setUser(user1);
        question1.setContact(QUESTION_CONTACT);
        question1.setContent(QUESTION_CONTENT);
        question1.setPublished(false);
        question1 = questionRepository.save(question1);

        Question question2 = new Question();
        question2.setUser(user2);
        question2.setContact(QUESTION_CONTACT);
        question2.setContent(QUESTION_CONTENT);
        question2.setPublished(true);
        question2 = questionRepository.save(question2);

        Question question3 = new Question();
        question3.setUser(null);
        question3.setContent(QUESTION_CONTENT);
        question3.setPublished(false);
        question3 = questionRepository.save(question3);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        mvc.perform(get("/questions/" + question1.getId()))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(question1.getId()),
                jsonPath("$.user.id").value(user1.getId()),
                jsonPath("$.contact").value(QUESTION_CONTACT)
            );

        mvc.perform(get("/questions/" + question2.getId()))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(question2.getId()),
                jsonPath("$.user").doesNotExist(),
                jsonPath("$.contact").doesNotExist()
            );

        mvc.perform(get("/questions/" + question3.getId()))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(TestSecurityConfig.ADMIN_UID)
    public void testShow_admin() throws Exception {
        // setup
        Question question = new Question();
        question.setUser(null);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(false);
        question = questionRepository.save(question);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // verify
        mvc.perform(get("/questions/" + question.getId()))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(question.getId()),
                jsonPath("$.user").doesNotExist()
            );
    }

    @Test
    @WithUserDetails
    public void testAnswer_nonAdmin() throws Exception {
        // setup
        User user = userRepository.findByUid(TestSecurityConfig.USER_UID);

        Question question = new Question();
        question.setUser(user);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(false);
        question = questionRepository.save(question);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        QuestionAnswerDto dto = new QuestionAnswerDto();
        dto.setContent(ANSWER_CONTENT);

        mvc.perform(post("/questions/{id}/answer", question.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(dto)));

        // verify
        Question questionFetched = questionRepository.findById(question.getId()).orElse(null);
        assertThat(questionFetched).isNotNull();
        assertThat(questionFetched.getAnswer()).isNull();
    }

    @Test
    @WithUserDetails(TestSecurityConfig.ADMIN_UID)
    public void testAnswer_admin() throws Exception {
        // setup
        User user = userRepository.findByUid(TestSecurityConfig.USER_UID);

        Question question = new Question();
        question.setUser(user);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(false);
        question = questionRepository.save(question);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        QuestionAnswerDto dto = new QuestionAnswerDto();
        dto.setContent(ANSWER_CONTENT);

        mvc.perform(post("/questions/{id}/answer", question.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(dto)));

        // verify
        Question questionFetched = questionRepository.findById(question.getId()).orElse(null);
        assertThat(questionFetched).isNotNull();
        assertThat(questionFetched.getAnswer()).isNotNull();
    }

    @Test
    @WithUserDetails
    public void testDelete_nonAdmin() throws Exception {
        // setup
        User user1 = userRepository.findByUid(TestSecurityConfig.USER_UID);
        User user2 = new User();
        user2.setUid(USER_UID);
        user2.setRole(USER_ROLE);
        userRepository.save(user2);

        // q1: current user, not published -> deletable
        Question question1 = new Question();
        question1.setUser(user1);
        question1.setCampus(QUESTION_CAMPUS);
        question1.setContent(QUESTION_CONTENT);
        question1.setPublished(false);
        question1 = questionRepository.save(question1);

        // q2: other user -> not deletable
        Question question2 = new Question();
        question2.setUser(user2);
        question2.setCampus(QUESTION_CAMPUS);
        question2.setContent(QUESTION_CONTENT);
        question2.setPublished(false);
        question2 = questionRepository.save(question2);

        // q3: published -> not deletable
        Question question3 = new Question();
        question3.setUser(user1);
        question3.setCampus(QUESTION_CAMPUS);
        question3.setContent(QUESTION_CONTENT);
        question3.setPublished(true);
        question3 = questionRepository.save(question3);

        // q4: anonymous -> not deletable
        Question question4 = new Question();
        question4.setUser(null);
        question4.setContent(QUESTION_CONTENT);
        question4.setPublished(false);
        question4 = questionRepository.save(question4);

        // clear transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        mvc.perform(delete("/questions/" + question1.getId()));
        mvc.perform(delete("/questions/" + question2.getId()));
        mvc.perform(delete("/questions/" + question3.getId()));
        mvc.perform(delete("/questions/" + question4.getId()));

        // fetch and verify
        Question fetchedQuestion1 = questionRepository.findById(question1.getId()).orElse(null);
        assertThat(fetchedQuestion1).isNull();

        Question fetchedQuestion2 = questionRepository.findById(question2.getId()).orElse(null);
        assertThat(fetchedQuestion2).isNotNull();

        Question fetchedQuestion3 = questionRepository.findById(question3.getId()).orElse(null);
        assertThat(fetchedQuestion3).isNotNull();

        Question fetchedQuestion4 = questionRepository.findById(question4.getId()).orElse(null);
        assertThat(fetchedQuestion4).isNotNull();
    }

    @Test
    @WithUserDetails(TestSecurityConfig.ADMIN_UID)
    public void testDelete_admin() throws Exception {
        // setup
        User user = userRepository.findByUid(TestSecurityConfig.USER_UID);

        Question question = new Question();
        question.setUser(user);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(false);
        question = questionRepository.save(question);

        // flush transaction
        entityManager.flush();
        entityManager.clear();

        // execute
        mvc.perform(delete("/questions/" + question.getId()));

        // verify
        Question questionFetched = questionRepository.findById(question.getId()).orElse(null);
        assertThat(questionFetched).isNull();
    }
}
