package tech.bjut.su.appeal.controller;

import com.jayway.jsonpath.JsonPath;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tech.bjut.su.appeal.config.TestSecurityConfig;
import tech.bjut.su.appeal.entity.Announcement;
import tech.bjut.su.appeal.entity.User;
import tech.bjut.su.appeal.repository.AnnouncementRepository;
import tech.bjut.su.appeal.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class AnnouncementControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    // The test data
    private static final String ANNOUNCEMENT_TITLE = "Title";
    private static final String ANNOUNCEMENT_CONTENT = "Content";

    @BeforeEach
    public void setUp() {
        User user = userRepository.findByUid(TestSecurityConfig.ADMIN_UID);
        Announcement announcement = new Announcement();
        announcement.setUser(user);
        announcement.setTitle(ANNOUNCEMENT_TITLE);
        announcement.setContent(ANNOUNCEMENT_CONTENT);
        announcementRepository.save(announcement);
    }

    @Test
    @WithAnonymousUser
    public void testAnonymous() throws Exception {
        String response = mvc.perform(get("/announcements"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.data[0].id").exists(),
                jsonPath("$.data[0].user").doesNotExist(),
                jsonPath("$.data[0].title").value(ANNOUNCEMENT_TITLE),
                jsonPath("$.data[0].content").value(ANNOUNCEMENT_CONTENT),
                jsonPath("$.cursor").exists()
            ).andReturn().getResponse().getContentAsString();

        long id = ((Number) JsonPath.read(response, "$.data[0].id")).longValue();
        mvc.perform(get("/announcements/" + id))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(id),
                jsonPath("$.user").doesNotExist(),
                jsonPath("$.title").value(ANNOUNCEMENT_TITLE),
                jsonPath("$.content").value(ANNOUNCEMENT_CONTENT)
            );
    }

    @Test
    @WithUserDetails(TestSecurityConfig.ADMIN_UID)
    public void testAdmin() throws Exception {
        String response = mvc.perform(get("/announcements"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.data[0].id").exists(),
                jsonPath("$.data[0].user.uid").value(TestSecurityConfig.ADMIN_UID),
                jsonPath("$.data[0].title").value(ANNOUNCEMENT_TITLE),
                jsonPath("$.data[0].content").value(ANNOUNCEMENT_CONTENT),
                jsonPath("$.cursor").exists()
            ).andReturn().getResponse().getContentAsString();

        long id = ((Number) JsonPath.read(response, "$.data[0].id")).longValue();
        mvc.perform(get("/announcements/" + id))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(id),
                jsonPath("$.user.uid").value(TestSecurityConfig.ADMIN_UID),
                jsonPath("$.title").value(ANNOUNCEMENT_TITLE),
                jsonPath("$.content").value(ANNOUNCEMENT_CONTENT)
            );
    }
}
