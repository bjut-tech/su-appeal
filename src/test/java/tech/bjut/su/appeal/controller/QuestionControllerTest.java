package tech.bjut.su.appeal.controller;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tech.bjut.su.appeal.config.TestSecurityConfig;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.enums.UserRoleEnum;
import tech.bjut.su.appeal.repository.QuestionRepository;
import tech.bjut.su.appeal.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class QuestionControllerTest {

    @Autowired
    private MockMvc mvc;

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
    private static final String QUESTION_CONTENT = "question";

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
    @Transactional
    @Rollback
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
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    @Rollback
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
}
