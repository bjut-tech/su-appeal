package tech.bjut.su.appeal.controller;

import org.hamcrest.Matchers;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithAnonymousUser
    public void testGetCurrentUser_anonymous() throws Exception {
        mvc.perform(get("/user"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails
    public void testGetCurrentUser_user() throws Exception {
        mvc.perform(get("/user"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.user.uid").value(TestSecurityConfig.USER_UID),
                jsonPath("$.user.name").value(TestSecurityConfig.USER_NAME),
                jsonPath("$.username").value(TestSecurityConfig.USER_UID),
                jsonPath("$.authorities").isArray(),
                jsonPath("$.authorities[*].authority").value(TestSecurityConfig.USER_ROLE.name())
            );
    }

    @Test
    @WithUserDetails(TestSecurityConfig.ADMIN_UID)
    public void testGetCurrentUser_admin() throws Exception {
        mvc.perform(get("/user"))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.user.uid").value(TestSecurityConfig.ADMIN_UID),
                jsonPath("$.user.name").value(TestSecurityConfig.ADMIN_NAME),
                jsonPath("$.username").value(TestSecurityConfig.ADMIN_UID),
                jsonPath("$.authorities").isArray(),
                jsonPath("$.authorities[*].authority").value(Matchers.containsInAnyOrder(TestSecurityConfig.ADMIN_ROLE.name(), "ADMIN"))
            );
    }
}
